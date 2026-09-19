package com.example.ratelimiter.limiter;

import com.example.ratelimiter.core.*;
import com.example.ratelimiter.store.RateLimitStore;
import org.springframework.stereotype.Component;

/**
 * Tokens accrue at {@code limit / windowSizeSeconds} per second up to a ceiling of
 * {@code limit}. A request consumes {@code cost} tokens, or is refused.
 * <p>
 * The bucket starts full, so an idle key gets one full burst before settling to the
 * steady rate. That is the point of the algorithm, not a defect.
 */
@Component
public class TokenBucketRateLimiter implements RateLimiter {

    private final RateLimitStore store;
    private final TimeProvider timeProvider;

    public TokenBucketRateLimiter(RateLimitStore store, TimeProvider timeProvider) {
        this.store = store;
        this.timeProvider = timeProvider;
    }

    @Override public Algorithm algorithm() { return Algorithm.TOKEN_BUCKET; }

    @Override
    public RateLimitDecision tryAcquire(String key, RateLimitPolicy policy, int cost) {
        long   now      = timeProvider.currentTimeSeconds();
        int    limit    = policy.limit();
        double capacity = limit;
        double refill   = policy.ratePerSecond();

        RateLimitDecision[] out = new RateLimitDecision[1];

        store.compute(key, existing -> {
            TokenBucketState s = (existing instanceof TokenBucketState t)
                    ? t
                    : new TokenBucketState(capacity, now);

            long   elapsed = Math.max(0, now - s.lastRefillEpochSeconds());
            double tokens  = Math.min(capacity, s.tokens() + elapsed * refill);

            if (tokens < cost) {
                long retryAfter = (long) Math.ceil((cost - tokens) / refill);
                out[0] = RateLimitDecision.reject(limit, fullAt(now, capacity, tokens, refill), retryAfter);
                return new TokenBucketState(tokens, now);
            }

            double left = tokens - cost;
            out[0] = RateLimitDecision.allow(limit, (int) Math.floor(left), fullAt(now, capacity, left, refill));
            return new TokenBucketState(left, now);
        });

        return out[0];
    }

    /** When the bucket would next be back at capacity. */
    private static long fullAt(long now, double capacity, double tokens, double refill) {
        return now + (long) Math.ceil((capacity - tokens) / refill);
    }
}
