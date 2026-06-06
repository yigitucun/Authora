package com.authora.authorization.server.tenant.service;

import com.authora.authorization.server.audit.service.AuditLogService;
import com.authora.authorization.server.tenant.mapper.TenantMapper;
import com.authora.authorization.server.tenant.model.Tenant;
import com.authora.authorization.server.user.mapper.UserMapper;
import com.authora.authorization.server.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantMapper tenantMapper;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    public void completeOnboarding(String email, String usageType, String companyName, String companySize) {
        User user = userMapper.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Tenant tenant = tenantMapper.findById(user.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        tenant.setUsageType(usageType);
        tenant.setCompanyName(companyName);
        tenant.setCompanySize(companySize);
        tenant.setUpdatedAt(LocalDateTime.now());

        tenantMapper.updateOnboarding(tenant);
        auditLogService.log(tenant.getId(), user.getId(), "TENANT_ONBOARDING_COMPLETED", "TENANT", tenant.getId().toString(),
                null, null, null);
    }
}
