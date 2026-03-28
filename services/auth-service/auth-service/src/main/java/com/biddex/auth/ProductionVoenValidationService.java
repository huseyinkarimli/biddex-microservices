package com.biddex.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Non-dev profiles: format validation, duplicate check against the database, and implicit registry acceptance.
 */
@Service
@Profile("!dev")
@RequiredArgsConstructor
public class ProductionVoenValidationService implements VoenValidationService {

	private final CompanyRepository companyRepository;

	@Override
	public boolean validateVoen(String voen) {
		if (!VoenRules.isValidFormat(voen)) {
			return false;
		}
		if (companyRepository.existsByVoen(voen.trim())) {
			return false;
		}
		return true;
	}
}
