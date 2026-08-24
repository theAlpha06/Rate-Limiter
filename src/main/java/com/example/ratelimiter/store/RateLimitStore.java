package com.example.ratelimiter.store;

import com.example.ratelimiter.core.Window;

public interface RateLimitStore {
    Window get(String key);

    void put (String key, Window window);
}
