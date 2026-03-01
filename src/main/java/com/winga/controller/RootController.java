package com.winga.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Root path (optional): GET / returns 200 OK with a simple message.
 * No auth required — useful for health checks or "API is running" from load balancers.
 */
@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Winga API is running"
        ));
    }
}
