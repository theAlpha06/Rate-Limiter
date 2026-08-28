package com.example.ratelimiter.store;

import com.example.ratelimiter.core.Window;

import java.util.function.UnaryOperator;

public interface RateLimitStore {
    Window compute(String key, UnaryOperator<Window> operator);
}
