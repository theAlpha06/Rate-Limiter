package com.example.ratelimiter.controller;

import com.example.ratelimiter.limiter.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {
    private final RateLimiter rateLimiter;

    public TestController(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(@RequestParam String userId) {
        if(!rateLimiter.allow(userId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Please try after sometime");
        }
        return ResponseEntity.ok("OK");
    }
}
