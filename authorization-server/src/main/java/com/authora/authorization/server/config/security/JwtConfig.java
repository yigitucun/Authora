package com.authora.authorization.server.config.security;

import com.authora.authorization.server.tenant.mapper.TenantMapper;
import com.authora.authorization.server.user.mapper.UserMapper;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class JwtConfig {
    private final UserMapper userMapper;
    private final TenantMapper tenantMapper;
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(){
        return context -> {
            if (context.getTokenType().getValue().equals("id_token") ||
                    context.getTokenType().getValue().equals("access_token")) {

                String email = context.getPrincipal().getName();
                context.getClaims().claim("email", email);

                String clientId = context.getRegisteredClient().getClientId();

                userMapper.findByEmail(email).ifPresent(user -> {
                    tenantMapper.findById(user.getTenantId()).ifPresent(tenant -> {
                        if ("authora-dashboard".equals(clientId)) {
                            context.getClaims().claim("onboarding_completed", tenant.isOnboardingCompleted());
                            context.getClaims().claim("dashboard_access", user.isTenantAdmin());
                            if (tenant.getCompanyName() != null) {
                                context.getClaims().claim("company_name", tenant.getCompanyName());
                            }
                        }
                        else {
                            context.getClaims().claim("tenant_id", tenant.getId());
                        }
                    });
                });
            }
        };
    }
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:8080")
                .build();
    }

}
