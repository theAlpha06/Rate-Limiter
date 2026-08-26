package com.example.ratelimiter.core;

import org.springframework.stereotype.Component;

@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }
}