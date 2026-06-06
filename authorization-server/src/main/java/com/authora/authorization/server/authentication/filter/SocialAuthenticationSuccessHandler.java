package com.authora.authorization.server.authentication.filter;

import com.authora.authorization.server.audit.service.AuditLogService;
import com.authora.authorization.server.client.mapper.RegisteredClientMapper;
import com.authora.authorization.server.client.model.RegisteredClientModel;
import com.authora.authorization.server.user.mapper.UserMapper;
import com.authora.authorization.server.user.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class SocialAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;
    private final RegisteredClientMapper registeredClientMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public SocialAuthenticationSuccessHandler(
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository,
            RegisteredClientMapper registeredClientMapper,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService) {
        this.authorizationRequestRepository = authorizationRequestRepository;
        this.registeredClientMapper = registeredClientMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws ServletException, IOException {
        String clientId = resolveClientId(request, response);
        if (clientId != null && !clientId.isBlank() && !"authora-dashboard".equals(clientId)) {
            RegisteredClientModel client = registeredClientMapper.findByClientId(clientId).orElse(null);
            if (client == null) {
                getRedirectStrategy().sendRedirect(request, response, "/sign-in?error=oauth2");
                return;
            }
            String email = extractEmail(authentication);
            if (email == null || email.isBlank()) {
                getRedirectStrategy().sendRedirect(request, response, "/sign-in?error=oauth2");
                return;
            }
            if (!userMapper.existsByTenantIdAndEmail(client.getTenantId(), email)) {
                User user = new User();
                user.setId(UUID.randomUUID());
                user.setTenantId(client.getTenantId());
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setTenantAdmin(false);
                user.setVerified(true);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.insert(user);

                String provider = (authentication instanceof OAuth2AuthenticationToken token)
                        ? token.getAuthorizedClientRegistrationId()
                        : "oauth2";
                auditLogService.log(client.getTenantId(), user.getId(), "USER_SOCIAL_SIGNUP", "USER", user.getId().toString(),
                        Map.of("provider", provider, "clientId", clientId), request.getRemoteAddr(), request.getHeader("User-Agent"));
            }
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private String resolveClientId(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = authorizationRequestRepository.removeAuthorizationRequest(request, response);
        if (authorizationRequest != null) {
            Object value = authorizationRequest.getAdditionalParameters().get("client_id");
            if (value != null) {
                return value.toString();
            }
        }
        return request.getParameter("client_id");
    }

    private String extractEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            Object email = oauth2User.getAttributes().get("email");
            if (email != null) {
                return email.toString();
            }
            Object login = oauth2User.getAttributes().get("login");
            if (login != null) {
                return login + "@github.local";
            }
        }
        return null;
    }
}
