package com.example.ratelimiter.limiter;

import com.example.ratelimiter.core.RateLimitConfig;
import com.example.ratelimiter.core.TimeProvider;
import com.example.ratelimiter.core.Window;
import com.example.ratelimiter.store.RateLimitStore;
import org.springframework.stereotype.Component;

@Component
public class FixedWindowRateLimiter implements RateLimiter {

    private final RateLimitStore rateLimitStore;
    private final RateLimitConfig rateLimitConfig;
    private final TimeProvider timeProvider;
    public FixedWindowRateLimiter(RateLimitStore rateLimitStore, RateLimitConfig rateLimitConfig, TimeProvider timeProvider) {
        this.rateLimitStore = rateLimitStore;
        this.rateLimitConfig = rateLimitConfig;
        this.timeProvider = timeProvider;
    }

    @Override
    public boolean allow(String key) {
        return putRequest(key);
    }

    private boolean putRequest(String key) {
        long currentTimeStamp = timeProvider.currentTimeSeconds();
        long windowStartTime = (currentTimeStamp / rateLimitConfig.getWindowSizeSeconds()) * rateLimitConfig.getWindowSizeSeconds();
        Window window = rateLimitStore.get(key);

        if(window == null || window.getWindowStart() != windowStartTime) {
            rateLimitStore.put(key, new Window(windowStartTime, 1));
        } else {
            if(window.getCount() >= rateLimitConfig.getLimit()) {
                return false;
            } else {
                window.increment();
            }
        }
        return true;
    }
}