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
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

public class UserAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final RegisteredClientMapper registeredClientMapper;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;
    private final RequestCache requestCache = new HttpSessionRequestCache();

    public UserAuthenticationSuccessHandler(RegisteredClientMapper registeredClientMapper,
                                            UserMapper userMapper,
                                            AuditLogService auditLogService) {
        this.registeredClientMapper = registeredClientMapper;
        this.userMapper = userMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws ServletException, IOException {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        String clientId = request.getParameter("clientId");
        if (savedRequest != null) {
            logLoginSuccess(request, clientId);
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }
        if (clientId != null && !clientId.isBlank() && !"authora-dashboard".equals(clientId)) {
            RegisteredClientModel client = registeredClientMapper.findByClientId(clientId).orElse(null);
            String redirectUri = firstRedirectUri(client != null ? client.getRedirectUris() : null);
            if (redirectUri != null) {
                logLoginSuccess(request, clientId);
                String authorizeUrl = UriComponentsBuilder.fromPath("/oauth2/authorize")
                        .queryParam("response_type", "code")
                        .queryParam("client_id", clientId)
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("scope", "openid profile")
                        .toUriString();
                getRedirectStrategy().sendRedirect(request, response, authorizeUrl);
                return;
            }
        }

        logLoginSuccess(request, clientId);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private String firstRedirectUri(String redirectUris) {
        if (redirectUris == null || redirectUris.isBlank()) {
            return null;
        }
        String[] parts = redirectUris.split(",");
        return parts.length > 0 ? parts[0].trim() : null;
    }

    private void logLoginSuccess(HttpServletRequest request, String clientId) {
        String email = request.getParameter("email");
        if (email == null || email.isBlank()) {
            return;
        }
        User user = userMapper.findByEmail(email).orElse(null);
        if (user == null) {
            return;
        }
        auditLogService.log(user.getTenantId(), user.getId(), "LOGIN_SUCCESS", "USER", user.getId().toString(),
                java.util.Map.of("clientId", clientId != null ? clientId : ""),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));
    }
}

