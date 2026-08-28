package com.example.ratelimiter.core;

public class Window {

    private final long windowStart;
    private final int count;

    public Window(long windowStart, int count) {
        this.windowStart = windowStart;
        this.count = count;
    }

    public long getWindowStart() {
        return windowStart;
    }

    public int getCount() {
        return count;
    }

    public Window increment() {
        return new Window(windowStart, count + 1);
    }
}