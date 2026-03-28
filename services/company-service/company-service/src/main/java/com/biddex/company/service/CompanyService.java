package com.biddex.company.service;

import com.biddex.company.client.AuthServiceClient;
import com.biddex.company.dto.CompanyDto;
import com.biddex.company.dto.CompanyShortDto;
import com.biddex.company.dto.CompanyUpdateRequest;
import com.biddex.company.entity.Company;
import com.biddex.company.entity.CompanyRole;
import com.biddex.company.entity.CompanyType;
import com.biddex.company.entity.Sector;
import com.biddex.company.entity.UserProfile;
import com.biddex.company.entity.VerificationStatus;
import com.biddex.company.event.CompanyKafkaProducer;
import com.biddex.company.event.CompanyVerifiedEvent;
import com.biddex.company.exception.CompanyNotFoundException;
import com.biddex.company.exception.UnauthorizedOperationException;
import com.biddex.company.mapper.CompanyMapper;
import com.biddex.company.repository.CompanyRepository;
import com.biddex.company.repository.UserProfileRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

	private static final String ROLE_ADMIN = "ROLE_ADMIN";

	private final CompanyRepository companyRepository;
	private final UserProfileRepository userProfileRepository;
	private final CompanyMapper companyMapper;
	private final CompanyKafkaProducer companyKafkaProducer;
	private final AuthServiceClient authServiceClient;

	@Transactional(readOnly = true)
	public CompanyDto getCompanyById(UUID id) {
		return companyMapper.toDto(companyById(id));
	}

	@Transactional(readOnly = true)
	public CompanyDto getCompanyByVoen(String voen) {
		return companyMapper.toDto(
				companyRepository.findByVoen(voen.trim())
						.orElseThrow(() -> new CompanyNotFoundException("Company not found for VÖEN"))
		);
	}

	@Transactional
	public CompanyDto updateCompany(UUID companyId, CompanyUpdateRequest request, UUID requestingUserId) {
		assertCompanyAdmin(requestingUserId, companyId);
		Company company = companyById(companyId);
		companyMapper.applyUpdate(request, company);
		return companyMapper.toDto(companyRepository.save(company));
	}

	@Transactional
	public void verifyCompany(UUID companyId, String authorizationHeader) {
		validateWithAuthServiceIfPresent(authorizationHeader);
		assertPlatformAdmin();
		Company company = companyById(companyId);
		company.setVerificationStatus(VerificationStatus.VERIFIED);
		company.setRejectionReason(null);
		companyRepository.save(company);
		companyKafkaProducer.publishCompanyVerified(
				new CompanyVerifiedEvent(company.getId(), company.getName(), company.getSector(), company.getCompanyType())
		);
	}

	@Transactional
	public void rejectCompany(UUID companyId, String reason, String authorizationHeader) {
		validateWithAuthServiceIfPresent(authorizationHeader);
		assertPlatformAdmin();
		Company company = companyById(companyId);
		company.setVerificationStatus(VerificationStatus.REJECTED);
		company.setRejectionReason(reason);
		companyRepository.save(company);
	}

	@Transactional(readOnly = true)
	public boolean existsById(UUID id) {
		return companyRepository.existsById(id);
	}

	@Transactional(readOnly = true)
	public List<CompanyShortDto> getCompaniesBySector(Sector sector) {
		return companyRepository
				.findBySectorAndCompanyTypeAndIsActiveTrueOrderBySellerRatingDesc(sector, CompanyType.SELLER)
				.stream()
				.map(companyMapper::toShortDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<CompanyDto> getPendingCompanies() {
		assertPlatformAdmin();
		return companyRepository.findByVerificationStatusOrderByCreatedAtAsc(VerificationStatus.PENDING)
				.stream()
				.map(companyMapper::toDto)
				.toList();
	}

	private Company companyById(UUID id) {
		return companyRepository.findById(id)
				.orElseThrow(() -> new CompanyNotFoundException("Company not found"));
	}

	private void assertCompanyAdmin(UUID requestingUserId, UUID companyId) {
		UserProfile profile = userProfileRepository.findByUserIdAndCompany_IdAndIsActiveTrue(requestingUserId, companyId)
				.orElseThrow(() -> new UnauthorizedOperationException("Not an active member of this company"));
		if (profile.getCompanyRole() != CompanyRole.COMPANY_ADMIN) {
			throw new UnauthorizedOperationException("Company administrator role required");
		}
	}

	private void assertPlatformAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).noneMatch(ROLE_ADMIN::equals)) {
			throw new UnauthorizedOperationException("Platform administrator role required");
		}
	}

	private void validateWithAuthServiceIfPresent(String authorizationHeader) {
		if (authorizationHeader == null || authorizationHeader.isBlank()) {
			return;
		}
		try {
			authServiceClient.validate(authorizationHeader);
		} catch (FeignException e) {
			int status = e.status();
			if (status == 401) {
				throw new UnauthorizedOperationException("Invalid or expired token");
			}
			if (status == 403) {
				throw new UnauthorizedOperationException("Auth service rejected the token");
			}
			throw new UnauthorizedOperationException("Auth service validation failed");
		}
	}
}
