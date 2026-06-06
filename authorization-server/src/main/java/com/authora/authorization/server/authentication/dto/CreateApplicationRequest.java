package com.authora.authorization.server.authentication.dto;

public record CreateApplicationRequest(
        String clientName,
        String redirectUri
) {}