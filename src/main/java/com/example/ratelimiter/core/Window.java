package com.example.ratelimiter.core;

public class Window {

    private final long windowStart;
    private int count;

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

    public void increment() {
        count++;
    }
}