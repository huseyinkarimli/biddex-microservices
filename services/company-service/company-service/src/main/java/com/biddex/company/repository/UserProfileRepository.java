package com.biddex.company.repository;

import com.biddex.company.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

	Optional<UserProfile> findByUserIdAndCompany_IdAndIsActiveTrue(UUID userId, UUID companyId);

	List<UserProfile> findByCompany_IdAndIsActiveTrueOrderByCreatedAtAsc(UUID companyId);

	Optional<UserProfile> findByUserIdAndCompany_Id(UUID userId, UUID companyId);
}
