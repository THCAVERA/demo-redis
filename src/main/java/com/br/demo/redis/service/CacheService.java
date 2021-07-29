package com.br.demo.redis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class CacheService {

    @Autowired
    private StringRedisTemplate redis;

    private ObjectMapper mapper = new ObjectMapper();

    private static final int MINUTES = 5;

    public <T> T findOrUpdate(String key, Class<T> clazz, Supplier<T> fn) {

        val found = findCache(key);
        if (found != null) {
            try {
                return String.class.equals(clazz)
                        ? clazz.cast(found)
                        : mapper.readValue(found, clazz);
            } catch (Exception e) {
                log.error("Oops", e);
            }
        }

        T value = fn.get();

        updateCache(key, value, MINUTES);

        return value;
    }

    public void updateCache(String key, Object data) {
        updateCache(key, data, MINUTES);
    }

    public void updateCache(String key, Object data, int minutes) {
        try {
            val value = data instanceof String
                    ? (String) data
                    : mapper.writeValueAsString(data);
            redis.opsForValue().set(key, value, minutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Oops", e);
        }
    }

    public void delete(String key) {
        retry(2, () -> redis.delete(key));
    }

    public String findCache(String key) {
        try {
            return redis.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Oops", e);
            return null;
        }
    }

    private void retry(int attempts, Runnable fn) {
        for (int i = 0; i < attempts; i++) {
            try {
                fn.run();
                break;
            } catch (Exception e) {
                log.error("Oops", e);
            }
        }
    }
}