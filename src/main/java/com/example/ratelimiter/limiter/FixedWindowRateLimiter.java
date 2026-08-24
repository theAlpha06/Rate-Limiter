package com.example.ratelimiter.limiter;

import com.example.ratelimiter.core.RateLimitConfig;
import com.example.ratelimiter.core.Window;
import com.example.ratelimiter.store.RateLimitStore;
import org.springframework.stereotype.Component;

@Component
public class FixedWindowRateLimiter implements RateLimiter {

    private final RateLimitStore rateLimitStore;
    private final RateLimitConfig rateLimitConfig;
    public FixedWindowRateLimiter(RateLimitStore rateLimitStore, RateLimitConfig rateLimitConfig) {
        this.rateLimitStore = rateLimitStore;
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public boolean allow(String key) {
        return putRequest(key);
    }

    private boolean putRequest(String key) {
        long currentTimeStamp = System.currentTimeMillis() / 1000;
        long windowStartTime = (currentTimeStamp / rateLimitConfig.getWindowSizeSeconds()) * rateLimitConfig.getWindowSizeSeconds();
        Window window = rateLimitStore.get(key);

        if(window == null || window.windowStart != windowStartTime) {
            rateLimitStore.put(key, new Window(windowStartTime, 1));
        } else {
            if(window.count + 1 > rateLimitConfig.getLimit()) {
                return false;
            } else {
                window.count += 1;
            }
        }
        return true;
    }
}