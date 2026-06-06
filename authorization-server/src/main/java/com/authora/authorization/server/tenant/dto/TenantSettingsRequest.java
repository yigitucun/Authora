package com.authora.authorization.server.tenant.dto;

public record TenantSettingsRequest(
        String name,
        String companyName,
        String usageType,
        String companySize
) {
}

