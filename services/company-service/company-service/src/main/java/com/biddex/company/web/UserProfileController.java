package com.biddex.company.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.biddex.company.dto.UserProfileDto;
import com.biddex.company.dto.UserProfileUpdateRequest;
import com.biddex.company.entity.CompanyRole;
import com.biddex.company.security.GatewaySecurity;
import com.biddex.company.security.GatewayUser;
import com.biddex.company.service.UserProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class UserProfileController {

	private final UserProfileService userProfileService;

	@GetMapping("/me")
	public ResponseEntity<UserProfileDto> me() {
		GatewayUser user = GatewaySecurity.requireCurrentUser();
		return ResponseEntity.ok(userProfileService.getProfile(user.userId(), user.companyId()));
	}

	@PutMapping("/me")
	public ResponseEntity<UserProfileDto> updateMe(@Valid @RequestBody UserProfileUpdateRequest request) {
		GatewayUser user = GatewaySecurity.requireCurrentUser();
		return ResponseEntity.ok(
				userProfileService.createOrUpdateProfile(user.userId(), user.companyId(), user.companyRole(), request)
		);
	}

	@GetMapping("/company/{companyId}/members")
	public ResponseEntity<List<UserProfileDto>> members(@PathVariable UUID companyId) {
		return ResponseEntity.ok(userProfileService.getCompanyMembers(companyId));
	}

	@PutMapping("/company/{companyId}/members/{userId}/role")
	public ResponseEntity<UserProfileDto> updateRole(
			@PathVariable UUID companyId,
			@PathVariable UUID userId,
			@RequestBody @Valid RoleUpdateBody body
	) {
		GatewayUser actor = GatewaySecurity.requireCurrentUser();
		return ResponseEntity.ok(
				userProfileService.updateMemberRole(actor.userId(), companyId, userId, body.newRole())
		);
	}

	public record RoleUpdateBody(@NotNull @JsonProperty("newRole") CompanyRole newRole) {
	}
}
