package com.example.ratelimiter.config;

import com.example.ratelimiter.core.RateLimitPolicy;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PolicyRegistry {

    private final Map<String, RateLimitPolicy> policies;

    public PolicyRegistry(RateLimiterProperties properties) {
        Map<String, RateLimitPolicy> resolved = new LinkedHashMap<>();
        properties.getPolicies().forEach((name, p) ->
                resolved.put(name, new RateLimitPolicy(
                        name, p.getAlgorithm(), p.getLimit(), p.getWindowSizeSeconds())));

        if (resolved.isEmpty()) {
            throw new IllegalStateException("No policies configured under 'rate-limiter.policies'");
        }
        this.policies = Map.copyOf(resolved);
    }

    public RateLimitPolicy get(String name) {
        RateLimitPolicy policy = policies.get(name);
        if (policy == null) throw new UnknownPolicyException(name, policies.keySet());
        return policy;
    }
}
