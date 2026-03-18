package com.paymentapp.api.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void save() {
        redisTemplate.opsForValue().set("hello", "world");
    }

    public String get() {
        return redisTemplate.opsForValue().get("hello");
    }
}