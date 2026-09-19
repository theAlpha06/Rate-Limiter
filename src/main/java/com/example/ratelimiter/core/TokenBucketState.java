package com.example.ratelimiter.core;

public record TokenBucketState(double tokens, long lastRefillEpochSeconds) implements RateLimitState { }
