package com.biddex.company.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class GatewayAuthenticationToken extends AbstractAuthenticationToken {

	private final GatewayUser gatewayUser;

	public GatewayAuthenticationToken(GatewayUser gatewayUser, Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.gatewayUser = gatewayUser;
		setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return gatewayUser;
	}

	public GatewayUser getGatewayUser() {
		return gatewayUser;
	}
}
