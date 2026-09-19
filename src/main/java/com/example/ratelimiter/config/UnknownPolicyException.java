package com.example.ratelimiter.config;

import java.util.Collection;

public class UnknownPolicyException extends RuntimeException {
    public UnknownPolicyException(String name, Collection<String> known) {
        super("Unknown policy '" + name + "'. Known policies: " + known);
    }
}
