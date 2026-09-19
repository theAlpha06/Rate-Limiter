package com.example.ratelimiter.core;

public record SlidingWindowState(long windowStart, int currentCount, int previousCount) implements RateLimitState {
    public SlidingWindowState plus(int cost) {
        return new SlidingWindowState(windowStart, currentCount + cost, previousCount);
    }
}
