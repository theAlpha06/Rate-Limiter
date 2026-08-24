package com.example.ratelimiter.core;

public class Window {
    public final long windowStart;
    public int count;

    public Window(long windowStart, int count) {
        this.windowStart = windowStart;
        this.count = count;
    }
}