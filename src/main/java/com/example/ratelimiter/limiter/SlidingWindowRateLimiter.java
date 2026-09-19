package com.example.ratelimiter.limiter;

import com.example.ratelimiter.core.*;
import com.example.ratelimiter.store.RateLimitStore;
import org.springframework.stereotype.Component;
@Component
public class SlidingWindowRateLimiter implements RateLimiter {

    private final RateLimitStore store;
    private final TimeProvider timeProvider;

    public SlidingWindowRateLimiter(RateLimitStore store, TimeProvider timeProvider) {
        this.store = store;
        this.timeProvider = timeProvider;
    }

    @Override public Algorithm algorithm() { return Algorithm.SLIDING_WINDOW; }

    @Override
    public RateLimitDecision tryAcquire(String key, RateLimitPolicy policy, int cost) {
        long now   = timeProvider.currentTimeSeconds();
        long size  = policy.windowSizeSeconds();
        int  limit = policy.limit();
        long windowStart = (now / size) * size;
        long resetAt     = windowStart + size;
        double weight    = (double) (size - (now - windowStart)) / size;

        RateLimitDecision[] out = new RateLimitDecision[1];

        store.compute(key, existing -> {
            SlidingWindowState s = roll(existing, windowStart, size);
            double estimate = s.previousCount() * weight + s.currentCount();

            if (estimate + cost > limit) {
                out[0] = RateLimitDecision.reject(limit, resetAt, resetAt - now);
                return s;
            }
            SlidingWindowState updated = s.plus(cost);
            double after = updated.previousCount() * weight + updated.currentCount();
            out[0] = RateLimitDecision.allow(limit, (int) Math.floor(limit - after), resetAt);
            return updated;
        });

        return out[0];
    }

    private static SlidingWindowState roll(RateLimitState existing, long windowStart, long size) {
        if (!(existing instanceof SlidingWindowState s))    return new SlidingWindowState(windowStart, 0, 0);
        if (s.windowStart() == windowStart)                 return s;
        if (s.windowStart() + size == windowStart)          return new SlidingWindowState(windowStart, 0, s.currentCount());
        return new SlidingWindowState(windowStart, 0, 0);   // more than one window elapsed
    }
}
