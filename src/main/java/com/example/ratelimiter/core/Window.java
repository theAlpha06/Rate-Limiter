package com.example.ratelimiter.core;

public record Window(long windowStart, int count) implements RateLimitState {

    public Window plus(int cost) {
        return new Window(windowStart, count + cost);
    }
}
