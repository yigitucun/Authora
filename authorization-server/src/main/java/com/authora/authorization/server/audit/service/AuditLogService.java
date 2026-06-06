package com.authora.authorization.server.audit.service;

import com.authora.authorization.server.audit.mapper.AuditLogMapper;
import com.authora.authorization.server.audit.model.AuditLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    public void log(UUID tenantId,
                    UUID actorUserId,
                    String action,
                    String targetType,
                    String targetId,
                    Map<String, Object> metadata,
                    String ip,
                    String userAgent) {
        if (tenantId == null || action == null || action.isBlank()) {
            return;
        }

        AuditLog log = new AuditLog();
        log.setId(UUID.randomUUID());
        log.setTenantId(tenantId);
        log.setActorUserId(actorUserId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setMetadata(toJson(metadata));
        log.setIp(ip);
        log.setUserAgent(userAgent);
        log.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(log);
    }

    private String toJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}

