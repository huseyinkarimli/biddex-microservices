package com.biddex.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final RedisTemplate<String, String> redisTemplate;

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		if (isPublicPath(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
			return;
		}

		String token = header.substring(7).trim();
		if (token.isEmpty()) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
			return;
		}

		if (Boolean.TRUE.equals(redisTemplate.hasKey(AuthRedisKeys.BLACKLIST_PREFIX + token))) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked");
			return;
		}

		final Claims claims;
		try {
			claims = jwtService.parseAndValidateAccessToken(token);
		} catch (TokenExpiredException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
			return;
		} catch (InvalidTokenException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
			return;
		}

		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		Object rawRoles = claims.get(JwtService.CLAIM_ROLES);
		if (rawRoles instanceof List<?> list) {
			for (Object o : list) {
				if (o instanceof String s) {
					authorities.add(new SimpleGrantedAuthority(s));
				}
			}
		}
		if (authorities.isEmpty()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		}

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				claims.getSubject(),
				null,
				authorities
		);
		authentication.setDetails(claims);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}

	private boolean isPublicPath(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
			uri = uri.substring(contextPath.length());
		}
		return uri.startsWith("/auth")
				|| uri.startsWith("/actuator")
				|| uri.startsWith("/error");
	}
}
