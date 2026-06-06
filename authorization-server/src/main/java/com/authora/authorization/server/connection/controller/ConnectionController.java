
package com.authora.authorization.server.connection.controller;

import com.authora.authorization.server.audit.service.AuditLogService;
import com.authora.authorization.server.client.mapper.RegisteredClientMapper;
import com.authora.authorization.server.client.model.RegisteredClientModel;
import com.authora.authorization.server.connection.dto.ConnectionUpdateRequest;
import com.authora.authorization.server.connection.mapper.AppConnectionMapper;
import com.authora.authorization.server.connection.mapper.ConnectionTypeMapper;
import com.authora.authorization.server.connection.model.AppConnection;
import com.authora.authorization.server.connection.model.ConnectionType;
import com.authora.authorization.server.user.mapper.UserMapper;
import com.authora.authorization.server.user.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/connections")
@RequiredArgsConstructor
public class ConnectionController {
    private final ConnectionTypeMapper connectionTypeMapper;
    private final AppConnectionMapper appConnectionMapper;
    private final RegisteredClientMapper registeredClientMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    @GetMapping("/types")
    public ResponseEntity<?> listConnectionTypes(@AuthenticationPrincipal Jwt jwt,
                                                @RequestParam("clientId") String clientId,
                                                @RequestParam(value = "social", required = false) Boolean social) {
        User user = getCurrentUser(jwt);
        RegisteredClientModel client = getClientForTenant(clientId, user.getTenantId());

        List<ConnectionType> types = connectionTypeMapper.findAllActive();
        if (social != null) {
            types = types.stream().filter(type -> type.isSocial() == social).toList();
        }

        Map<UUID, AppConnection> byType = appConnectionMapper.findByClientId(client.getClientId()).stream()
                .collect(Collectors.toMap(AppConnection::getConnectionTypeId, connection -> connection));

        List<Map<String, Object>> response = new ArrayList<>();
        for (ConnectionType type : types) {
            AppConnection connection = byType.get(type.getId());
            response.add(Map.of(
                    "id", type.getId().toString(),
                    "name", type.getName(),
                    "description", type.getDescription() != null ? type.getDescription() : "",
                    "isSocial", type.isSocial(),
                    "requiredFields", type.getRequiredFields() != null ? type.getRequiredFields() : "[]",
                    "settingsSchema", type.getSettingsSchema() != null ? type.getSettingsSchema() : "[]",
                    "enabled", connection != null && connection.isEnabled(),
                    "settings", connection != null && connection.getSettings() != null ? connection.getSettings() : "{}"
            ));
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/types/{typeId}")
    public ResponseEntity<?> updateConnection(@AuthenticationPrincipal Jwt jwt,
                                             @PathVariable UUID typeId,
                                             @RequestParam("clientId") String clientId,
                                             @RequestBody ConnectionUpdateRequest request,
                                             HttpServletRequest httpRequest) {
        User user = getCurrentUser(jwt);
        RegisteredClientModel client = getClientForTenant(clientId, user.getTenantId());

        ConnectionType type = connectionTypeMapper.findById(typeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection type not found"));

        AppConnection connection = appConnectionMapper.findByClientIdAndConnectionTypeId(client.getClientId(), type.getId())
                .orElse(null);

        String settingsJson = toJson(request.settings());
        LocalDateTime now = LocalDateTime.now();
        if (connection == null) {
            connection = new AppConnection();
            connection.setId(UUID.randomUUID());
            connection.setClientId(client.getClientId());
            connection.setConnectionTypeId(type.getId());
            connection.setEnabled(request.enabled());
            connection.setSettings(settingsJson);
            connection.setFormConfig("{}");
            connection.setCreatedAt(now);
            connection.setUpdatedAt(now);
            appConnectionMapper.insert(connection);
        } else {
            connection.setEnabled(request.enabled());
            connection.setSettings(settingsJson);
            connection.setUpdatedAt(now);
            appConnectionMapper.update(connection);
        }

        auditLogService.log(user.getTenantId(), user.getId(), "CONNECTION_UPDATED", "CONNECTION", type.getName(),
                Map.of("enabled", request.enabled(), "clientId", client.getClientId()),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"));

        return ResponseEntity.ok(Map.of(
                "id", type.getId().toString(),
                "enabled", connection.isEnabled()
        ));
    }

    private String toJson(Map<String, Object> settings) {
        if (settings == null || settings.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private User getCurrentUser(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return userMapper.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private RegisteredClientModel getClientForTenant(String clientId, UUID tenantId) {
        if (clientId == null || clientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId is required");
        }
        RegisteredClientModel client = registeredClientMapper.findByClientId(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        if (client.getTenantId() == null || !client.getTenantId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
        }
        return client;
    }
}
