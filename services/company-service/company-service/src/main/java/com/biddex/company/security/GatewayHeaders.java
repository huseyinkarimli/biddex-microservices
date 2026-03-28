package com.biddex.company.security;

public final class GatewayHeaders {

	public static final String USER_ID = "X-User-Id";
	public static final String USER_EMAIL = "X-User-Email";
	public static final String COMPANY_ID = "X-Company-Id";
	public static final String COMPANY_TYPE = "X-Company-Type";
	public static final String COMPANY_ROLE = "X-Company-Role";

	/**
	 * Comma-separated authorities from the JWT (e.g. {@code ROLE_ADMIN,ROLE_USER}). Gateway should forward this.
	 */
	public static final String USER_ROLES = "X-User-Roles";

	private GatewayHeaders() {
	}
}
