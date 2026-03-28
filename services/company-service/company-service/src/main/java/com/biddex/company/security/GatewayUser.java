package com.biddex.company.security;

import com.biddex.company.entity.CompanyRole;
import com.biddex.company.entity.CompanyType;

import java.util.UUID;

public record GatewayUser(
		UUID userId,
		String email,
		UUID companyId,
		CompanyType companyType,
		CompanyRole companyRole
) {
}
