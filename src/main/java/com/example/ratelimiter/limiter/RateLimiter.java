package com.example.ratelimiter.limiter;

public interface RateLimiter {
    public final int MAX_REQUESTS = 5;
    public final long WINDOW_SIZE_SECONDS = 60;

    boolean allow(String key);

}