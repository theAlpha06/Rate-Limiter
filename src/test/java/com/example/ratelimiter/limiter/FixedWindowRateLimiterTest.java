package com.example.ratelimiter.limiter;

import com.example.ratelimiter.core.Algorithm;
import com.example.ratelimiter.core.RateLimitDecision;
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

class FixedWindowRateLimiterTest {

    private static final int  LIMIT       = 5;
    private static final long WINDOW_SIZE = 60;

    private FixedWindowRateLimiter rateLimiter;
    private FakeTimeProvider timeProvider;
    private RateLimitPolicy policy;

    @BeforeEach
    void setUp() {
        timeProvider = new FakeTimeProvider(0);
        rateLimiter  = new FixedWindowRateLimiter(new InMemoryRateLimitStore(), timeProvider);
        policy       = new RateLimitPolicy("test", Algorithm.FIXED_WINDOW, LIMIT, WINDOW_SIZE);
    }

    private boolean allow(String key) {
        return rateLimiter.tryAcquire(key, policy, 1).allowed();
    }

    @Test
    void shouldAllowRequestWhenUnderLimit() {
        assertTrue(allow("user1"));
        assertTrue(allow("user1"));
        assertTrue(allow("user1"));
    }

    @Test
    void shouldAllowExactlyLimitRequests() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(allow("user1"), "request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void shouldRejectRequestWhenLimitExceeded() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(allow("user1"));
        }
        assertFalse(allow("user1"));
    }

    @Test
    void shouldMaintainSeparateLimitsForDifferentUsers() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(allow("user1"));
        }
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(allow("user2"));
        }
        assertFalse(allow("user1"));
        assertFalse(allow("user2"));
    }

    @Test
    void shouldResetLimitWhenWindowChanges() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(allow("user1"));
        }
        assertFalse(allow("user1"));

        timeProvider.setCurrentTimeSeconds(60);

        assertTrue(allow("user1"));
    }

    @Test
    void shouldKeepRequestsInSameWindow() {
        timeProvider.setCurrentTimeSeconds(10);
        assertTrue(allow("user1"));

        timeProvider.setCurrentTimeSeconds(30);
        assertTrue(allow("user1"));

        timeProvider.setCurrentTimeSeconds(59);
        assertTrue(allow("user1"));
        assertTrue(allow("user1"));
        assertTrue(allow("user1"));

        assertFalse(allow("user1"));
    }

    @Test
    void shouldStartNewWindowAtExactBoundary() {
        timeProvider.setCurrentTimeSeconds(59);
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(allow("user1"));
        }
        assertFalse(allow("user1"));

        timeProvider.setCurrentTimeSeconds(60);

        assertTrue(allow("user1"));
    }

    @Test
    void shouldAllowBurstAtWindowBoundary() {
        // Last second of the first window
        timeProvider.setCurrentTimeSeconds(59);
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(allow("user1"));
        }
        assertFalse(allow("user1"));

        // First second of the next window
        timeProvider.setCurrentTimeSeconds(60);

        // Fixed window grants an entirely fresh allowance, so 2x LIMIT lands inside two
        // seconds. Inherent to the algorithm — SLIDING_WINDOW is the fix, not a code change.
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(allow("user1"));
        }
        assertFalse(allow("user1"));
    }

    @Test
    void shouldReportRemainingAndResetOnEachDecision() {
        RateLimitDecision first = rateLimiter.tryAcquire("user1", policy, 1);

        assertTrue(first.allowed());
        assertEquals(LIMIT, first.limit());
        assertEquals(LIMIT - 1, first.remaining());
        assertEquals(WINDOW_SIZE, first.resetAtEpochSeconds());
        assertEquals(0, first.retryAfterSeconds());
    }

    @Test
    void shouldReportRetryAfterWhenRejected() {
        timeProvider.setCurrentTimeSeconds(10);
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(allow("user1"));
        }

        RateLimitDecision rejected = rateLimiter.tryAcquire("user1", policy, 1);

        assertFalse(rejected.allowed());
        assertEquals(0, rejected.remaining());
        assertEquals(WINDOW_SIZE, rejected.resetAtEpochSeconds());
        assertEquals(50, rejected.retryAfterSeconds(), "window ends at 60, now is 10");
    }

    @Test
    void shouldConsumeCostUnitsPerRequest() {
        assertTrue(rateLimiter.tryAcquire("user1", policy, 4).allowed());
        assertTrue(rateLimiter.tryAcquire("user1", policy, 1).allowed());
        assertFalse(rateLimiter.tryAcquire("user1", policy, 1).allowed());
    }

    @Test
    void shouldRejectCostLargerThanLimitWithoutConsumingAllowance() {
        assertFalse(rateLimiter.tryAcquire("user1", policy, LIMIT + 1).allowed());

        // the oversized request must not have eaten into the allowance
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(allow("user1"));
        }
    }

    @Test
    void shouldNotAllowMoreThanLimitUnderConcurrency() throws Exception {
        int numberOfRequests = 100;

        ExecutorService executor = Executors.newFixedThreadPool(20);
        AtomicInteger allowedRequests = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {
            futures.add(executor.submit(() -> {
                if (allow("user1")) {
                    allowedRequests.incrementAndGet();
                }
            }));
        }

        for (Future<?> future : futures) {
            future.get();
        }
        executor.shutdown();

        assertEquals(LIMIT, allowedRequests.get());
    }
}
