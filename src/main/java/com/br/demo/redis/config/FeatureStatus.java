package com.br.demo.redis.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeatureStatus {

    private static final String HASH_FEATURE_STATUS = "feature_status";

    public enum Feature {
        FOLLOW_WRITE,
        SEARCH_BATCH
    }

    private HashOperations<String, String, String> client;

    @Autowired
    public FeatureStatus(StringRedisTemplate client) {
        this.client = client.opsForHash();
    }

    public boolean isInactive(Feature feature) {
        return checkStatus(feature, Status.INACTIVE, false);
    }

    public boolean isActive(Feature feature) {
        return checkStatus(feature, Status.ACTIVE, true);
    }

    private boolean checkStatus(Feature feature, Status status, boolean defaultIfNull) {
        try {
            val value = client.get(HASH_FEATURE_STATUS, feature.name());
            if (value != null) {
                return value.equals(status.getValue());
            }
        } catch (Exception e) {
            log.error("redis connection error", e);
        }
        return defaultIfNull;
    }

    @Getter
    @AllArgsConstructor
    private enum Status {
        INACTIVE("inactive"),
        ACTIVE("active");

        private String value;
    }
}
