package com.example.ratelimiter.core;

public class RateLimitConfig {
    private final Algorithm algorithm;
    private final int limit;
    private final long windowSizeSeconds;

    public RateLimitConfig(Algorithm algorithm, int limit, long windowSizeSeconds) {
        this.algorithm = algorithm;
        this.limit = limit;
        this.windowSizeSeconds = windowSizeSeconds;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public int getLimit() {
        return limit;
    }

    public long getWindowSizeSeconds() {
        return windowSizeSeconds;
    }
}
