package com.biddex.company.client;

import java.util.UUID;

/**
 * Mirrors auth-service {@code UserDto} JSON for Feign deserialization.
 */
public record AuthUserValidationDto(
		UUID id,
		String email,
		String firstName,
		String lastName,
		UUID companyId,
		String companyType,
		String companyRole
) {
}
