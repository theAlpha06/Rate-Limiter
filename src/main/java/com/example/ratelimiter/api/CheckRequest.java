package com.example.ratelimiter.api;

public record CheckRequest(String policy, String key, Integer cost) { }
