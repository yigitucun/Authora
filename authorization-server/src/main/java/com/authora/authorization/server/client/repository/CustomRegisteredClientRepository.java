package com.authora.authorization.server.client.repository;

import com.authora.authorization.server.client.mapper.RegisteredClientMapper;
import com.authora.authorization.server.client.model.RegisteredClientModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Primary
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    private final RegisteredClientMapper registeredClientMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void save(RegisteredClient registeredClient) {
        RegisteredClientModel model = toModel(registeredClient);
        if (registeredClientMapper.findById(registeredClient.getId()).isPresent()) {
            registeredClientMapper.update(model);
        } else {
            registeredClientMapper.insert(model);
        }
    }

    @Override
    public RegisteredClient findById(String id) {
        return registeredClientMapper.findById(id)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return registeredClientMapper.findByClientId(clientId)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    private RegisteredClient toRegisteredClient(RegisteredClientModel model) {
        RegisteredClient.Builder builder = RegisteredClient
                .withId(model.getId())
                .clientId(model.getClientId())
                .clientIdIssuedAt(model.getClientIdIssuedAt())
                .clientSecret(model.getClientSecret())
                .clientName(model.getClientName());

        // Null ve Boşluk Korumalı Split İşlemleri
        if (StringUtils.hasText(model.getClientAuthenticationMethods())) {
            Arrays.stream(model.getClientAuthenticationMethods().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText) // Boşluk stringlerini atlar
                    .map(ClientAuthenticationMethod::new)
                    .forEach(builder::clientAuthenticationMethod);
        }

        if (StringUtils.hasText(model.getAuthorizationGrantTypes())) {
            Arrays.stream(model.getAuthorizationGrantTypes().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(AuthorizationGrantType::new)
                    .forEach(builder::authorizationGrantType);
        }

        if (StringUtils.hasText(model.getRedirectUris())) {
            Arrays.stream(model.getRedirectUris().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .forEach(builder::redirectUri);
        }

        if (StringUtils.hasText(model.getPostLogoutRedirectUris())) {
            Arrays.stream(model.getPostLogoutRedirectUris().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .forEach(builder::postLogoutRedirectUri);
        }

        if (StringUtils.hasText(model.getScopes())) {
            Arrays.stream(model.getScopes().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .forEach(builder::scope);
        }

        // ClientSettings
        try {
            if (StringUtils.hasText(model.getClientSettings())) {
                Map<String, Object> clientSettingsMap = objectMapper.readValue(
                        model.getClientSettings(),
                        new TypeReference<>() {}
                );
                builder.clientSettings(ClientSettings.withSettings(clientSettingsMap).build());
            }
        } catch (Exception e) {
            builder.clientSettings(ClientSettings.builder().build());
        }

        // TokenSettings
        try {
            if (StringUtils.hasText(model.getTokenSettings())) {
                Map<String, Object> rawTokenSettings = objectMapper.readValue(
                        model.getTokenSettings(),
                        new TypeReference<>() {}
                );

                // Jackson'ın döndüğü Map üzerinde işlem yapabilmek için (UnsupportedOperationException yememek için) sarmalıyoruz
                Map<String, Object> tokenSettingsMap = new HashMap<>(rawTokenSettings);

                tokenSettingsMap.replaceAll((key, value) -> {
                    // 1. Süre (Duration) kontrolleri
                    if (key.contains("time-to-live")) {
                        if (value instanceof String s) {
                            try { return Duration.parse(s); } catch (Exception e) { return value; }
                        }
                        if (value instanceof Number n) {
                            return Duration.ofSeconds(n.longValue());
                        }
                    }

                    // 2. Token Format kontrolü
                    if (key.equals("settings.token.access-token-format")) {
                        if (value instanceof Map<?, ?> map) {
                            Object val = map.get("value");
                            if (val != null) {
                                return new OAuth2TokenFormat(val.toString());
                            }
                        } else if (value instanceof String s) {
                            return new OAuth2TokenFormat(s);
                        }
                    }

                    // 3. İŞTE HAYAT KURTARAN YENİ KONTROL: Signature Algorithm
                    if (key.equals("settings.token.id-token-signature-algorithm")) {
                        if (value instanceof String s) {
                            return SignatureAlgorithm.from(s);
                        }
                    }

                    return value;
                });

                builder.tokenSettings(TokenSettings.withSettings(tokenSettingsMap).build());
            }
        } catch (Exception e) {
            builder.tokenSettings(TokenSettings.builder().build());
        }

        return builder.build();
    }

    private RegisteredClientModel toModel(RegisteredClient client) {
        try {
            String clientAuthMethods = client.getClientAuthenticationMethods().stream()
                    .map(ClientAuthenticationMethod::getValue)
                    .reduce((a, b) -> a + "," + b)
                    .orElse(null);

            String grantTypes = client.getAuthorizationGrantTypes().stream()
                    .map(AuthorizationGrantType::getValue)
                    .reduce((a, b) -> a + "," + b)
                    .orElse(null);

            String redirectUris = client.getRedirectUris().isEmpty() ? null :
                    String.join(",", client.getRedirectUris());

            String postLogoutUris = client.getPostLogoutRedirectUris().isEmpty() ? null :
                    String.join(",", client.getPostLogoutRedirectUris());

            String scopes = client.getScopes().isEmpty() ? null :
                    String.join(",", client.getScopes());

            String clientSettings = objectMapper.writeValueAsString(
                    client.getClientSettings().getSettings()
            );

            Map<String, Object> tokenSettingsMap = new HashMap<>(client.getTokenSettings().getSettings());
            tokenSettingsMap.replaceAll((key, value) -> {
                if (value instanceof Duration d) {
                    return d.toString();
                }
                return value;
            });
            String tokenSettings = objectMapper.writeValueAsString(tokenSettingsMap);

            return RegisteredClientModel.builder()
                    .id(client.getId())
                    .clientId(client.getClientId())
                    .clientIdIssuedAt(client.getClientIdIssuedAt())
                    .clientSecret(client.getClientSecret())
                    .clientSecretExpiresAt(client.getClientSecretExpiresAt())
                    .clientName(client.getClientName())
                    .clientAuthenticationMethods(clientAuthMethods)
                    .authorizationGrantTypes(grantTypes)
                    .redirectUris(redirectUris)
                    .postLogoutRedirectUris(postLogoutUris)
                    .scopes(scopes)
                    .clientSettings(clientSettings)
                    .tokenSettings(tokenSettings)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("RegisteredClient serialize hatası", e);
        }
    }
}