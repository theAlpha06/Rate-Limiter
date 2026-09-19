package com.example.ratelimiter.service;

import com.example.ratelimiter.config.PolicyRegistry;
import com.example.ratelimiter.core.RateLimitDecision;
import com.example.ratelimiter.core.RateLimitPolicy;
import com.example.ratelimiter.limiter.RateLimiter;
import com.example.ratelimiter.limiter.RateLimiterFactory;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final PolicyRegistry policyRegistry;
    private final RateLimiterFactory factory;

    public RateLimitService(PolicyRegistry policyRegistry, RateLimiterFactory factory) {
        this.policyRegistry = policyRegistry;
        this.factory = factory;
    }

    public RateLimitDecision check(String policyName, String key, int cost) {
        RateLimitPolicy policy = policyRegistry.get(policyName);
        if(cost > policy.limit()) {
            throw new CostExceedsLimitException(cost, policy.limit());
        }
        RateLimiter limiter = factory.get(policy.algorithm());
        return limiter.tryAcquire(policy.name() + ":" + key, policy, cost);
    }
}

