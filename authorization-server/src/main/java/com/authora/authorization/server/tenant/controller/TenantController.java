package com.authora.authorization.server.tenant.controller;

import com.authora.authorization.server.tenant.dto.OnBoardingRequest;
import com.authora.authorization.server.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping("/onboarding")
    public ResponseEntity<?> completeOnboarding(
            @RequestBody OnBoardingRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        tenantService.completeOnboarding(email, request.usageType(), request.companyName(), request.companySize());
        return ResponseEntity.ok().build();
    }


}