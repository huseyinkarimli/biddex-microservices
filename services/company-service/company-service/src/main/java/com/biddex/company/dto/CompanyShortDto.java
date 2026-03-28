package com.biddex.company.dto;

import com.biddex.company.entity.CompanyType;
import com.biddex.company.entity.Sector;
import com.biddex.company.entity.VerificationStatus;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class CompanyShortDto {
	UUID id;
	String name;
	Sector sector;
	CompanyType companyType;
	VerificationStatus verificationStatus;
	double sellerRating;
}
