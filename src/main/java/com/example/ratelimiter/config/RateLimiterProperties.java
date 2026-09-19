package com.example.ratelimiter.config;

import com.example.ratelimiter.core.Algorithm;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    private Map<String, PolicyProperties> policies = new LinkedHashMap<>();

    public Map<String, PolicyProperties> getPolicies() { return policies; }
    public void setPolicies(Map<String, PolicyProperties> policies) { this.policies = policies; }

    public static class PolicyProperties {
        private Algorithm algorithm;
        private int limit;
        private long windowSizeSeconds;

        public Algorithm getAlgorithm() {
            return  algorithm;
        }

        public int getLimit() {
            return limit;
        }

        public long getWindowSizeSeconds() {
            return windowSizeSeconds;
        }

        public void setAlgorithm(Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public void setWindowSizeSeconds(long windowSizeSeconds) {
            this.windowSizeSeconds = windowSizeSeconds;
        }
    }
}