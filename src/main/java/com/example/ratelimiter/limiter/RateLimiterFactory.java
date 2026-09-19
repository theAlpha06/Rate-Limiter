package com.example.ratelimiter.limiter;

import com.example.ratelimiter.core.Algorithm;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class RateLimiterFactory {

    private final Map<Algorithm, RateLimiter> limiters = new EnumMap<>(Algorithm.class);

    public RateLimiterFactory(List<RateLimiter> rateLimiters) {
        for (RateLimiter limiter : rateLimiters) {
            RateLimiter previous = limiters.put(limiter.algorithm(), limiter);
            if (previous != null) {
                throw new IllegalStateException("Two RateLimiter beans claim " + limiter.algorithm());
            }
        }
    }

    public RateLimiter get(Algorithm algorithm) {
        RateLimiter limiter = limiters.get(algorithm);
        if (limiter == null) {
            throw new IllegalStateException(
                    "No RateLimiter for " + algorithm + ". Available: " + limiters.keySet());
        }
        return limiter;
    }
}
