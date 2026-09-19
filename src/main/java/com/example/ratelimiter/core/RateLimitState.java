package com.example.ratelimiter.core;

public sealed interface RateLimitState
        permits Window, SlidingWindowState, TokenBucketState, LeakyBucketState { }
