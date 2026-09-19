package com.example.ratelimiter.limiter;

import com.example.ratelimiter.core.Algorithm;
import com.example.ratelimiter.core.RateLimitDecision;
import com.example.ratelimiter.core.RateLimitPolicy;

public interface RateLimiter {
    RateLimitDecision tryAcquire(String key, RateLimitPolicy policy, int cost);
    Algorithm algorithm();
}