package com.authora.authorization.server.infrastructure.otp;

import com.authora.authorization.server.common.exception.GlobalException;
import com.authora.authorization.server.infrastructure.cache.RedisService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final RedisService redisService;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final String REDIS_OTP_PREFIX = "otp:tx:";
    private static final String REDIS_EMAIL_INDEX_PREFIX = "otp:email_to_tx:";
    private static final Duration OTP_TIME_TO_LIVE = Duration.ofMinutes(15);
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    private String generateSecureCode(){
        return String.valueOf( 100000+secureRandom.nextInt(900000));
    }

    private String generateTransactionToken(){
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public void refreshOtp(String transactionToken){
        String redisKey = REDIS_OTP_PREFIX + transactionToken;
        String email = redisService.hGet(redisKey, "email");
        if (email == null) {
            throw new GlobalException("Invalid or expired transaction!", HttpStatus.NOT_FOUND);
        }

        String lastResendAt = redisService.hGet(redisKey, "resend_at");
        if (lastResendAt != null) {
            long secondsPassed = Duration.between(
                    Instant.ofEpochSecond(Long.parseLong(lastResendAt)),
                    Instant.now()
            ).getSeconds();
            if (secondsPassed < RESEND_COOLDOWN_SECONDS) {
                throw new GlobalException(
                        "Please wait " + (RESEND_COOLDOWN_SECONDS - secondsPassed) + " seconds before requesting a new code.",
                        HttpStatus.TOO_MANY_REQUESTS
                );
            }
        }

        String newOtp = generateSecureCode();
        redisService.hSet(redisKey, "otp", newOtp);
        redisService.hSet(redisKey, "attempts", "5");
        redisService.hSet(redisKey, "resend_at", String.valueOf(Instant.now().getEpochSecond()));
        redisService.expire(redisKey, OTP_TIME_TO_LIVE);
    }

    public OtpGenerationResult generateOtpAndSave(String email){
        String otp = generateSecureCode();
        String transactionToken = generateTransactionToken();
        String redisKey = REDIS_OTP_PREFIX + transactionToken;
        Map<String,String> otpData = Map.of(
                "email", email,
                "otp",otp,
                "transaction_token",transactionToken,
                "attempts", "5",
                "resend_at", String.valueOf(Instant.now().getEpochSecond())
        );
        redisService.hSetAll(redisKey,otpData,OTP_TIME_TO_LIVE);
        redisService.set(REDIS_EMAIL_INDEX_PREFIX+email,transactionToken,OTP_TIME_TO_LIVE);
        return new OtpGenerationResult(transactionToken,otp);
    }
    public String getEmailByTransactionToken(String transactionToken) {
        String redisKey = REDIS_OTP_PREFIX + transactionToken;
        String email = redisService.hGet(redisKey, "email");
        if (email == null) {
            throw new GlobalException("Invalid or expired transaction!", HttpStatus.NOT_FOUND);
        }
        return email;
    }

    public String verifyOtpCode(String userInputOtp,String transactionToken){
        String redisKey = REDIS_OTP_PREFIX + transactionToken;
        String otp = redisService.hGet(redisKey,"otp");
        String attemptsStr = redisService.hGet(redisKey,"attempts");
        String email = redisService.hGet(redisKey,"email");
        if (otp==null || attemptsStr==null){
            throw new GlobalException("Invalid or expired transaction!",HttpStatus.BAD_REQUEST);
        }
        int attempts = Integer.parseInt(attemptsStr);
        if (attempts <= 0) {
            throw new GlobalException("Maximum attempt limit reached! Please request a new code.", HttpStatus.TOO_MANY_REQUESTS);
        }
        if (!otp.equals(userInputOtp)){
            attempts--;
            redisService.hSet(redisKey,"attempts",String.valueOf(attempts));
            throw new GlobalException("Invalid or expired transaction!", HttpStatus.BAD_REQUEST);
        }
        redisService.delete(redisKey);
        redisService.delete(REDIS_EMAIL_INDEX_PREFIX+email);
        return email;

    }

    public String getActiveTokenByEmail(String email) {
        return (String) redisService.get(REDIS_EMAIL_INDEX_PREFIX + email);
    }

    public record OtpGenerationResult(String transactionToken, String otpCode) {}

}
