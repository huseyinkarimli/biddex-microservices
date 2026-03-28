package com.biddex.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

	@NotBlank
	@Email
	@Size(max = 320)
	private String email;

	@NotBlank
	@Size(min = 8, max = 128)
	private String password;

	@NotBlank
	@Size(max = 120)
	private String firstName;

	@NotBlank
	@Size(max = 120)
	private String lastName;

	@NotBlank
	@Size(max = 255)
	private String companyName;

	@NotBlank
	@Pattern(regexp = "^[0-9]{10}$", message = "VÖEN must be exactly 10 digits")
	private String voen;

	@NotNull
	private Sector sector;

	@NotNull
	private CompanyType companyType;
}
