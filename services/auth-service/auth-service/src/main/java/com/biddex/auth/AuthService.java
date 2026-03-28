package com.biddex.auth;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.EnumSet;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {


	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;
	private final UserCompanyRepository userCompanyRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final UserDetailsServiceImpl userDetailsService;
	private final RedisTemplate<String, String> redisTemplate;
	private final VoenValidationService voenValidationService;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.getEmail());
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new UserAlreadyExistsException("Email already registered");
		}
		if (companyRepository.existsByVoen(request.getVoen().trim())) {
			throw new VoenAlreadyRegisteredException("VÖEN already registered");
		}
		if (!voenValidationService.validateVoen(request.getVoen().trim())) {
			throw new InvalidVoenException("Invalid VÖEN format or verification failed");
		}

		Company company = Company.builder()
				.name(request.getCompanyName().trim())
				.voen(request.getVoen().trim())
				.sector(request.getSector())
				.companyType(request.getCompanyType())
				.verificationStatus(VerificationStatus.PENDING)
				.build();
		companyRepository.save(company);

		User user = User.builder()
				.email(email)
				.password(passwordEncoder.encode(request.getPassword()))
				.firstName(request.getFirstName().trim())
				.lastName(request.getLastName().trim())
				.isActive(true)
				.isVerified(false)
				.systemRoles(EnumSet.of(SystemRole.USER))
				.build();
		userRepository.save(user);

		UserCompany membership = UserCompany.builder()
				.user(user)
				.company(company)
				.companyRole(CompanyRole.COMPANY_ADMIN)
				.isActive(true)
				.build();
		userCompanyRepository.save(membership);

		AuthUserPrincipal principal = AuthUserPrincipal.from(user, membership);
		return issueTokens(principal);
	}

	public AuthResponse login(LoginRequest request) {
		String email = normalizeEmail(request.getEmail());
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
		if (!user.isActive()) {
			throw new InvalidCredentialsException("Invalid email or password");
		}
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new InvalidCredentialsException("Invalid email or password");
		}
		var memberships = userCompanyRepository.findActiveMembershipsWithCompany(user.getId());
		AuthUserPrincipal principal = AuthUserPrincipal.from(user, AuthUserPrincipal.selectPrimaryMembership(memberships));
		return issueTokens(principal);
	}

	public AuthResponse refreshToken(String refreshTokenRaw) {
		Claims claims = jwtService.parseAndValidateRefreshToken(refreshTokenRaw);
		UUID userId = UUID.fromString(claims.get(JwtService.CLAIM_USER_ID, String.class));
		String email = claims.getSubject();

		String stored = redisTemplate.opsForValue().get(AuthRedisKeys.REFRESH_PREFIX + userId);
		if (stored == null || !stored.equals(refreshTokenRaw)) {
			throw new RefreshTokenMismatchException("Refresh token is invalid or has been replaced");
		}

		UserDetails userDetails = userDetailsService.loadUserByUsername(email);
		if (!(userDetails instanceof AuthUserPrincipal principal)) {
			throw new InvalidTokenException("Cannot rebuild principal");
		}
		if (!principal.getUserId().equals(userId)) {
			throw new RefreshTokenMismatchException("Refresh token does not match user");
		}

		String newAccess = jwtService.generateAccessToken(principal);
		return AuthResponse.builder()
				.accessToken(newAccess)
				.refreshToken(refreshTokenRaw)
				.tokenType("Bearer")
				.expiresIn(jwtService.getAccessTokenExpirationMillis() / 1000)
				.build();
	}

	public void logout(String accessTokenRaw) {
		Claims claims = jwtService.parseAndValidateAccessToken(accessTokenRaw);
		long expMs = claims.getExpiration().getTime();
		long ttlMs = Math.max(0L, expMs - System.currentTimeMillis());
		if (ttlMs > 0) {
			redisTemplate.opsForValue().set(AuthRedisKeys.BLACKLIST_PREFIX + accessTokenRaw, "1", Duration.ofMillis(ttlMs));
		}
	}

	public UserDto validateAccessToken(String accessTokenRaw) {
		if (Boolean.TRUE.equals(redisTemplate.hasKey(AuthRedisKeys.BLACKLIST_PREFIX + accessTokenRaw))) {
			throw new InvalidTokenException("Token has been revoked");
		}
		Claims claims = jwtService.parseAndValidateAccessToken(accessTokenRaw);
		UUID userId = UUID.fromString(claims.get(JwtService.CLAIM_USER_ID, String.class));
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new InvalidTokenException("User no longer exists"));
		if (!user.isActive()) {
			throw new InvalidTokenException("User is inactive");
		}

		String companyIdStr = claims.get(JwtService.CLAIM_COMPANY_ID, String.class);
		UUID companyId = null;
		if (companyIdStr != null && !companyIdStr.isBlank()) {
			try {
				companyId = UUID.fromString(companyIdStr.trim());
			} catch (IllegalArgumentException e) {
				throw new InvalidTokenException("Invalid company id in token");
			}
		}

		CompanyType companyType = parseEnum(claims.get(JwtService.CLAIM_COMPANY_TYPE, String.class), CompanyType.class);
		CompanyRole companyRole = parseEnum(claims.get(JwtService.CLAIM_COMPANY_ROLE, String.class), CompanyRole.class);

		return UserDto.builder()
				.id(user.getId())
				.email(user.getEmail())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.companyId(companyId)
				.companyType(companyType)
				.companyRole(companyRole)
				.build();
	}

	private static <E extends Enum<E>> E parseEnum(String raw, Class<E> type) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return Enum.valueOf(type, raw.trim());
		} catch (IllegalArgumentException e) {
			throw new InvalidTokenException("Invalid token claims");
		}
	}

	private static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private AuthResponse issueTokens(AuthUserPrincipal principal) {
		String access = jwtService.generateAccessToken(principal);
		String refresh = jwtService.generateRefreshToken(principal);
		redisTemplate.opsForValue().set(
				AuthRedisKeys.REFRESH_PREFIX + principal.getUserId(),
				refresh,
				Duration.ofMillis(jwtService.getRefreshTokenExpirationMillis()));
		return AuthResponse.builder()
				.accessToken(access)
				.refreshToken(refresh)
				.tokenType("Bearer")
				.expiresIn(jwtService.getAccessTokenExpirationMillis() / 1000)
				.build();
	}
}
