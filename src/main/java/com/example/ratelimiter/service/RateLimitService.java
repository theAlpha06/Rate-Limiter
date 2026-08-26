package com.example.ratelimiter.service;

import com.example.ratelimiter.config.RateLimiterProperties;
import com.example.ratelimiter.limiter.RateLimiter;
import com.example.ratelimiter.limiter.RateLimiterFactory;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final RateLimiter rateLimiter;

    public RateLimitService(
            RateLimiterFactory rateLimiterFactory,
            RateLimiterProperties rateLimiterProperties) {
        this.rateLimiter = rateLimiterFactory.getRateLimiter(rateLimiterProperties.getAlgorithm());
    }

    public boolean allow(String key) {
        return rateLimiter.allow(key);
    }
}
