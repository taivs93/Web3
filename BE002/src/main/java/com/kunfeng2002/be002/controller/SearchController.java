package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.entity.NoLombokToken;
import com.kunfeng2002.be002.service.SearchService;
import com.kunfeng2002.be002.service.RealTimeCoinCrawlerService;
import com.kunfeng2002.be002.service.CoinCrawlerBridgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SearchController {

    private final SearchService searchService;
    private final RealTimeCoinCrawlerService realTimeCoinCrawlerService;
    private final CoinCrawlerBridgeService coinCrawlerBridgeService;

    @GetMapping("/token")
    public ResponseEntity<List<NoLombokToken>> searchToken(
            @RequestParam String query,
            @RequestParam String network,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("Token search request: query={}, network={}, page={}, size={}", 
                    query, network, page, size);

            List<NoLombokToken> tokens = searchService.searchToken(query, network, page, size);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            log.error("Error in token search", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/token/realtime")
    public ResponseEntity<List<NoLombokToken>> searchTokenRealtime(
            @RequestParam String query,
            @RequestParam String network,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("Real-time token search request: query={}, network={}, page={}, size={}", 
                    query, network, page, size);

            if (!realTimeCoinCrawlerService.isCrawling()) {
                log.info("Starting real-time crawling for fresh search results");
                realTimeCoinCrawlerService.startRealTimeCrawling();
            }

            List<NoLombokToken> tokens = searchService.searchToken(query, network, page, size);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            log.error("Error in real-time token search", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/newtokens")
    public ResponseEntity<List<NoLombokToken>> getNewTokens(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("New tokens request: limit={}", limit);
            List<NoLombokToken> newTokens = searchService.getTopTokens("BSC", limit);
            return ResponseEntity.ok(newTokens);
        } catch (Exception e) {
            log.error("Error getting new tokens", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/newtokens/realtime")
    public ResponseEntity<List<NoLombokToken>> getNewTokensRealtime(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Getting {} new tokens with real-time crawling", limit);

            if (!realTimeCoinCrawlerService.isCrawling()) {
                log.info("Starting real-time crawling for new tokens");
                realTimeCoinCrawlerService.startRealTimeCrawling();
            }

            List<NoLombokToken> newTokens = searchService.getTopTokens("BSC", limit);
            return ResponseEntity.ok(newTokens);
        } catch (Exception e) {
            log.error("Error getting new tokens with real-time crawling", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/token/coincrawler")
    public ResponseEntity<List<NoLombokToken>> searchTokenWithCoincrawler(
            @RequestParam String query,
            @RequestParam(defaultValue = "BSC") String network,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Coincrawler token search: query={}, network={}, limit={}", query, network, limit);
            
            List<NoLombokToken> tokens = coinCrawlerBridgeService.searchTokensFromCoincrawler(query, network, limit);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            log.error("Error in coincrawler token search", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/newtokens/coincrawler")
    public ResponseEntity<List<NoLombokToken>> getNewTokensWithCoincrawler(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Coincrawler new tokens: limit={}", limit);
            
            List<NoLombokToken> tokens = coinCrawlerBridgeService.getNewTokensFromCoincrawler(limit);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            log.error("Error getting new tokens with coincrawler", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/toptokens/coincrawler")
    public ResponseEntity<List<NoLombokToken>> getTopTokensWithCoincrawler(
            @RequestParam(defaultValue = "BSC") String network,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Coincrawler top tokens: network={}, limit={}", network, limit);
            
            List<NoLombokToken> tokens = coinCrawlerBridgeService.getTopTokensFromCoincrawler(network, limit);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            log.error("Error getting top tokens with coincrawler", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/coincrawler/stats")
    public ResponseEntity<CoinCrawlerBridgeService.CoincrawlerStats> getCoincrawlerStats() {
        try {
            log.info("Getting coincrawler stats");
            
            CoinCrawlerBridgeService.CoincrawlerStats stats = coinCrawlerBridgeService.getCoincrawlerStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting coincrawler stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}