package com.example.ratelimiter.limiter;

import com.example.ratelimiter.core.Algorithm;
import com.example.ratelimiter.core.RateLimitConfig;
import com.example.ratelimiter.core.TimeProvider;
import com.example.ratelimiter.core.Window;
import com.example.ratelimiter.store.InMemoryRateLimitStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FixedWindowRateLimiterTest {

    private FixedWindowRateLimiter rateLimiter;
    private FakeTimeProvider timeProvider;

    private static final int LIMIT = 5;
    private static final long WINDOW_SIZE = 60;

    @BeforeEach
    void setUp() {

        InMemoryRateLimitStore store =
                new InMemoryRateLimitStore();

        RateLimitConfig config =
                new RateLimitConfig(
                        Algorithm.FIXED_WINDOW,
                        LIMIT,
                        WINDOW_SIZE
                );

        timeProvider = new FakeTimeProvider(0);

        rateLimiter =
                new FixedWindowRateLimiter(
                        store,
                        config,
                        timeProvider
                );
    }

    @Test
    void shouldAllowRequestWhenUnderLimit() {

        assertTrue(rateLimiter.allow("user1"));
        assertTrue(rateLimiter.allow("user1"));
        assertTrue(rateLimiter.allow("user1"));
    }

    @Test
    void shouldAllowExactlyLimitRequests() {

        for (int i = 0; i < LIMIT; i++) {
            assertTrue(rateLimiter.allow("user1"));
        }
    }

    @Test
    void shouldRejectRequestWhenLimitExceeded() {

        for (int i = 0; i < LIMIT; i++) {
            assertTrue(rateLimiter.allow("user1"));
        }

        assertFalse(rateLimiter.allow("user1"));
    }

    @Test
    void shouldMaintainSeparateLimitsForDifferentUsers() {

        for (int i = 0; i < LIMIT; i++) {
            assertTrue(rateLimiter.allow("user1"));
        }

        for (int i = 0; i < LIMIT; i++) {
            assertTrue(rateLimiter.allow("user2"));
        }

        assertFalse(rateLimiter.allow("user1"));
        assertFalse(rateLimiter.allow("user2"));
    }

    @Test
    void shouldResetLimitWhenWindowChanges() {

        for (int i = 0; i < LIMIT; i++) {
            assertTrue(rateLimiter.allow("user1"));
        }

        assertFalse(rateLimiter.allow("user1"));

        timeProvider.setCurrentTimeSeconds(60);

        assertTrue(rateLimiter.allow("user1"));
    }

    @Test
    void shouldKeepRequestsInSameWindow() {

        timeProvider.setCurrentTimeSeconds(10);

        assertTrue(rateLimiter.allow("user1"));

        timeProvider.setCurrentTimeSeconds(30);

        assertTrue(rateLimiter.allow("user1"));

        timeProvider.setCurrentTimeSeconds(59);

        assertTrue(rateLimiter.allow("user1"));

        assertTrue(rateLimiter.allow("user1"));

        assertTrue(rateLimiter.allow("user1"));

        assertFalse(rateLimiter.allow("user1"));
    }

    @Test
    void shouldStartNewWindowAtExactBoundary() {

        timeProvider.setCurrentTimeSeconds(59);

        for (int i = 0; i < LIMIT; i++) {
            assertTrue(rateLimiter.allow("user1"));
        }

        assertFalse(rateLimiter.allow("user1"));

        timeProvider.setCurrentTimeSeconds(60);

        assertTrue(rateLimiter.allow("user1"));
    }

    private static class FakeTimeProvider implements TimeProvider {

        private long currentTimeSeconds;

        FakeTimeProvider(long initialTimeSeconds) {
            this.currentTimeSeconds = initialTimeSeconds;
        }

        @Override
        public long currentTimeSeconds() {
            return currentTimeSeconds;
        }

        public void setCurrentTimeSeconds(long currentTimeSeconds) {
            this.currentTimeSeconds = currentTimeSeconds;
        }
    }

    @Test
    void shouldAllowBurstAtWindowBoundary() {

        // Last second of first window
        timeProvider.setCurrentTimeSeconds(59);

        for (int i = 0; i < LIMIT; i++) {
            assertTrue(rateLimiter.allow("user1"));
        }

        // Limit reached for first window
        assertFalse(rateLimiter.allow("user1"));

        // First second of next window
        timeProvider.setCurrentTimeSeconds(60);

        // Fixed Window considers this a completely new window
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(rateLimiter.allow("user1"));
        }

        // Limit reached for second window
        assertFalse(rateLimiter.allow("user1"));
    }

    @Test
    void shouldNotAllowMoreThanLimitUnderConcurrency() throws Exception {

        int numberOfRequests = 100;

        ExecutorService executor =
                Executors.newFixedThreadPool(20);

        AtomicInteger allowedRequests =
                new AtomicInteger();

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {

            futures.add(
                    executor.submit(() -> {

                        if (rateLimiter.allow("user1")) {
                            allowedRequests.incrementAndGet();
                        }

                    })
            );
        }

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();

        assertEquals(
                LIMIT,
                allowedRequests.get()
        );
    }
}