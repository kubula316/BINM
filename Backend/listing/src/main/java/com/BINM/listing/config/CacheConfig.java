package com.BINM.listing.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager mgr = new CaffeineCacheManager("categoryTree");
        mgr.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(Duration.ofHours(12))); // opcjonalny TTL
        return mgr;
    }
}
