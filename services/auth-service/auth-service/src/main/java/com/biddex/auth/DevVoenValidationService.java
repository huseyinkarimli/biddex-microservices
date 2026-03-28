package com.biddex.auth;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Development profile: external registry check is mocked as always successful after format validation.
 */
@Service
@Profile("dev")
public class DevVoenValidationService implements VoenValidationService {

	@Override
	public boolean validateVoen(String voen) {
		return VoenRules.isValidFormat(voen);
	}
}
