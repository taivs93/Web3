package com.kunfeng2002.web3.coincrawler.controller;

import com.kunfeng2002.web3.coincrawler.entity.Coin;
import com.kunfeng2002.web3.coincrawler.entity.Pair;
import com.kunfeng2002.web3.coincrawler.entity.Token;
import com.kunfeng2002.web3.coincrawler.model.CrawlStatistics;
import com.kunfeng2002.web3.coincrawler.model.SearchCriteria;
import com.kunfeng2002.web3.coincrawler.service.Web3CoinCrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    
    @GetMapping("/address/{address}")
    public ResponseEntity<Coin> getCoinByAddress(
            @PathVariable String address) {
        
        log.debug("Getting coin by address: {}", address);
        Optional<Coin> coin = coinCrawlerService.getCoinByAddress(address);
        return coin.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/search")
    public ResponseEntity<List<Coin>> searchCoins(
            @Valid @RequestBody SearchCriteria criteria) {
        
        log.debug("Searching coins with criteria: {}", criteria);
        List<Coin> coins = coinCrawlerService.searchCoins(criteria);
        return ResponseEntity.ok(coins);
    }
    
    @GetMapping("/letter/{letter}")
    public ResponseEntity<List<Coin>> getCoinsByLetter(
            @PathVariable String letter,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.debug("Getting coins by letter: {} - page: {}, size: {}", letter, page, size);
        List<Coin> coins = coinCrawlerService.getCoinsByLetter(letter, page, size);
        return ResponseEntity.ok(coins);
    }
    
    @GetMapping("/top-volume")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved top coins"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Coin>> getTopCoinsByVolume(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting top coins by volume - limit: {}", limit);
        List<Coin> coins = coinCrawlerService.getTopCoinsByVolume(limit);
        return ResponseEntity.ok(coins);
    }
    
    @GetMapping("/new")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved new coins"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Coin>> getNewCoins(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting new coins - limit: {}", limit);
        List<Coin> coins = coinCrawlerService.getNewCoins(limit);
        return ResponseEntity.ok(coins);
    }
    
    @GetMapping("/trending")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved trending coins"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    
    @GetMapping("/pairs/address/{address}")
    public ResponseEntity<Pair> getPairByAddress(
            @PathVariable String address) {
        
        log.debug("Getting pair by address: {}", address);
        Optional<Pair> pair = coinCrawlerService.getPairByAddress(address);
        return pair.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/pairs/token/{tokenId}")
    public ResponseEntity<List<Pair>> getPairsByTokenId(
            @PathVariable Long tokenId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.debug("Getting pairs by token ID: {} - page: {}, size: {}", tokenId, page, size);
        List<Pair> pairs = coinCrawlerService.getPairsByTokenId(tokenId, page, size);
        return ResponseEntity.ok(pairs);
    }
    
    @GetMapping("/pairs/top-volume")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved top pairs"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Pair>> getTopPairsByVolume(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting top pairs by volume - limit: {}", limit);
        List<Pair> pairs = coinCrawlerService.getTopPairsByVolume(limit);
        return ResponseEntity.ok(pairs);
    }
    
    @GetMapping("/pairs/new")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved new pairs"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Pair>> getNewPairs(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting new pairs - limit: {}", limit);
        List<Pair> pairs = coinCrawlerService.getNewPairs(limit);
        return ResponseEntity.ok(pairs);
    }
    
    @GetMapping("/pairs/trending")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved trending pairs"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    
    @GetMapping("/tokens/address/{address}")
    public ResponseEntity<Token> getTokenByAddress(
            @PathVariable String address) {
        
        log.debug("Getting token by address: {}", address);
        Optional<Token> token = coinCrawlerService.getTokenByAddress(address);
        return token.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/tokens/search")
    public ResponseEntity<List<Token>> searchTokens(
            @Valid @RequestBody SearchCriteria criteria) {
        
        log.debug("Searching tokens with criteria: {}", criteria);
        List<Token> tokens = coinCrawlerService.searchTokens(criteria);
        return ResponseEntity.ok(tokens);
    }
    
    @GetMapping("/tokens/letter/{letter}")
    public ResponseEntity<List<Token>> getTokensByLetter(
            @PathVariable String letter,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.debug("Getting tokens by letter: {} - page: {}, size: {}", letter, page, size);
        List<Token> tokens = coinCrawlerService.getTokensByLetter(letter, page, size);
        return ResponseEntity.ok(tokens);
    }
    
    @GetMapping("/tokens/top-volume")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved top tokens"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Token>> getTopTokensByVolume(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting top tokens by volume - limit: {}", limit);
        List<Token> tokens = coinCrawlerService.getTopTokensByVolume(limit);
        return ResponseEntity.ok(tokens);
    }
    
    @GetMapping("/tokens/new")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved new tokens"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Token>> getNewTokens(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting new tokens - limit: {}", limit);
        List<Token> tokens = coinCrawlerService.getNewTokens(limit);
        return ResponseEntity.ok(tokens);
    }
    
    @GetMapping("/tokens/trending")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved trending tokens"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Token>> getTrendingTokens(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.debug("Getting trending tokens - limit: {}", limit);
        List<Token> tokens = coinCrawlerService.getTrendingTokens(limit);
        return ResponseEntity.ok(tokens);
    }
    
    
    @PostMapping("/crawl/start")
    public ResponseEntity<CrawlStatistics> startCrawling() {
        log.info("Starting manual crawling");
        CrawlStatistics statistics = coinCrawlerService.startCrawling();
        return ResponseEntity.ok(statistics);
    }
    
    @PostMapping("/crawl/stop")
    public ResponseEntity<Void> stopCrawling() {
        log.info("Stopping crawling");
        coinCrawlerService.stopCrawling();
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/crawl/status")
    public ResponseEntity<CrawlStatistics> getCrawlStatus() {
        log.debug("Getting crawl status");
        CrawlStatistics statistics = coinCrawlerService.getCrawlStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/letter")
    public ResponseEntity<Map<String, Long>> getStatisticsByLetter() {
        log.debug("Getting statistics by letter");
        Map<String, Long> statistics = coinCrawlerService.getStatisticsByLetter();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/category")
    public ResponseEntity<Map<String, Long>> getStatisticsByCategory() {
        log.debug("Getting statistics by category");
        Map<String, Long> statistics = coinCrawlerService.getStatisticsByCategory();
        return ResponseEntity.ok(statistics);
    }
    
    
    @PostMapping("/refresh/{id}")
    public ResponseEntity<Coin> refreshCoinData(
            @PathVariable Long id) {
        
        log.info("Refreshing coin data for ID: {}", id);
        Optional<Coin> coin = coinCrawlerService.refreshCoinData(id);
        return coin.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/pairs/refresh/{id}")
    public ResponseEntity<Pair> refreshPairData(
            @PathVariable Long id) {
        
        log.info("Refreshing pair data for ID: {}", id);
        Optional<Pair> pair = coinCrawlerService.refreshPairData(id);
        return pair.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/tokens/refresh/{id}")
    public ResponseEntity<Token> refreshTokenData(
            @PathVariable Long id) {
        
        log.info("Refreshing token data for ID: {}", id);
        Optional<Token> token = coinCrawlerService.refreshTokenData(id);
        return token.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldData() {
        log.info("Starting data cleanup");
        int cleanedCount = coinCrawlerService.cleanupOldData();
        return ResponseEntity.ok(Map.of("cleanedCount", cleanedCount));
    }
    
    @GetMapping("/export/{type}")
    public ResponseEntity<String> exportToCsv(
            @PathVariable String type,
            @RequestBody(required = false) SearchCriteria criteria) {
        
        log.info("Exporting {} data to CSV", type);
        String csvContent = coinCrawlerService.exportToCsv(type, criteria);
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=" + type + ".csv")
                .body(csvContent);
    }
}
