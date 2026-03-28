package com.biddex.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
		String refreshToken = resolveBearerToken(authorization);
		return ResponseEntity.ok(authService.refreshToken(refreshToken));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
		String accessToken = resolveBearerToken(authorization);
		authService.logout(accessToken);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/validate")
	public ResponseEntity<UserDto> validate(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
		String accessToken = resolveBearerToken(authorization);
		return ResponseEntity.ok(authService.validateAccessToken(accessToken));
	}

	private static String resolveBearerToken(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			throw new InvalidTokenException("Missing or invalid Authorization header");
		}
		String token = authorizationHeader.substring(7).trim();
		if (token.isEmpty()) {
			throw new InvalidTokenException("Missing or invalid Authorization header");
		}
		return token;
	}
}
