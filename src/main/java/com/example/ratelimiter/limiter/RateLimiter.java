package com.example.ratelimiter.limiter;

public interface RateLimiter {

    boolean allow(String key);

}