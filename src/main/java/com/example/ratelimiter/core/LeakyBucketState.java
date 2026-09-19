package com.example.ratelimiter.core;

public record LeakyBucketState(double level, long lastLeakEpochSeconds) implements RateLimitState { }
