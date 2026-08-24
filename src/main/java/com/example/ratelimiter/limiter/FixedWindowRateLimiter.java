package com.example.ratelimiter.limiter;

import com.example.ratelimiter.core.Window;
import com.example.ratelimiter.store.RateLimitStore;
import org.springframework.stereotype.Component;

@Component
public class FixedWindowRateLimiter implements RateLimiter {

    private final RateLimitStore rateLimitStore;
    public FixedWindowRateLimiter(RateLimitStore rateLimitStore) {
        this.rateLimitStore = rateLimitStore;
    }

    @Override
    public boolean allow(String key) {
        return putRequest(key);
    }

    public boolean putRequest(String key) {
        long currentTimeStamp = System.currentTimeMillis() / 1000;
        long windowStartTime = (currentTimeStamp / WINDOW_SIZE_SECONDS) * WINDOW_SIZE_SECONDS;
        Window window = rateLimitStore.get(key);

        if(window == null || window.timestamp != windowStartTime) {
            rateLimitStore.put(key, new Window(windowStartTime, 1));
        } else {
            if(window.count + 1 > MAX_REQUESTS) {
                return false;
            } else {
                window.count += 1;
            }
        }
        return true;
    }
}