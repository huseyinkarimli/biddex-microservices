package com.biddex.company.security;

import com.biddex.company.entity.CompanyRole;
import com.biddex.company.entity.CompanyType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

	private static final AntPathMatcher MATCHER = new AntPathMatcher();

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		if (isPublicPath(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		String userIdRaw = request.getHeader(GatewayHeaders.USER_ID);
		if (userIdRaw == null || userIdRaw.isBlank()) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing " + GatewayHeaders.USER_ID);
			return;
		}

		UUID userId;
		try {
			userId = UUID.fromString(userIdRaw.trim());
		} catch (IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid " + GatewayHeaders.USER_ID);
			return;
		}

		String email = request.getHeader(GatewayHeaders.USER_EMAIL);
		if (email == null) {
			email = "";
		}

		UUID companyId = null;
		String companyIdRaw = request.getHeader(GatewayHeaders.COMPANY_ID);
		if (companyIdRaw != null && !companyIdRaw.isBlank()) {
			try {
				companyId = UUID.fromString(companyIdRaw.trim());
			} catch (IllegalArgumentException e) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid " + GatewayHeaders.COMPANY_ID);
				return;
			}
		}

		CompanyType companyType = null;
		String companyTypeRaw = request.getHeader(GatewayHeaders.COMPANY_TYPE);
		if (companyTypeRaw != null && !companyTypeRaw.isBlank()) {
			try {
				companyType = CompanyType.valueOf(companyTypeRaw.trim());
			} catch (IllegalArgumentException e) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid " + GatewayHeaders.COMPANY_TYPE);
				return;
			}
		}

		CompanyRole companyRole = null;
		String companyRoleRaw = request.getHeader(GatewayHeaders.COMPANY_ROLE);
		if (companyRoleRaw != null && !companyRoleRaw.isBlank()) {
			try {
				companyRole = CompanyRole.valueOf(companyRoleRaw.trim());
			} catch (IllegalArgumentException e) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid " + GatewayHeaders.COMPANY_ROLE);
				return;
			}
		}

		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		String rolesHeader = request.getHeader(GatewayHeaders.USER_ROLES);
		if (rolesHeader != null && !rolesHeader.isBlank()) {
			for (String part : rolesHeader.split(",")) {
				String role = part.trim();
				if (role.isEmpty()) {
					continue;
				}
				if (!role.startsWith("ROLE_")) {
					role = "ROLE_" + role;
				}
				authorities.add(new SimpleGrantedAuthority(role));
			}
		}

		if (companyRole != null) {
			authorities.add(new SimpleGrantedAuthority("ROLE_COMPANY_" + companyRole.name()));
		}

		GatewayUser gatewayUser = new GatewayUser(userId, email, companyId, companyType, companyRole);
		GatewayAuthenticationToken authentication = new GatewayAuthenticationToken(gatewayUser, authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}

	private boolean isPublicPath(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
			uri = uri.substring(contextPath.length());
		}
		if (uri.startsWith("/actuator") || uri.startsWith("/error")) {
			return true;
		}
		return HttpMethod.GET.matches(request.getMethod()) && MATCHER.match("/companies/*/exists", uri);
	}
}
