package com.biddex.auth;

public interface VoenValidationService {

	/**
	 * @return true if the VÖEN is acceptable for registration (format + profile-specific rules).
	 */
	boolean validateVoen(String voen);
}
