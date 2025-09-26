package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.entity.Coin;
import com.kunfeng2002.be002.entity.Pair;
import com.kunfeng2002.be002.entity.Token;
import com.kunfeng2002.be002.service.Web3CoinCrawlerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/web3/coins")
@RequiredArgsConstructor
@Validated
@Slf4j
public class Web3CoinCrawlerController {
    
    private final Web3CoinCrawlerService coinCrawlerService;
    
    @GetMapping
    public ResponseEntity<Page<Coin>> getAllCoins(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.debug("Getting all coins - page: {}, size: {}", page, size);
        List<Coin> coins = coinCrawlerService.getAllCoins(page, size);
        Page<Coin> coinPage = new PageImpl<>(coins, PageRequest.of(page, size), coins.size());
        return ResponseEntity.ok(coinPage);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Coin> getCoinById(
            @PathVariable Long id) {
        
        log.debug("Getting coin by ID: {}", id);
        Optional<Coin> coin = coinCrawlerService.getCoinById(id);
        return coin.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Coin>> searchCoins(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.debug("Searching coins with query: {}, page: {}, size: {}", query, page, size);
        List<Coin> coins = coinCrawlerService.searchCoins(query, page, size);
        return ResponseEntity.ok(coins);
    }
    
    @GetMapping("/top")
    public ResponseEntity<List<Coin>> getTopCoins(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting top coins - limit: {}", limit);
        List<Coin> coins = coinCrawlerService.getTopCoins(limit);
        return ResponseEntity.ok(coins);
    }
    
    @GetMapping("/new")
    public ResponseEntity<List<Coin>> getNewCoins(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting new coins - limit: {}", limit);
        List<Coin> coins = coinCrawlerService.getNewCoins(limit);
        return ResponseEntity.ok(coins);
    }
    
    @GetMapping("/trending")
    public ResponseEntity<List<Coin>> getTrendingCoins(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting trending coins - limit: {}", limit);
        List<Coin> coins = coinCrawlerService.getTrendingCoins(limit);
        return ResponseEntity.ok(coins);
    }
    
    @GetMapping("/pairs")
    public ResponseEntity<Page<Pair>> getAllPairs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.debug("Getting all pairs - page: {}, size: {}", page, size);
        List<Pair> pairs = coinCrawlerService.getAllPairs(page, size);
        Page<Pair> pairPage = new PageImpl<>(pairs, PageRequest.of(page, size), pairs.size());
        return ResponseEntity.ok(pairPage);
    }
    
    @GetMapping("/pairs/{id}")
    public ResponseEntity<Pair> getPairById(
            @PathVariable Long id) {
        
        log.debug("Getting pair by ID: {}", id);
        Optional<Pair> pair = coinCrawlerService.getPairById(id);
        return pair.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/pairs/search")
    public ResponseEntity<List<Pair>> searchPairs(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.debug("Searching pairs with query: {}, page: {}, size: {}", query, page, size);
        List<Pair> pairs = coinCrawlerService.searchPairs(query, page, size);
        return ResponseEntity.ok(pairs);
    }
    
    @GetMapping("/pairs/top")
    public ResponseEntity<List<Pair>> getTopPairs(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting top pairs - limit: {}", limit);
        List<Pair> pairs = coinCrawlerService.getTopPairs(limit);
        return ResponseEntity.ok(pairs);
    }
    
    @GetMapping("/pairs/new")
    public ResponseEntity<List<Pair>> getNewPairs(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting new pairs - limit: {}", limit);
        List<Pair> pairs = coinCrawlerService.getNewPairs(limit);
        return ResponseEntity.ok(pairs);
    }
    
    @GetMapping("/pairs/trending")
    public ResponseEntity<List<Pair>> getTrendingPairs(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting trending pairs - limit: {}", limit);
        List<Pair> pairs = coinCrawlerService.getTrendingPairs(limit);
        return ResponseEntity.ok(pairs);
    }
    
    @GetMapping("/tokens")
    public ResponseEntity<Page<Token>> getAllTokens(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.debug("Getting all tokens - page: {}, size: {}", page, size);
        List<Token> tokens = coinCrawlerService.getAllTokens(page, size);
        Page<Token> tokenPage = new PageImpl<>(tokens, PageRequest.of(page, size), tokens.size());
        return ResponseEntity.ok(tokenPage);
    }
    
    @GetMapping("/tokens/{id}")
    public ResponseEntity<Token> getTokenById(
            @PathVariable Long id) {
        
        log.debug("Getting token by ID: {}", id);
        Optional<Token> token = coinCrawlerService.getTokenById(id);
        return token.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/tokens/search")
    public ResponseEntity<List<Token>> searchTokens(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.debug("Searching tokens with query: {}, page: {}, size: {}", query, page, size);
        List<Token> tokens = coinCrawlerService.searchTokens(query, page, size);
        return ResponseEntity.ok(tokens);
    }
    
    @GetMapping("/tokens/top")
    public ResponseEntity<List<Token>> getTopTokens(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting top tokens - limit: {}", limit);
        List<Token> tokens = coinCrawlerService.getTopTokens(limit);
        return ResponseEntity.ok(tokens);
    }
    
    @GetMapping("/tokens/new")
    public ResponseEntity<List<Token>> getNewTokens(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting new tokens - limit: {}", limit);
        List<Token> tokens = coinCrawlerService.getNewTokens(limit);
        return ResponseEntity.ok(tokens);
    }
    
    @GetMapping("/tokens/trending")
    public ResponseEntity<List<Token>> getTrendingTokens(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting trending tokens - limit: {}", limit);
        List<Token> tokens = coinCrawlerService.getTrendingTokens(limit);
        return ResponseEntity.ok(tokens);
    }
    
    @PostMapping("/crawl/start")
    public ResponseEntity<String> startCrawling() {
        log.info("Starting manual crawling");
        coinCrawlerService.startCrawling();
        return ResponseEntity.ok("Crawling started");
    }
    
    @PostMapping("/crawl/stop")
    public ResponseEntity<Void> stopCrawling() {
        log.info("Stopping crawling");
        coinCrawlerService.stopCrawling();
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/crawl/status")
    public ResponseEntity<Map<String, Object>> getCrawlStatus() {
        log.debug("Getting crawl status");
        Map<String, Object> statistics = coinCrawlerService.getCrawlStats();
        return ResponseEntity.ok(statistics);
    }
    
    @PostMapping("/crawl/coins")
    public ResponseEntity<List<Coin>> crawlNewCoins(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int count) {
        
        log.info("Manually triggering crawl for {} new coins", count);
        List<Coin> coins = coinCrawlerService.crawlNewCoins(count);
        return ResponseEntity.ok(coins);
    }
    
    @PostMapping("/crawl/pairs")
    public ResponseEntity<List<Pair>> crawlNewPairs(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int count) {
        
        log.info("Manually triggering crawl for {} new pairs", count);
        List<Pair> pairs = coinCrawlerService.crawlNewPairs(count);
        return ResponseEntity.ok(pairs);
    }
    
    @PostMapping("/crawl/tokens")
    public ResponseEntity<List<Token>> crawlNewTokens(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int count) {
        
        log.info("Manually triggering crawl for {} new tokens", count);
        List<Token> tokens = coinCrawlerService.crawlNewTokens(count);
        return ResponseEntity.ok(tokens);
    }
}
