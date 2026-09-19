package com.example.ratelimiter.store;

import com.example.ratelimiter.core.RateLimitState;

import java.util.function.UnaryOperator;

public interface RateLimitStore {

    /**
     * Atomically applies {@code operator} to the state held under {@code key} and
     * stores the result. The operator receives {@code null} when no state is present.
     * <p>
     * Implementations MUST apply the operator <strong>exactly once</strong>, with no
     * other operation on {@code key} interleaving. The limiters rely on this: they
     * build their {@link com.example.ratelimiter.core.RateLimitDecision} inside the
     * operator and publish it through a captured holder, which is only safe because
     * the operator runs once, on one thread. An implementation that retries — a
     * compare-and-swap loop, for instance — would silently corrupt every decision.
     * <p>
     * The operator must be fast and free of side effects beyond that holder. It must
     * never call back into this store: implementations may hold a lock on
     * {@code key} while it runs, and re-entering can deadlock.
     *
     * @return the state that was stored
     */
    RateLimitState compute(String key, UnaryOperator<RateLimitState> operator);
}
