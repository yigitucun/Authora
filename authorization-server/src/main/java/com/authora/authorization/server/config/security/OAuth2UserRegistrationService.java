package com.authora.authorization.server.config.security;

import com.authora.authorization.server.client.mapper.RegisteredClientMapper;
import com.authora.authorization.server.client.model.RegisteredClientModel;
import com.authora.authorization.server.user.mapper.UserMapper;
import com.authora.authorization.server.user.model.User;
import com.authora.authorization.server.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserRegistrationService {

    private final UserMapper userMapper;
    private final RegisteredClientMapper registeredClientMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public void processOAuth2PostLogin(String email, String name, String provider, String clientId) {

        UUID tenantId = resolveTenantId(clientId);

        Optional<User> existingUser = userMapper.findByTenantIdAndEmail(tenantId, email);

        if (existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setId(UUID.randomUUID());
            newUser.setTenantId(tenantId);
            newUser.setEmail(email);

            newUser.setPassword(null);

            newUser.setVerified(true);
            newUser.setTenantAdmin(false);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setUpdatedAt(LocalDateTime.now());

            userMapper.insert(newUser);

            auditLogService.log(tenantId, newUser.getId(), "USER_REGISTERED_VIA_OAUTH2", "USER", newUser.getId().toString(), null, null, null);

        } else {
            User user = existingUser.get();
        }
    }

    private UUID resolveTenantId(String clientId) {
        if (clientId == null || clientId.isBlank() || clientId.equals("authora-dashboard")) {
            return null; // Ana dashboard tenant id'sini buraya koyarsın
        }

        return registeredClientMapper.findByClientId(clientId)
                .map(RegisteredClientModel::getTenantId)
                .orElseThrow(() -> new RuntimeException("Geçersiz Client ID: " + clientId));
    }
}