package com.br.demo.redis.config;

import com.br.demo.redis.exception.ResourceAlreadyLockedException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("teste.service.name")
public class DistributedLock {

    private final StringRedisTemplate redisTemplate;

    @Value("${teste.service.name}")
    private String serviceName;

    public DistributedLockResource tryAcquire(String resourceId, Duration expiration) throws ResourceAlreadyLockedException {
        final String key = buildLockerKey(resourceId);
        final String value = UUID.randomUUID().toString();
        final boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, value, expiration);
        if (!acquired) {
            throw new ResourceAlreadyLockedException();
        }
        return new DistributedLockResource(key, value);
    }

    private String buildLockerKey(String resourceId) {
        return String.format("%s:distributed-lock:%s", serviceName, resourceId);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public final class DistributedLockResource implements AutoCloseable {
        private final String lockerKey;
        private final String lockerValue;

        public void close() {
            final String value = redisTemplate.opsForValue().get(lockerKey);
            if (value != null && value.equals(lockerValue)) {
                redisTemplate.delete(lockerKey);
            }
        }
    }
}
