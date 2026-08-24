package com.example.ratelimiter.core;

public class Window {
    public final long timestamp;
    public int count;

    public Window(long timestamp, int count) {
        this.timestamp = timestamp;
        this.count = count;
    }
}