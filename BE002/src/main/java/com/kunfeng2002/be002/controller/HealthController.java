package com.kunfeng2002.be002.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "BE002 Web3 Backend is running!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "BE002 Web3 Backend");
        response.put("version", "1.0.0");
        response.put("description", "Web3 Backend with Coin Crawler Integration");
        response.put("features", new String[]{
            "Real-time Coin Crawling",
            "Token Management",
            "BSCScan API Integration",
            "RESTful APIs"
        });
        return ResponseEntity.ok(response);
    }
}
