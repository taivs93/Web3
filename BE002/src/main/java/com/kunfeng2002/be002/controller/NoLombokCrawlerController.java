package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.service.NoLombokRealTimeCoinCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/nolombok/crawler")
@CrossOrigin(origins = "*")
public class NoLombokCrawlerController {
    
    @Autowired
    private NoLombokRealTimeCoinCrawlerService crawlerService;
    
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startCrawling() {
        try {
            crawlerService.startCrawling();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Crawling started successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to start crawling: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopCrawling() {
        try {
            crawlerService.stopCrawling();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Crawling stopped successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to stop crawling: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCrawlerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("isRunning", crawlerService.isRunning());
        status.put("lastProcessedBlock", crawlerService.getLastProcessedBlock());
        status.put("totalNewTokens", crawlerService.getTotalNewTokens());
        return ResponseEntity.ok(status);
    }
}
