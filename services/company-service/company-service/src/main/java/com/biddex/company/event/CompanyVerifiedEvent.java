package com.biddex.company.event;

import com.biddex.company.entity.CompanyType;
import com.biddex.company.entity.Sector;

import java.util.UUID;

public record CompanyVerifiedEvent(
		UUID companyId,
		String companyName,
		Sector sector,
		CompanyType companyType
) {
}
