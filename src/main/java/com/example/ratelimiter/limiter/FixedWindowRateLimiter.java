package com.example.ratelimiter.limiter;

import com.example.ratelimiter.core.*;
import com.example.ratelimiter.store.RateLimitStore;
import org.springframework.stereotype.Component;

@Component
public class FixedWindowRateLimiter implements RateLimiter {

    private final RateLimitStore store;
    private final TimeProvider timeProvider;

    public FixedWindowRateLimiter(RateLimitStore store, TimeProvider timeProvider) {
        this.store = store;
        this.timeProvider = timeProvider;
    }

    @Override public Algorithm algorithm() { return Algorithm.FIXED_WINDOW; }

    @Override
    public RateLimitDecision tryAcquire(String key, RateLimitPolicy policy, int cost) {
        long now   = timeProvider.currentTimeSeconds();
        long size  = policy.windowSizeSeconds();
        int  limit = policy.limit();
        long windowStart = (now / size) * size;
        long resetAt = windowStart + size;

        RateLimitDecision[] out = new RateLimitDecision[1];

        store.compute(key, existing -> {
            Window w = (existing instanceof Window win && win.windowStart() == windowStart)
                    ? win
                    : new Window(windowStart, 0);

            if ((long)w.count() + cost > limit) {
                out[0] = RateLimitDecision.reject(limit, resetAt, resetAt - now);
                return w;
            }

            Window updated = w.plus(cost);
            out[0] = RateLimitDecision.allow(limit, limit - updated.count(), resetAt);
            return updated;
        });

        return out[0];
    }
}
