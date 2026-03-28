package com.biddex.company.dto;

import com.biddex.company.entity.CompanyRole;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class UserProfileDto {
	UUID id;
	UUID userId;
	UUID companyId;
	String position;
	String phone;
	CompanyRole companyRole;
}
