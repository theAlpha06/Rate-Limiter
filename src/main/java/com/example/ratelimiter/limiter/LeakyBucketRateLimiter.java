package com.example.ratelimiter.limiter;

import com.example.ratelimiter.core.*;
import com.example.ratelimiter.store.RateLimitStore;
import org.springframework.stereotype.Component;

/**
 * Each request raises the bucket level by {@code cost}; the level drains at
 * {@code limit / windowSizeSeconds} per second. A request that would overflow
 * {@code limit} is refused.
 * <p>
 * This is the <em>meter</em> formulation. Admission behaviour is the mirror of
 * {@link TokenBucketRateLimiter} — the only observable difference is that this
 * bucket starts empty rather than full. A true queueing leaky bucket, which
 * releases requests at a constant rate, needs a scheduler and cannot be expressed
 * by a call that must return a verdict immediately.
 */
@Component
public class LeakyBucketRateLimiter implements RateLimiter {

    private final RateLimitStore store;
    private final TimeProvider timeProvider;

    public LeakyBucketRateLimiter(RateLimitStore store, TimeProvider timeProvider) {
        this.store = store;
        this.timeProvider = timeProvider;
    }

    @Override public Algorithm algorithm() { return Algorithm.LEAKY_BUCKET; }

    @Override
    public RateLimitDecision tryAcquire(String key, RateLimitPolicy policy, int cost) {
        long   now      = timeProvider.currentTimeSeconds();
        int    limit    = policy.limit();
        double capacity = limit;
        double leak     = policy.ratePerSecond();

        RateLimitDecision[] out = new RateLimitDecision[1];

        store.compute(key, existing -> {
            LeakyBucketState s = (existing instanceof LeakyBucketState l)
                    ? l
                    : new LeakyBucketState(0, now);

            long   elapsed = Math.max(0, now - s.lastLeakEpochSeconds());
            double level   = Math.max(0, s.level() - elapsed * leak);

            if (level + cost > capacity) {
                long retryAfter = (long) Math.ceil((level + cost - capacity) / leak);
                out[0] = RateLimitDecision.reject(limit, drainedAt(now, level, leak), retryAfter);
                return new LeakyBucketState(level, now);
            }

            double raised = level + cost;
            out[0] = RateLimitDecision.allow(limit, (int) Math.floor(capacity - raised), drainedAt(now, raised, leak));
            return new LeakyBucketState(raised, now);
        });

        return out[0];
    }

    /** When the bucket would next be empty. */
    private static long drainedAt(long now, double level, double leak) {
        return now + (long) Math.ceil(level / leak);
    }
}
