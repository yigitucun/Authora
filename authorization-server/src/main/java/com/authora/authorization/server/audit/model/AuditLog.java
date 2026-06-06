package com.authora.authorization.server.audit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {
    private UUID id;
    private UUID tenantId;
    private UUID actorUserId;
    private String action;
    private String targetType;
    private String targetId;
    private String metadata;
    private String ip;
    private String userAgent;
    private LocalDateTime createdAt;
}

