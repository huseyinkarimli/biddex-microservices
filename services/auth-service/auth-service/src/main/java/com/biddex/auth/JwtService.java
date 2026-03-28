package com.biddex.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

	public static final String CLAIM_TOKEN_KIND = "tkn";
	public static final String TOKEN_KIND_ACCESS = "access";
	public static final String TOKEN_KIND_REFRESH = "refresh";

	public static final String CLAIM_USER_ID = "uid";
	public static final String CLAIM_COMPANY_ID = "cid";
	public static final String CLAIM_COMPANY_TYPE = "ctp";
	public static final String CLAIM_COMPANY_ROLE = "crl";
	public static final String CLAIM_ROLES = "roles";

	private final JwtProperties jwtProperties;

	public String generateAccessToken(UserDetails userDetails) {
		AuthUserPrincipal principal = requirePrincipal(userDetails);
		Date now = new Date();
		Date exp = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());
		String jti = UUID.randomUUID().toString();
		List<String> roleNames = principal.getAuthorities().stream()
				.map(a -> a.getAuthority())
				.collect(Collectors.toList());
		return Jwts.builder()
				.id(jti)
				.subject(principal.getUsername())
				.issuedAt(now)
				.expiration(exp)
				.claim(CLAIM_TOKEN_KIND, TOKEN_KIND_ACCESS)
				.claim(CLAIM_USER_ID, principal.getUserId().toString())
				.claim(CLAIM_COMPANY_ID, principal.getCompanyId() != null ? principal.getCompanyId().toString() : "")
				.claim(CLAIM_COMPANY_TYPE, principal.getCompanyType() != null ? principal.getCompanyType().name() : "")
				.claim(CLAIM_COMPANY_ROLE, principal.getCompanyRole() != null ? principal.getCompanyRole().name() : "")
				.claim(CLAIM_ROLES, roleNames)
				.signWith(signingKey())
				.compact();
	}

	public String generateRefreshToken(UserDetails userDetails) {
		AuthUserPrincipal principal = requirePrincipal(userDetails);
		Date now = new Date();
		Date exp = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());
		String jti = UUID.randomUUID().toString();
		return Jwts.builder()
				.id(jti)
				.subject(principal.getUsername())
				.issuedAt(now)
				.expiration(exp)
				.claim(CLAIM_TOKEN_KIND, TOKEN_KIND_REFRESH)
				.claim(CLAIM_USER_ID, principal.getUserId().toString())
				.signWith(signingKey())
				.compact();
	}

	public void validateToken(String token) {
		parseClaims(token, TOKEN_KIND_ACCESS);
	}

	public Claims parseAndValidateAccessToken(String token) {
		return parseClaims(token, TOKEN_KIND_ACCESS);
	}

	public Claims parseAndValidateRefreshToken(String token) {
		return parseClaims(token, TOKEN_KIND_REFRESH);
	}

	public String extractEmail(String token) {
		return parseClaimsUnverifiedType(token).getSubject();
	}

	public long getAccessTokenExpirationMillis() {
		return jwtProperties.getAccessTokenExpiration();
	}

	public long getRefreshTokenExpirationMillis() {
		return jwtProperties.getRefreshTokenExpiration();
	}

	private Claims parseClaims(String token, String expectedKind) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(signingKey())
					.build()
					.parseSignedClaims(token)
					.getPayload();
			String kind = claims.get(CLAIM_TOKEN_KIND, String.class);
			if (!expectedKind.equals(kind)) {
				throw new InvalidTokenException("Unexpected token type");
			}
			return claims;
		} catch (ExpiredJwtException e) {
			throw new TokenExpiredException("Token has expired");
		} catch (JwtException e) {
			throw new InvalidTokenException("Invalid token: " + e.getMessage());
		}
	}

	/**
	 * Used where type claim is not yet validated (e.g. before classification).
	 */
	private Claims parseClaimsUnverifiedType(String token) {
		try {
			return Jwts.parser()
					.verifyWith(signingKey())
					.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch (ExpiredJwtException e) {
			throw new TokenExpiredException("Token has expired");
		} catch (JwtException e) {
			throw new InvalidTokenException("Invalid token: " + e.getMessage());
		}
	}

	private AuthUserPrincipal requirePrincipal(UserDetails userDetails) {
		if (userDetails instanceof AuthUserPrincipal principal) {
			return principal;
		}
		throw new IllegalArgumentException("UserDetails must be AuthUserPrincipal");
	}

	private SecretKey signingKey() {
		String raw = jwtProperties.getSecret().trim();
		try {
			byte[] bytes = Decoders.BASE64.decode(raw);
			return Keys.hmacShaKeyFor(bytes);
		} catch (IllegalArgumentException ignored) {
			byte[] utf8 = raw.getBytes(StandardCharsets.UTF_8);
			if (utf8.length < 32) {
				throw new IllegalStateException("jwt.secret must be Base64(32 bytes) or at least 32 UTF-8 characters");
			}
			return Keys.hmacShaKeyFor(utf8);
		}
	}
}
