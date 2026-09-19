package com.example.ratelimiter.api;

import com.example.ratelimiter.service.RateLimitService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class RateLimitController {

    private final RateLimitService service;

    public RateLimitController(RateLimitService service) { this.service = service; }

    @PostMapping("/check")
    public CheckResponse check(@RequestBody CheckRequest request) {
        if (request.policy() == null || request.policy().isBlank())
            throw new IllegalArgumentException("'policy' is required");
        if (request.key() == null || request.key().isBlank())
            throw new IllegalArgumentException("'key' is required");

        int cost = request.cost() == null ? 1 : request.cost();
        if (cost < 1) throw new IllegalArgumentException("'cost' must be >= 1");

        return CheckResponse.from(service.check(request.policy(), request.key(), cost));
    }
}
