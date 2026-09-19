package com.example.ratelimiter.service;

public class CostExceedsLimitException extends RuntimeException {

    private final int cost;
    private final int limit;

    public CostExceedsLimitException(int cost, int limit) {
        super("Request cost (" + cost + ") exceeds rate limit (" + limit + ")");
        this.cost = cost;
        this.limit = limit;
    }

    public int getCost() {
        return cost;
    }

    public int getLimit() {
        return limit;
    }
}