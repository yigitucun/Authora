package com.authora.authorization.server.infrastructure.cache;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
@AllArgsConstructor
public class RedisService {
    private static final String PREFIX = "authora:";
    private final StringRedisTemplate redisTemplate;

    public String get(String key){
        return redisTemplate.opsForValue().get(PREFIX+key);
    }

    public void set(String key, String value, Duration duration){
        redisTemplate.opsForValue().set(PREFIX+key,value,duration);
    }
    public void delete(String key){
        redisTemplate.delete(PREFIX+key);
    }

    public void hSetAll(String key, Map<String, String> fields, Duration duration) {
        String fullKey = PREFIX + key;
        redisTemplate.opsForHash().putAll(fullKey, fields);
        redisTemplate.expire(fullKey, duration);
    }

    public void hSet(String key, String field,String value){
        String fullKey = PREFIX + key;
        redisTemplate.opsForHash().put(fullKey,field,value);
    }

    public String hGet(String key, String hashKey) {
        Object value = redisTemplate.opsForHash().get(PREFIX + key, hashKey);
        return value != null ? value.toString() : null;
    }
    public void expire(String key, Duration duration) {
        redisTemplate.expire(PREFIX + key, duration);
    }

}
