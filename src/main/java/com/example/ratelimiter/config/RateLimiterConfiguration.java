package com.example.ratelimiter.config;

import com.example.ratelimiter.core.RateLimitConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfiguration {
    @Bean
    public RateLimitConfig rateLimiterConfig(RateLimiterProperties rateLimiterProperties) {
        return new RateLimitConfig(
                rateLimiterProperties.getAlgorithm(),
                rateLimiterProperties.getLimit(),
                rateLimiterProperties.getWindowSizeSeconds()
        );
    }
}
