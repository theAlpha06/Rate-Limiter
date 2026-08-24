package com.example.ratelimiter.config;

import com.example.ratelimiter.core.Algorithm;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    private Algorithm algorithm;
    private int limit;
    private long windowSizeSeconds;

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getWindowSizeSeconds() {
        return windowSizeSeconds;
    }

    public void setWindowSizeSeconds(long windowSizeSeconds) {
        this.windowSizeSeconds = windowSizeSeconds;
    }
}