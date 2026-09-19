package com.example.ratelimiter.core;

public record RateLimitPolicy(String name, Algorithm algorithm, int limit, long windowSizeSeconds) {

    public RateLimitPolicy {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("policy name is required");
        if (algorithm == null)
            throw new IllegalArgumentException("algorithm is required for policy '" + name + "'");
        if (limit < 1)
            throw new IllegalArgumentException("limit must be >= 1 for policy '" + name + "'");
        if (windowSizeSeconds < 1)
            throw new IllegalArgumentException("window-size-seconds must be >= 1 for policy '" + name + "'");
    }

    public double ratePerSecond() { return (double) limit / windowSizeSeconds; }
}
