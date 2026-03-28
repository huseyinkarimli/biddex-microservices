package com.biddex.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectCompanyRequest {

	@NotBlank
	@Size(max = 2000)
	private String reason;
}
