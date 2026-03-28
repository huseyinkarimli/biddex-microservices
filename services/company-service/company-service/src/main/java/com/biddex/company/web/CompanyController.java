package com.biddex.company.web;

import com.biddex.company.dto.CompanyDto;
import com.biddex.company.dto.CompanyShortDto;
import com.biddex.company.dto.CompanyUpdateRequest;
import com.biddex.company.dto.RejectCompanyRequest;
import com.biddex.company.entity.Sector;
import com.biddex.company.security.GatewaySecurity;
import com.biddex.company.security.GatewayUser;
import com.biddex.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

	private final CompanyService companyService;

	@GetMapping("/pending")
	public ResponseEntity<List<CompanyDto>> pending() {
		return ResponseEntity.ok(companyService.getPendingCompanies());
	}

	@GetMapping("/sector/{sector}")
	public ResponseEntity<List<CompanyShortDto>> bySector(@PathVariable Sector sector) {
		return ResponseEntity.ok(companyService.getCompaniesBySector(sector));
	}

	@GetMapping("/voen/{voen}")
	public ResponseEntity<CompanyDto> byVoen(@PathVariable String voen) {
		return ResponseEntity.ok(companyService.getCompanyByVoen(voen));
	}

	@GetMapping("/{id}/exists")
	public ResponseEntity<Boolean> exists(@PathVariable UUID id) {
		return ResponseEntity.ok(companyService.existsById(id));
	}

	@GetMapping("/{id}")
	public ResponseEntity<CompanyDto> getById(@PathVariable UUID id) {
		return ResponseEntity.ok(companyService.getCompanyById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<CompanyDto> update(
			@PathVariable UUID id,
			@Valid @RequestBody CompanyUpdateRequest request
	) {
		GatewayUser user = GatewaySecurity.requireCurrentUser();
		return ResponseEntity.ok(companyService.updateCompany(id, request, user.userId()));
	}

	@PutMapping("/{id}/verify")
	public ResponseEntity<Void> verify(
			@PathVariable UUID id,
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
	) {
		companyService.verifyCompany(id, authorization);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/reject")
	public ResponseEntity<Void> reject(
			@PathVariable UUID id,
			@Valid @RequestBody RejectCompanyRequest body,
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
	) {
		companyService.rejectCompany(id, body.getReason(), authorization);
		return ResponseEntity.noContent().build();
	}
}
