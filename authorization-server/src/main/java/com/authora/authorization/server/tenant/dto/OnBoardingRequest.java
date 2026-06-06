package com.authora.authorization.server.tenant.dto;

public record OnBoardingRequest(
        String usageType,
        String companyName,
        String companySize
) {
}
