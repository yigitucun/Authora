package com.authora.authorization.server.tenant.controller;

import com.authora.authorization.server.audit.service.AuditLogService;
import com.authora.authorization.server.tenant.dto.TenantSettingsRequest;
import com.authora.authorization.server.tenant.mapper.TenantMapper;
import com.authora.authorization.server.tenant.model.Tenant;
import com.authora.authorization.server.user.mapper.UserMapper;
import com.authora.authorization.server.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/v1/tenant")
@RequiredArgsConstructor
public class TenantSettingsController {

    private static final Set<String> ALLOWED_USAGE_TYPES = Set.of("COMPANY", "INDIVIDUAL");
    private static final Set<String> ALLOWED_COMPANY_SIZES = Set.of("1-10", "11-50", "51-100", "101-500", "500+");

    private final TenantMapper tenantMapper;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<?> getSettings(@AuthenticationPrincipal Jwt jwt) {
        Tenant tenant = getTenant(jwt);
        return ResponseEntity.ok(Map.of(
                "id", tenant.getId().toString(),
                "name", tenant.getName(),
                "companyName", tenant.getCompanyName() != null ? tenant.getCompanyName() : "",
                "usageType", tenant.getUsageType() != null ? tenant.getUsageType() : "",
                "companySize", tenant.getCompanySize() != null ? tenant.getCompanySize() : "",
                "onboardingCompleted", tenant.isOnboardingCompleted()
        ));
    }

    @PutMapping
    public ResponseEntity<?> updateSettings(
            @RequestBody TenantSettingsRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {

        validateRequest(request);
        Tenant tenant = getTenant(jwt);
        tenant.setName(request.name().trim());
        tenant.setCompanyName(normalize(request.companyName()));
        tenant.setUsageType(normalize(request.usageType()));
        tenant.setCompanySize(normalize(request.companySize()));
        tenant.setUpdatedAt(LocalDateTime.now());

        tenantMapper.updateSettings(tenant);
        auditLogService.log(tenant.getId(), userMapper.findByEmail(jwt.getClaimAsString("email")).map(User::getId).orElse(null),
                "TENANT_SETTINGS_UPDATED", "TENANT", tenant.getId().toString(),
                Map.of("companyName", tenant.getCompanyName() != null ? tenant.getCompanyName() : ""),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    private void validateRequest(TenantSettingsRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workspace name is required");
        }

        String usageType = normalize(request.usageType());
        if (usageType != null && !ALLOWED_USAGE_TYPES.contains(usageType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid usageType");
        }

        String companySize = normalize(request.companySize());
        if (companySize != null && !ALLOWED_COMPANY_SIZES.contains(companySize)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid companySize");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Tenant getTenant(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        User user = userMapper.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return tenantMapper.findById(user.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
    }
}
