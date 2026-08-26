package com.example.ratelimiter.controller;

import com.example.ratelimiter.limiter.RateLimiter;
import com.example.ratelimiter.service.RateLimitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {
    private final RateLimitService rateLimitService;
    public TestController(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(@RequestParam String userId) {
        boolean allow = rateLimitService.allow(userId);

        if(!allow) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded");
        }
        return ResponseEntity.ok("OK");
    }
}
