package com.example.ratelimiter.api;

import com.example.ratelimiter.core.RateLimitDecision;

public record CheckResponse(boolean allowed, int limit, int remaining, long resetAt, long retryAfter) {
    public static CheckResponse from(RateLimitDecision d) {
        return new CheckResponse(d.allowed(), d.limit(), d.remaining(),
                d.resetAtEpochSeconds(), d.retryAfterSeconds());
    }
}