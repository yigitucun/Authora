package com.authora.authorization.server.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String callbackUrl,
        String clientSecret,
        UUID masterTenantId,
        String publicBaseUrl,
        Long emailVerificationTtlMinutes
) {
}
