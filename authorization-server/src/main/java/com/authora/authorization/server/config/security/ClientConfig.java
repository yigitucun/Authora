package com.authora.authorization.server.config.security;

import com.authora.authorization.server.client.repository.CustomRegisteredClientRepository;
import com.authora.authorization.server.config.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class ClientConfig {
    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;
    private final CustomRegisteredClientRepository customRegisteredClientRepository;

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient client = customRegisteredClientRepository.findByClientId("authora-dashboard");
        if (client == null) {
            RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("authora-dashboard")
                    .clientIdIssuedAt(Instant.now())
                    .clientSecret(passwordEncoder.encode(appProperties.clientSecret()))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri(appProperties.callbackUrl())
                    .postLogoutRedirectUri("http://127.0.0.1:8080/")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(false)
                            .requireProofKey(false).
                            build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofHours(1))
                            .refreshTokenTimeToLive(Duration.ofDays(7))
                            .reuseRefreshTokens(false)
                            .build())
                    .build();

            customRegisteredClientRepository.save(oidcClient);
        }
        return customRegisteredClientRepository;
    }



}
