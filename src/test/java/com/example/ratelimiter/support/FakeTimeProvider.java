package com.example.ratelimiter.support;

import com.example.ratelimiter.core.TimeProvider;

/**
 * A clock the test drives by hand. This is what makes window-boundary and refill
 * behaviour deterministic — the alternative is sleeping for real seconds, which is
 * both slow and flaky, since windows align to absolute time rather than test start.
 * <p>
 * {@code volatile} because concurrency tests read it from many threads.
 */
public class FakeTimeProvider implements TimeProvider {

    private volatile long currentTimeSeconds;

    public FakeTimeProvider(long initialTimeSeconds) {
        this.currentTimeSeconds = initialTimeSeconds;
    }

    @Override
    public long currentTimeSeconds() {
        return currentTimeSeconds;
    }

    public void setCurrentTimeSeconds(long currentTimeSeconds) {
        this.currentTimeSeconds = currentTimeSeconds;
    }

    public void advance(long seconds) {
        this.currentTimeSeconds += seconds;
    }
}
