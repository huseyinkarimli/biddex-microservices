package com.biddex.company.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {

	@Size(max = 160)
	private String position;

	@Size(max = 64)
	private String phone;
}
