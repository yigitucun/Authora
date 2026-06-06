package com.authora.authorization.server.authentication.service;

import com.authora.authorization.server.authentication.dto.SignUpRequest;
import com.authora.authorization.server.client.mapper.RegisteredClientMapper;
import com.authora.authorization.server.client.model.RegisteredClientModel;
import com.authora.authorization.server.audit.service.AuditLogService;
import com.authora.authorization.server.tenant.mapper.TenantMapper;
import com.authora.authorization.server.tenant.model.Tenant;
import com.authora.authorization.server.user.mapper.UserMapper;
import com.authora.authorization.server.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final UserMapper userMapper;
    private final RegisteredClientMapper registeredClientMapper;
    private final PasswordEncoder passwordEncoder;
    private final TenantMapper tenantMapper;
    private final EmailVerificationService emailVerificationService;
    private final AuditLogService auditLogService;


    @Transactional
    public void signup(SignUpRequest request){
        if (userMapper.existsByEmail(request.getEmail())) {
            throw new RuntimeException("This email is already registered");
        }
        if (request.getClientId() == null || request.getClientId().isBlank() || request.getClientId().equals("authora-dashboard")){
            Tenant newTenant = createNewTenantForCompany(request.getEmail());

            User user = signUpUser(newTenant.getId(), request, true);
            emailVerificationService.sendVerificationEmail(user, request.getClientId());
            auditLogService.log(newTenant.getId(), user.getId(), "USER_SIGNUP", "USER", user.getId().toString(), null, null, null);

        } else {
            RegisteredClientModel clientModel = registeredClientMapper.findByClientId(request.getClientId())
                    .orElseThrow(()->new RuntimeException("Invalid Client ID"));

            User user = signUpUser(clientModel.getTenantId(), request, false);
            emailVerificationService.sendVerificationEmail(user, request.getClientId());
            auditLogService.log(clientModel.getTenantId(), user.getId(), "USER_SIGNUP", "USER", user.getId().toString(), null, null, null);
        }
    }

    private Tenant createNewTenantForCompany(String email) {
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID());

        String prefix = email.contains("@") ? email.split("@")[0] : "New";
        tenant.setName(prefix + "'s Workspace");

        tenant.setActive(true);
        tenant.setOnboardingCompleted(false);
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());

        tenantMapper.insert(tenant);
        return tenant;
    }

    private User signUpUser(UUID tenantId, SignUpRequest request, boolean isTenantAdmin){
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setTenantId(tenantId);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTenantAdmin(isTenantAdmin);
        user.setVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }
}