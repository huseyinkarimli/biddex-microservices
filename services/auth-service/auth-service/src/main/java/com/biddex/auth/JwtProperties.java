package com.biddex.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

	/**
	 * HMAC key: Base64-encoded 32 bytes (256-bit) or raw secret at least 32 bytes UTF-8.
	 */
	private String secret;

	private long accessTokenExpiration = 900_000L;

	private long refreshTokenExpiration = 604_800_000L;
}
