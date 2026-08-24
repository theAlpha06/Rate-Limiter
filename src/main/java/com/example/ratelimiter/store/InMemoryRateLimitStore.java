package com.example.ratelimiter.store;

import com.example.ratelimiter.core.Window;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryRateLimitStore implements RateLimitStore{

    private final Map<String, Window> requests = new HashMap<>();

    @Override
    public Window get(String key) {
        return requests.get(key);
    }

    @Override
    public void put(String key, Window window) {
        requests.put(key, window);
    }
}
