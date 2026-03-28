package com.biddex.company.security;

import com.biddex.company.exception.UnauthorizedOperationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class GatewaySecurity {

	private GatewaySecurity() {
	}

	public static GatewayUser requireCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof GatewayAuthenticationToken token) {
			return token.getGatewayUser();
		}
		throw new UnauthorizedOperationException("Missing gateway user context");
	}

	public static GatewayUser currentUserOrNull() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof GatewayAuthenticationToken token) {
			return token.getGatewayUser();
		}
		return null;
	}
}
