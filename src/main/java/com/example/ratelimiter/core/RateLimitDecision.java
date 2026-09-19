package com.example.ratelimiter.core;

public record RateLimitDecision(boolean allowed, int limit, int remaining, long resetAtEpochSeconds, long retryAfterSeconds) {
    public static RateLimitDecision allow(int limit, int remaining, long resetAt) {
        return new RateLimitDecision(true, limit, Math.max(0, remaining), resetAt, 0);
    }

    public static RateLimitDecision reject(int limit, long resetAt, long retryAfter) {
        return new RateLimitDecision(false, limit, 0, resetAt, Math.max(1, retryAfter));
    }
}
