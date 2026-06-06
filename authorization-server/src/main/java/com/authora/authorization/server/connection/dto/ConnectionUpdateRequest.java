package com.authora.authorization.server.connection.dto;

import java.util.Map;

public record ConnectionUpdateRequest(
        boolean enabled,
        Map<String, Object> settings
) {
}

