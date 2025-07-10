package org.bank.account.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisLockService {
    private final StringRedisTemplate stringRedisTemplate;

    public boolean tryLock(String key, long timeoutMillis) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, "locked", timeoutMillis, TimeUnit.MILLISECONDS));
    }

    public void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
