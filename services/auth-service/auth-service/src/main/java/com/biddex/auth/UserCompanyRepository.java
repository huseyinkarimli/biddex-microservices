package com.biddex.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCompanyRepository extends JpaRepository<UserCompany, UUID> {

	@Query("""
			select uc from UserCompany uc join fetch uc.company
			where uc.user.id = :userId and uc.isActive = true
			order by uc.createdAt asc
			""")
	List<UserCompany> findActiveMembershipsWithCompany(@Param("userId") UUID userId);

	Optional<UserCompany> findByUser_IdAndCompany_IdAndIsActiveTrue(UUID userId, UUID companyId);
}
