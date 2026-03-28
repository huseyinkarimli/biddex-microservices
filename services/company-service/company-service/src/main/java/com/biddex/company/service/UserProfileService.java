package com.biddex.company.service;

import com.biddex.company.dto.UserProfileDto;
import com.biddex.company.dto.UserProfileUpdateRequest;
import com.biddex.company.entity.Company;
import com.biddex.company.entity.CompanyRole;
import com.biddex.company.entity.UserProfile;
import com.biddex.company.exception.CompanyNotFoundException;
import com.biddex.company.exception.UnauthorizedOperationException;
import com.biddex.company.exception.UserProfileNotFoundException;
import com.biddex.company.mapper.UserProfileMapper;
import com.biddex.company.repository.CompanyRepository;
import com.biddex.company.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

	private final UserProfileRepository userProfileRepository;
	private final CompanyRepository companyRepository;
	private final UserProfileMapper userProfileMapper;

	@Transactional
	public UserProfileDto createOrUpdateProfile(UUID userId, UUID companyId, CompanyRole roleFromGateway, UserProfileUpdateRequest request) {
		if (companyId == null) {
			throw new UnauthorizedOperationException("Company context is required");
		}
		if (roleFromGateway == null) {
			throw new UnauthorizedOperationException("Company role header is required for profile provisioning");
		}
		Company company = companyRepository.findById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException("Company not found"));

		UserProfile profile = userProfileRepository.findByUserIdAndCompany_Id(userId, companyId)
				.orElse(null);

		if (profile == null) {
			profile = UserProfile.builder()
					.userId(userId)
					.company(company)
					.companyRole(roleFromGateway)
					.position(request.getPosition())
					.phone(request.getPhone())
					.isActive(true)
					.build();
		} else {
			if (request.getPosition() != null) {
				profile.setPosition(request.getPosition());
			}
			if (request.getPhone() != null) {
				profile.setPhone(request.getPhone());
			}
		}

		return userProfileMapper.toDto(userProfileRepository.save(profile));
	}

	@Transactional(readOnly = true)
	public UserProfileDto getProfile(UUID userId, UUID companyId) {
		if (companyId == null) {
			throw new UnauthorizedOperationException("Company context is required");
		}
		return userProfileRepository.findByUserIdAndCompany_IdAndIsActiveTrue(userId, companyId)
				.map(userProfileMapper::toDto)
				.orElseThrow(() -> new UserProfileNotFoundException("Profile not found for user and company"));
	}

	@Transactional(readOnly = true)
	public List<UserProfileDto> getCompanyMembers(UUID companyId) {
		return userProfileRepository.findByCompany_IdAndIsActiveTrueOrderByCreatedAtAsc(companyId)
				.stream()
				.map(userProfileMapper::toDto)
				.toList();
	}

	@Transactional
	public UserProfileDto updateMemberRole(UUID actorUserId, UUID companyId, UUID targetUserId, CompanyRole newRole) {
		assertCompanyAdmin(actorUserId, companyId);
		UserProfile target = userProfileRepository.findByUserIdAndCompany_IdAndIsActiveTrue(targetUserId, companyId)
				.orElseThrow(() -> new UserProfileNotFoundException("Target user is not an active member of this company"));
		target.setCompanyRole(newRole);
		return userProfileMapper.toDto(userProfileRepository.save(target));
	}

	private void assertCompanyAdmin(UUID requestingUserId, UUID companyId) {
		UserProfile profile = userProfileRepository.findByUserIdAndCompany_IdAndIsActiveTrue(requestingUserId, companyId)
				.orElseThrow(() -> new UnauthorizedOperationException("Not an active member of this company"));
		if (profile.getCompanyRole() != CompanyRole.COMPANY_ADMIN) {
			throw new UnauthorizedOperationException("Company administrator role required");
		}
	}
}
