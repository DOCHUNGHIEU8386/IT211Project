package com.example.it211project.service.impl;

import com.example.it211project.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * FR-13 (AF3): Implement Token Blacklist với Redis
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    @Value("${token.blacklist.ttl:86400000}")
    private Long defaultTtlMs;

    @Override
    public void blacklistToken(String token, long ttlSeconds) {
        if (token == null || token.isEmpty()) {
            log.warn("Attempt to blacklist null or empty token");
            return;
        }

        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "revoked", ttlSeconds, TimeUnit.SECONDS);
        log.info("Token blacklisted: {}, TTL: {} seconds", token.substring(0, Math.min(20, token.length())) + "...", ttlSeconds);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        boolean isBlacklisted = Boolean.TRUE.equals(exists);

        if (isBlacklisted) {
            log.debug("Token is blacklisted: {}", token.substring(0, Math.min(20, token.length())) + "...");
        }

        return isBlacklisted;
    }

    @Override
    public void removeFromBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
        log.debug("Token removed from blacklist: {}", token.substring(0, Math.min(20, token.length())) + "...");
    }
}