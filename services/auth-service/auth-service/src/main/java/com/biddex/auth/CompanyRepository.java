package com.biddex.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

	boolean existsByVoen(String voen);

	Optional<Company> findByVoen(String voen);
}
