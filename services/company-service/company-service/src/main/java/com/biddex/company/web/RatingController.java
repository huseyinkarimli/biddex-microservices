package com.biddex.company.web;

import com.biddex.company.dto.RatingDto;
import com.biddex.company.dto.RatingRequest;
import com.biddex.company.security.GatewaySecurity;
import com.biddex.company.security.GatewayUser;
import com.biddex.company.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class RatingController {

	private final RatingService ratingService;

	@PostMapping
	public ResponseEntity<RatingDto> submit(@Valid @RequestBody RatingRequest request) {
		GatewayUser user = GatewaySecurity.requireCurrentUser();
		return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.submitRating(request, user.companyId()));
	}

	@GetMapping("/company/{companyId}")
	public ResponseEntity<List<RatingDto>> forCompany(@PathVariable UUID companyId) {
		return ResponseEntity.ok(ratingService.getCompanyRatings(companyId));
	}
}
