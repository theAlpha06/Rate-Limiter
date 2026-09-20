package com.example.ratelimiter.store;

import com.example.ratelimiter.core.RateLimitState;

import java.util.function.UnaryOperator;

public interface RateLimitStore {

    RateLimitState compute(String key, UnaryOperator<RateLimitState> operator);
}
