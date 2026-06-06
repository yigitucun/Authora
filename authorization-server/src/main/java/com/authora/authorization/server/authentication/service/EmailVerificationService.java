package com.authora.authorization.server.authentication.service;

import com.authora.authorization.server.config.properties.AppProperties;
import com.authora.authorization.server.infrastructure.cache.RedisService;
import com.authora.authorization.server.infrastructure.messaging.event.EmailVerificationEvent;
import com.authora.authorization.server.audit.service.AuditLogService;
import com.authora.authorization.server.user.mapper.UserMapper;
import com.authora.authorization.server.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private static final String REDIS_VERIFY_PREFIX = "verify:token:";
    private static final String REDIS_VERIFY_CLIENT_PREFIX = "verify:client:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);
    private static final String EMAIL_VERIFICATION_TOPIC = "notification.email.verification";

    private final RedisService redisService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserMapper userMapper;
    private final AppProperties appProperties;
    private final AuditLogService auditLogService;
    private final SecureRandom secureRandom = new SecureRandom();

    public void sendVerificationEmail(User user, String clientId) {
        if (user.isVerified()) {
            return;
        }

        String token = generateToken();
        Duration ttl = resolveTtl();
        redisService.set(REDIS_VERIFY_PREFIX + token, user.getId().toString(), ttl);
        if (clientId != null && !clientId.isBlank()) {
            redisService.set(REDIS_VERIFY_CLIENT_PREFIX + token, clientId, ttl);
        }

        String verifyUrl = buildVerifyUrl(token);

        EmailVerificationEvent event = new EmailVerificationEvent(user.getEmail(), verifyUrl);
        kafkaTemplate.send(EMAIL_VERIFICATION_TOPIC, user.getId().toString(), event);
    }

    public VerificationResult verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return new VerificationResult(false, null);
        }

        String userId = redisService.get(REDIS_VERIFY_PREFIX + token);
        if (userId == null) {
            return new VerificationResult(false, null);
        }

        String clientId = redisService.get(REDIS_VERIFY_CLIENT_PREFIX + token);

        redisService.delete(REDIS_VERIFY_PREFIX + token);
        redisService.delete(REDIS_VERIFY_CLIENT_PREFIX + token);

        User user = userMapper.findById(UUID.fromString(userId)).orElse(null);
        if (user == null) {
            return new VerificationResult(false, clientId);
        }

        if (!user.isVerified()) {
            userMapper.updateVerified(user.getId(), true, LocalDateTime.now());
            auditLogService.log(user.getTenantId(), user.getId(), "EMAIL_VERIFIED", "USER", user.getId().toString(), null, null, null);
        }

        return new VerificationResult(true, clientId);
    }

    private String generateToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Duration resolveTtl() {
        Long ttlMinutes = appProperties.emailVerificationTtlMinutes();
        if (ttlMinutes == null || ttlMinutes <= 0) {
            return DEFAULT_TTL;
        }
        return Duration.ofMinutes(ttlMinutes);
    }

    private String buildVerifyUrl(String token) {
        String baseUrl = appProperties.publicBaseUrl();
        String normalizedBase = baseUrl != null ? baseUrl.replaceAll("/+$", "") : "";
        return normalizedBase + "/verify-email?token=" + token;
    }

    public record VerificationResult(boolean verified, String clientId) {}
}

