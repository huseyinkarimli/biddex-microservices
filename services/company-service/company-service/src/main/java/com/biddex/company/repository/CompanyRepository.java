package com.biddex.company.repository;

import com.biddex.company.entity.Company;
import com.biddex.company.entity.CompanyType;
import com.biddex.company.entity.Sector;
import com.biddex.company.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

	Optional<Company> findByVoen(String voen);

	List<Company> findBySectorAndCompanyTypeAndIsActiveTrueOrderBySellerRatingDesc(
			Sector sector,
			CompanyType companyType
	);

	List<Company> findByVerificationStatusOrderByCreatedAtAsc(VerificationStatus status);
}
