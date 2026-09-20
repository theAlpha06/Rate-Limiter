package com.example.ratelimiter.store;

import com.example.ratelimiter.core.RateLimitState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

@Component
public class InMemoryRateLimitStore implements RateLimitStore {

    private final Map<String, RateLimitState> states = new ConcurrentHashMap<>();

    @Override
    public RateLimitState compute(String key, UnaryOperator<RateLimitState> operator) {
        return states.compute(key, (k, current) -> operator.apply(current));
    }
}
