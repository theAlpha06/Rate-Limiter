package com.example.ratelimiter.store;

import com.example.ratelimiter.core.Window;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

@Component
public class InMemoryRateLimitStore implements RateLimitStore{

    private final Map<String, Window> requests = new ConcurrentHashMap<>();

    @Override
    public Window compute(String key, UnaryOperator<Window> operator) {
        return requests.compute(key, (k, currentWindow) -> operator.apply(currentWindow));
    }
}
