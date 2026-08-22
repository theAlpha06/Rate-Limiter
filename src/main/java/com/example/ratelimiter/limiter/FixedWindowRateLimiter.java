package com.example.ratelimiter.limiter;

import java.util.HashMap;
import java.util.Map;

public class FixedWindowRateLimiter implements RateLimiter {
    private final Map<String, Window> requests = new HashMap<>();

    @Override
    public boolean allow(String key) {
        return putRequest(key);
    }

    public boolean putRequest(String key) {
        long currentTimeStamp = System.currentTimeMillis() / 1000;
        long windowStartTime = (currentTimeStamp / WINDOW_SIZE_SECONDS) * WINDOW_SIZE_SECONDS;
        Window window = requests.get(key);

        if(window == null || window.timestamp != windowStartTime) {
            requests.put(key, new Window(windowStartTime, 1));
        } else {
            if(window.count + 1 > MAX_REQUESTS) {
                return false;
            } else {
                window.count += 1;
            }
        }
        return true;
    }

    static class Window {
        private final long timestamp;
        private int count;

        Window(long timestamp, int count) {
            this.timestamp = timestamp;
            this.count = count;
        }
    }
}