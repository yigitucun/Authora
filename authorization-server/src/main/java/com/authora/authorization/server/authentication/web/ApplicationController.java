package com.authora.authorization.server.authentication.web;

import com.authora.authorization.server.audit.service.AuditLogService;
import com.authora.authorization.server.authentication.dto.CreateApplicationRequest;
import com.authora.authorization.server.client.mapper.RegisteredClientMapper;
import com.authora.authorization.server.client.model.RegisteredClientModel;
import com.authora.authorization.server.user.mapper.UserMapper;
import com.authora.authorization.server.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final RegisteredClientMapper registeredClientMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<?> getApplications(@AuthenticationPrincipal Jwt jwt) {
        User user = getCurrentUser(jwt);

        List<RegisteredClientModel> clients = registeredClientMapper
                .findByTenantId(user.getTenantId());

        return ResponseEntity.ok(clients.stream().map(client -> Map.of(
                "id", client.getId(),
                "clientName", client.getClientName(),
                "clientId", client.getClientId(),
                "createdAt", client.getClientIdIssuedAt().toString(),
                "redirectUris", client.getRedirectUris() != null ? client.getRedirectUris() : ""
        )).toList());
    }

    @PostMapping
    public ResponseEntity<?> createApplication(
            @RequestBody CreateApplicationRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {

        validateApplicationRequest(request);
        User user = getCurrentUser(jwt);

        String clientId = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        String clientSecret = UUID.randomUUID().toString();

        RegisteredClientModel client = RegisteredClientModel.builder()
                .id(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientIdIssuedAt(Instant.now())
                .clientSecret(passwordEncoder.encode(clientSecret))
                .clientName(request.clientName())
                .clientAuthenticationMethods("client_secret_basic")
                .authorizationGrantTypes("authorization_code,refresh_token")
                .redirectUris(request.redirectUri())
                .scopes("openid,profile")
                .clientSettings("{\"settings.client.require-proof-key\":false,\"settings.client.require-authorization-consent\":false}")
                .tokenSettings("{}")
                .tenantId(user.getTenantId())
                .build();

        registeredClientMapper.insert(client);

        auditLogService.log(user.getTenantId(), user.getId(), "CLIENT_CREATED", "APPLICATION", clientId,
                Map.of("clientName", request.clientName()),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"));

        return ResponseEntity.ok(Map.of(
                "id", client.getId(),
                "clientId", clientId,
                "clientSecret", clientSecret,
                "clientName", request.clientName(),
                "redirectUri", request.redirectUri()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateApplication(
            @PathVariable String id,
            @RequestBody CreateApplicationRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {

        validateApplicationRequest(request);
        User user = getCurrentUser(jwt);

        RegisteredClientModel client = getClientForTenant(id, user.getTenantId());
        client.setClientName(request.clientName());
        client.setRedirectUris(request.redirectUri());
        registeredClientMapper.update(client);

        auditLogService.log(user.getTenantId(), user.getId(), "CLIENT_UPDATED", "APPLICATION", client.getClientId(),
                Map.of("clientName", client.getClientName()),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"));

        return ResponseEntity.ok(Map.of(
                "id", client.getId(),
                "clientId", client.getClientId(),
                "clientName", client.getClientName(),
                "redirectUri", client.getRedirectUris() != null ? client.getRedirectUris() : ""
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {

        User user = getCurrentUser(jwt);
        RegisteredClientModel client = getClientForTenant(id, user.getTenantId());
        registeredClientMapper.delete(client.getId());

        auditLogService.log(user.getTenantId(), user.getId(), "CLIENT_DELETED", "APPLICATION", client.getClientId(),
                Map.of("clientName", client.getClientName()),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"));
        return ResponseEntity.noContent().build();
    }

    private void validateApplicationRequest(CreateApplicationRequest request) {
        if (request == null || request.clientName() == null || request.clientName().isBlank()
                || request.redirectUri() == null || request.redirectUri().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientName and redirectUri are required");
        }
        if (!isValidRedirectUri(request.redirectUri())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "redirectUri must be a valid http/https URL");
        }
    }

    private boolean isValidRedirectUri(String redirectUri) {
        try {
            URI uri = new URI(redirectUri.trim());
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private User getCurrentUser(Jwt jwt) {
        String email = jwt.getClaimAsString("email");

        return userMapper.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private RegisteredClientModel getClientForTenant(String id, UUID tenantId) {
        RegisteredClientModel client = registeredClientMapper.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        if (client.getTenantId() == null || !client.getTenantId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
        }

        return client;
    }
}