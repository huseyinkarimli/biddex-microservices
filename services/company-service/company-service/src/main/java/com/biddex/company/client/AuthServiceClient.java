package com.biddex.company.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", contextId = "authServiceClient")
public interface AuthServiceClient {

	@GetMapping("/auth/validate")
	AuthUserValidationDto validate(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);
}
