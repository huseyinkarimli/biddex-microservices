package com.biddex.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

	private UUID id;
	private String email;
	private String firstName;
	private String lastName;
	private UUID companyId;
	private CompanyType companyType;
	private CompanyRole companyRole;
}
