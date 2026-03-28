package com.biddex.auth;

import java.util.regex.Pattern;

public final class VoenRules {

	private static final Pattern VOEN_PATTERN = Pattern.compile("^[0-9]{10}$");

	private VoenRules() {
	}

	/**
	 * Azerbaijani VÖEN: exactly 10 digits (numeric, therefore first character is a digit).
	 */
	public static boolean isValidFormat(String voen) {
		if (voen == null) {
			return false;
		}
		return VOEN_PATTERN.matcher(voen.trim()).matches();
	}
}
