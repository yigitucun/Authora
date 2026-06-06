package com.authora.authorization.server.audit.controller;

import com.authora.authorization.server.audit.mapper.AuditLogMapper;
import com.authora.authorization.server.audit.model.AuditLog;
import com.authora.authorization.server.user.mapper.UserMapper;
import com.authora.authorization.server.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditLogMapper auditLogMapper;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal Jwt jwt,
                                  @RequestParam(value = "limit", required = false, defaultValue = "50") int limit) {
        User user = getCurrentUser(jwt);
        int safeLimit = Math.min(Math.max(limit, 1), 200);

        List<AuditLog> logs = auditLogMapper.findRecentByTenantId(user.getTenantId(), safeLimit);
        Map<UUID, String> userEmails = userMapper.findAllByTenantId(user.getTenantId()).stream()
                .collect(Collectors.toMap(User::getId, User::getEmail));

        return ResponseEntity.ok(logs.stream().map(log -> Map.of(
                "id", log.getId().toString(),
                "action", log.getAction(),
                "targetType", log.getTargetType() != null ? log.getTargetType() : "",
                "targetId", log.getTargetId() != null ? log.getTargetId() : "",
                "actorEmail", log.getActorUserId() != null ? userEmails.getOrDefault(log.getActorUserId(), "") : "",
                "ip", log.getIp() != null ? log.getIp() : "",
                "userAgent", log.getUserAgent() != null ? log.getUserAgent() : "",
                "createdAt", log.getCreatedAt().toString()
        )).toList());
    }

    private User getCurrentUser(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return userMapper.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}

