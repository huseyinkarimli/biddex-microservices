package com.biddex.company.dto;

import com.biddex.company.entity.CompanyType;
import com.biddex.company.entity.Sector;
import com.biddex.company.entity.VerificationStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class CompanyDto {
	UUID id;
	String name;
	String voen;
	Sector sector;
	CompanyType companyType;
	VerificationStatus verificationStatus;
	String address;
	String phone;
	String email;
	String website;
	double buyerRating;
	double sellerRating;
	int ratingCount;
	boolean isActive;
	String rejectionReason;
	Instant createdAt;
	Instant updatedAt;
}
