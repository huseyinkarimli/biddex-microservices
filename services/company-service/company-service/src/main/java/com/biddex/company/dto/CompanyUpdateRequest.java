package com.biddex.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyUpdateRequest {

	@Size(max = 500)
	private String address;

	@Size(max = 64)
	private String phone;

	@Email
	@Size(max = 320)
	private String email;

	@Size(max = 512)
	private String website;
}
