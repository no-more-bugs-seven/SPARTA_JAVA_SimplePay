package com.paymentapp.core.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisUtil {
    public static final String RT = "RT ";
    public static final String BL = "BL ";
    private final StringRedisTemplate redisTemplate;

    public void save(String prefix, String key, String value, long timeout) {
        redisTemplate.opsForValue().set(prefix + key, value, timeout, TimeUnit.SECONDS);
    }

    public String get(String prefix, String key) {
        return redisTemplate.opsForValue().get(prefix + key);
    }

    public void delete(String prefix, String key) {
        redisTemplate.delete(prefix + key);
    }
}