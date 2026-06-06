package com.authora.authorization.server.config.security;

import com.authora.authorization.server.client.mapper.RegisteredClientMapper;
import com.authora.authorization.server.client.model.RegisteredClientModel;
import com.authora.authorization.server.connection.mapper.AppConnectionMapper;
import com.authora.authorization.server.connection.model.AppConnection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component // SİSTEMİ UYANDIRAN SİHİRLİ KELİME
@RequiredArgsConstructor
public class DynamicClientRegistrationRepository implements ClientRegistrationRepository {

    private final RegisteredClientMapper registeredClientMapper;
    private final AppConnectionMapper appConnectionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        String provider = registrationId;
        String oidcClientId = resolveOidcClientIdFromRequest();

        if (registrationId.contains("__")) {
            String[] parts = registrationId.split("__");
            provider = parts[0];
            oidcClientId = parts[1];
        }

        if (oidcClientId != null && !oidcClientId.isBlank()) {
            ClientRegistration dynamic = buildDynamicRegistration(registrationId, provider, oidcClientId);
            if (dynamic != null) {
                log.debug("Using per-tenant credentials for provider '{}', oidcClientId='{}'", provider, oidcClientId);
                return dynamic;
            }
        }

        log.warn("DB'de sağlayıcı bulunamadı, işlem iptal edildi: {}", provider);
        return null; // Artık yaml dosyasına düşmek yok!
    }

    private String resolveOidcClientIdFromRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;

            HttpServletRequest request = attrs.getRequest();
            String param = request.getParameter("client_id");
            if (param != null && !param.isBlank()) {
                return param;
            }

            Object sessionValue = request.getSession(false) != null
                    ? request.getSession().getAttribute("oauth2_client_id")
                    : null;
            if (sessionValue != null) {
                return sessionValue.toString();
            }
            return null;
        } catch (Exception e) {
            log.warn("Could not resolve oidcClientId: {}", e.getMessage());
            return null;
        }
    }

    private ClientRegistration buildDynamicRegistration(String originalRegistrationId, String provider, String oidcClientId) {
        try {
            RegisteredClientModel oidcClient = registeredClientMapper.findByClientId(oidcClientId).orElse(null);
            if (oidcClient == null || oidcClient.getTenantId() == null) return null;

            AppConnection connection = findEnabledConnectionForTenant(oidcClient.getTenantId(), provider);
            if (connection == null) return null;

            Map<String, String> settings = parseSettings(connection.getSettings());
            if (settings == null) return null;

            String socialClientId = settings.get("clientId");
            String socialClientSecret = settings.get("clientSecret");
            String redirectUri = settings.get("redirectUri");

            if (socialClientId == null || socialClientId.isBlank() || socialClientSecret == null || socialClientSecret.isBlank()) {
                return null;
            }

            ClientRegistration.Builder builder;
            String lowerProvider = provider.toLowerCase();

            if ("google".equals(lowerProvider)) {
                builder = CommonOAuth2Provider.GOOGLE.getBuilder(originalRegistrationId);
            } else if ("github".equals(lowerProvider)) {
                builder = CommonOAuth2Provider.GITHUB.getBuilder(originalRegistrationId);
            } else if ("facebook".equals(lowerProvider)) {
                builder = CommonOAuth2Provider.FACEBOOK.getBuilder(originalRegistrationId);
            } else {
                return null;
            }

            builder.clientId(socialClientId).clientSecret(socialClientSecret);


            return builder.build();

        } catch (Exception e) {
            log.warn("Dinamik ClientRegistration patladı: {}", e.getMessage());
            return null;
        }
    }

    private AppConnection findEnabledConnectionForTenant(UUID tenantId, String registrationId) {
        String providerName = capitalize(registrationId);
        for (RegisteredClientModel client : registeredClientMapper.findByTenantId(tenantId)) {
            AppConnection conn = appConnectionMapper
                    .findEnabledByClientIdAndProviderName(client.getClientId(), providerName)
                    .orElse(null);
            if (conn != null) {
                return conn;
            }
        }
        return null;
    }

    private Map<String, String> parseSettings(String settingsJson) {
        if (settingsJson == null || settingsJson.isBlank() || settingsJson.equals("{}")) return null;
        try {
            return objectMapper.readValue(settingsJson, new TypeReference<>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}