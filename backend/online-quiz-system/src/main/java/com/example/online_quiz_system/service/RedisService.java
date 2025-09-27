package com.example.online_quiz_system.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    private final StringRedisTemplate redis;

    public RedisService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void cacheAccessToken(String tokenSignature, String value, Duration ttl) {
        redis.opsForValue().set("access_token:" + tokenSignature, value, ttl);
    }

    public void cacheRefreshToken(String tokenHash, String value, Duration ttl) {
        redis.opsForValue().set("refresh_token:" + tokenHash, value, ttl);
    }

    public void blacklistToken(String tokenSignature, Duration ttl) {
        redis.opsForValue().set("blacklist:" + tokenSignature, String.valueOf(System.currentTimeMillis()), ttl);
    }

    public boolean isBlacklisted(String tokenSignature) {
        return Boolean.TRUE.equals(redis.hasKey("blacklist:" + tokenSignature));
    }

    public void deleteAccessTokenSignature(String tokenSignature) {
        redis.delete("access_token:" + tokenSignature);
    }

    public void deleteRefreshTokenHash(String tokenHash) {
        redis.delete("refresh_token:" + tokenHash);
    }

    public long incrementWithTtl(String key, Duration ttl) {
        Long value = redis.opsForValue().increment(key);
        if (value != null && value == 1L) {
            redis.expire(key, ttl);
        }
        return value == null ? 0L : value;
    }

    public void blacklistJti(String jti, Duration ttl) {
        redis.opsForValue().set("jti:blacklist:" + jti, String.valueOf(System.currentTimeMillis()), ttl);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redis.hasKey(key));
    }
}


