package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.service.RealTimeCoinCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CoinCrawlerController {

    private final RealTimeCoinCrawlerService realTimeCoinCrawlerService;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startCrawling() {
        try {
            log.info("Starting real-time coin crawling via API");
            realTimeCoinCrawlerService.startRealTimeCrawling();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Real-time coin crawling started",
                "isRunning", realTimeCoinCrawlerService.isCrawling()
            ));
        } catch (Exception e) {
            log.error("Error starting real-time crawling", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error starting crawling: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopCrawling() {
        try {
            log.info("Stopping real-time coin crawling via API");
            realTimeCoinCrawlerService.stopRealTimeCrawling();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Real-time coin crawling stopped",
                "isRunning", realTimeCoinCrawlerService.isCrawling()
            ));
        } catch (Exception e) {
            log.error("Error stopping real-time crawling", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error stopping crawling: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        try {
            RealTimeCoinCrawlerService.CrawlStats stats = realTimeCoinCrawlerService.getCrawlStats();
            
            return ResponseEntity.ok(Map.of(
                "isRunning", stats.isRunning(),
                "lastProcessedBlock", stats.getLastProcessedBlock(),
                "totalNewTokens", stats.getTotalNewTokens(),
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("Error getting crawling status", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error getting status: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/crawl-now")
    public ResponseEntity<Map<String, Object>> crawlNow() {
        try {
            log.info("Manual crawl triggered via API");
            realTimeCoinCrawlerService.crawlNewTokensAsync();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Manual crawl triggered",
                "isRunning", realTimeCoinCrawlerService.isCrawling()
            ));
        } catch (Exception e) {
            log.error("Error in manual crawl", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error in manual crawl: " + e.getMessage()
            ));
        }
    }
}
