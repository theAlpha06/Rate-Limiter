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

class TokenBucketRateLimiterTest {

    // 5 tokens over 5 seconds = exactly 1 token/second, which keeps the arithmetic
    // exact and the expected retry-after values free of floating point noise.
    private static final int  LIMIT       = 5;
    private static final long WINDOW_SIZE = 5;

    private TokenBucketRateLimiter rateLimiter;
    private FakeTimeProvider timeProvider;
    private RateLimitPolicy policy;

    @BeforeEach
    void setUp() {
        timeProvider = new FakeTimeProvider(0);
        rateLimiter  = new TokenBucketRateLimiter(new InMemoryRateLimitStore(), timeProvider);
        policy       = new RateLimitPolicy("test", Algorithm.TOKEN_BUCKET, LIMIT, WINDOW_SIZE);
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

    /** The bucket starts full, which is what makes this algorithm burst-tolerant. */
    @Test
    void shouldAllowAFullBurstUpToCapacityImmediately() {
        assertEquals(LIMIT, allowedOutOf(LIMIT + 3, "user1"));
    }

    @Test
    void shouldMaintainSeparateBucketsForDifferentUsers() {
        assertEquals(LIMIT, allowedOutOf(LIMIT + 1, "user1"));
        assertEquals(LIMIT, allowedOutOf(LIMIT + 1, "user2"));
    }

    @Test
    void shouldRefillOneTokenPerSecond() {
        assertEquals(LIMIT, allowedOutOf(LIMIT + 1, "user1"));   // drain it
        assertFalse(allow("user1"));

        timeProvider.advance(1);
        assertTrue(allow("user1"), "one second should have refilled exactly one token");
        assertFalse(allow("user1"), "and only one");

        timeProvider.advance(2);
        assertEquals(2, allowedOutOf(4, "user1"));
    }

    @Test
    void shouldNotRefillBeyondCapacity() {
        assertEquals(LIMIT, allowedOutOf(LIMIT, "user1"));

        // Idle far longer than it takes to refill — the bucket must cap at capacity
        timeProvider.advance(WINDOW_SIZE * 100);

        assertEquals(LIMIT, allowedOutOf(LIMIT + 3, "user1"));
    }

    @Test
    void shouldReportRemainingTokens() {
        RateLimitDecision first = rateLimiter.tryAcquire("user1", policy, 1);

        assertTrue(first.allowed());
        assertEquals(LIMIT, first.limit());
        assertEquals(LIMIT - 1, first.remaining());
        assertEquals(0, first.retryAfterSeconds());
    }

    @Test
    void shouldReportRetryAfterWhenDrained() {
        assertEquals(LIMIT, allowedOutOf(LIMIT, "user1"));

        RateLimitDecision rejected = rateLimiter.tryAcquire("user1", policy, 1);

        assertFalse(rejected.allowed());
        assertEquals(0, rejected.remaining());
        assertEquals(1, rejected.retryAfterSeconds(), "one token/second, so one second");
    }

    @Test
    void shouldRequireCostTokensToBeAvailable() {
        assertTrue(rateLimiter.tryAcquire("user1", policy, 3).allowed());
        assertFalse(rateLimiter.tryAcquire("user1", policy, 3).allowed(), "only 2 tokens left");
        assertTrue(rateLimiter.tryAcquire("user1", policy, 2).allowed());
    }

    @Test
    void shouldRejectCostLargerThanCapacityWithoutConsuming() {
        assertFalse(rateLimiter.tryAcquire("user1", policy, LIMIT + 1).allowed());

        assertEquals(LIMIT, allowedOutOf(LIMIT, "user1"), "bucket must still be full");
    }

    @Test
    void shouldNotAllowMoreThanCapacityUnderConcurrency() throws Exception {
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
