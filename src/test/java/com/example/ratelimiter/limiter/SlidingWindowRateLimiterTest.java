package com.example.ratelimiter.limiter;

import com.example.ratelimiter.core.Algorithm;
import com.example.ratelimiter.core.RateLimitPolicy;
import com.example.ratelimiter.store.InMemoryRateLimitStore;
import com.example.ratelimiter.support.FakeTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowRateLimiterTest {

    private static final int  LIMIT       = 5;
    private static final long WINDOW_SIZE = 60;

    private SlidingWindowRateLimiter rateLimiter;
    private FakeTimeProvider timeProvider;
    private RateLimitPolicy policy;

    @BeforeEach
    void setUp() {
        timeProvider = new FakeTimeProvider(0);
        rateLimiter  = new SlidingWindowRateLimiter(new InMemoryRateLimitStore(), timeProvider);
        policy       = new RateLimitPolicy("test", Algorithm.SLIDING_WINDOW, LIMIT, WINDOW_SIZE);
    }

    private boolean allow(String key) {
        return rateLimiter.tryAcquire(key, policy, 1).allowed();
    }

    private int allowedOutOf(int attempts, String key) {
        int allowed = 0;
        for (int i = 0; i < attempts; i++) {
            if (allow(key)) allowed++;
        }
        return allowed;
    }

    @Test
    void shouldAllowExactlyLimitRequestsInAFreshWindow() {
        assertEquals(LIMIT, allowedOutOf(LIMIT + 3, "user1"));
    }

    @Test
    void shouldMaintainSeparateLimitsForDifferentUsers() {
        assertEquals(LIMIT, allowedOutOf(LIMIT + 1, "user1"));
        assertEquals(LIMIT, allowedOutOf(LIMIT + 1, "user2"));
    }

    /**
     * The whole reason this algorithm exists. Fixed window hands out a completely
     * fresh allowance the instant a boundary passes, admitting 2x the limit across
     * it. Here the previous window still carries near-full weight, so it does not.
     */
    @Test
    void shouldNotAllowFreshAllowanceImmediatelyAfterBoundary() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(allow("user1"));
        }
        assertFalse(allow("user1"));

        timeProvider.setCurrentTimeSeconds(WINDOW_SIZE);   // first second of the next window

        assertFalse(allow("user1"), "previous window still weighted at ~1.0, so nothing is free yet");
    }

    @Test
    void shouldReleaseAllowanceGraduallyAsPreviousWindowAgesOut() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(allow("user1"));
        }

        // Halfway through the next window: previous counts at 0.5, so the estimate is
        // 5 * 0.5 = 2.5, leaving room for exactly 2 more.
        timeProvider.setCurrentTimeSeconds(90);

        assertEquals(2, allowedOutOf(5, "user1"));
    }

    @Test
    void shouldRestoreFullAllowanceAfterAnIdleGap() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(allow("user1"));
        }

        // More than one window elapsed, so there is no previous window to carry over
        timeProvider.setCurrentTimeSeconds(WINDOW_SIZE * 3);

        assertEquals(LIMIT, allowedOutOf(LIMIT + 1, "user1"));
    }

    @Test
    void shouldReportRetryAfterWhenRejected() {
        timeProvider.setCurrentTimeSeconds(10);
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(allow("user1"));
        }

        var rejected = rateLimiter.tryAcquire("user1", policy, 1);

        assertFalse(rejected.allowed());
        assertEquals(0, rejected.remaining());
        assertEquals(WINDOW_SIZE, rejected.resetAtEpochSeconds());
        assertEquals(50, rejected.retryAfterSeconds());
    }

    @Test
    void shouldConsumeCostUnitsPerRequest() {
        assertTrue(rateLimiter.tryAcquire("user1", policy, 4).allowed());
        assertTrue(rateLimiter.tryAcquire("user1", policy, 1).allowed());
        assertFalse(rateLimiter.tryAcquire("user1", policy, 1).allowed());
    }

    @Test
    void shouldNotAllowMoreThanLimitUnderConcurrency() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        AtomicInteger allowed = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            futures.add(executor.submit(() -> {
                if (allow("user1")) allowed.incrementAndGet();
            }));
        }
        for (Future<?> f : futures) f.get();
        executor.shutdown();

        assertEquals(LIMIT, allowed.get());
    }
}
