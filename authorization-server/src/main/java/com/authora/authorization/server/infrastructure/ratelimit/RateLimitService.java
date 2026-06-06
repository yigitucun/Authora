package com.authora.authorization.server.infrastructure.ratelimit;

import com.authora.authorization.server.infrastructure.cache.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final String PREFIX = "rate:";
    private static final int MAX_ATTEMPTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);
    private static final String BLOCKED_SUFFIX = ":blocked";

    private final RedisService redisService;

    /**
     * Returns true if the request is allowed, false if it should be blocked.
     */
    public boolean isAllowed(String ip, String endpoint) {
        String blockedKey = PREFIX + endpoint + ":" + ip + BLOCKED_SUFFIX;
        if (redisService.get(blockedKey) != null) {
            return false;
        }

        String countKey = PREFIX + endpoint + ":" + ip;
        String countStr = redisService.get(countKey);

        int count = countStr != null ? Integer.parseInt(countStr) : 0;

        if (count == 0) {
            redisService.set(countKey, "1", WINDOW);
            return true;
        }

        if (count >= MAX_ATTEMPTS) {
            redisService.set(blockedKey, "1", BLOCK_DURATION);
            redisService.delete(countKey);
            return false;
        }

        redisService.set(countKey, String.valueOf(count + 1), WINDOW);
        return true;
    }

    public boolean isBlocked(String ip, String endpoint) {
        String blockedKey = PREFIX + endpoint + ":" + ip + BLOCKED_SUFFIX;
        return redisService.get(blockedKey) != null;
    }
}
