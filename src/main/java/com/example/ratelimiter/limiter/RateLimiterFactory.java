package com.example.ratelimiter.limiter;

import com.example.ratelimiter.core.Algorithm;
import org.springframework.stereotype.Component;

@Component
public class RateLimiterFactory{

    private final FixedWindowRateLimiter fixedWindowRateLimiter;

    public RateLimiterFactory(FixedWindowRateLimiter fixedWindowRateLimiter) {
        this.fixedWindowRateLimiter = fixedWindowRateLimiter;
    }

    public RateLimiter getRateLimiter(Algorithm algorithm) {
        if(algorithm == Algorithm.FIXED_WINDOW) {
            return fixedWindowRateLimiter;
        }

        return null;
    }
}
