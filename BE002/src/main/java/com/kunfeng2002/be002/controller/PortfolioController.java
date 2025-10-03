package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.dto.request.AddTokenRequest;
import com.kunfeng2002.be002.dto.request.PortfolioRequest;
import com.kunfeng2002.be002.dto.response.PortfolioResponse;
import com.kunfeng2002.be002.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
@Slf4j
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    public ResponseEntity<PortfolioResponse> createPortfolio(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody PortfolioRequest request) {
        log.info("Creating portfolio for user ID: {}, request: {}", userId, request);
        try {
            PortfolioResponse response = portfolioService.createPortfolio(userId, request);
            log.info("Portfolio created successfully: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating portfolio for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> getUserPortfolios(
            @RequestHeader("X-User-Id") Long userId) {
        List<PortfolioResponse> portfolios = portfolioService.getUserPortfolios(userId);
        return ResponseEntity.ok(portfolios);
    }

    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponse> getPortfolio(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long portfolioId) {
        PortfolioResponse portfolio = portfolioService.getPortfolio(userId, portfolioId);
        return ResponseEntity.ok(portfolio);
    }

    @PostMapping("/tokens")
    public ResponseEntity<PortfolioResponse> addTokenToPortfolio(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AddTokenRequest request) {
        PortfolioResponse response = portfolioService.addTokenToPortfolio(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{portfolioId}/tokens/{tokenId}")
    public ResponseEntity<Void> removeTokenFromPortfolio(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long portfolioId,
            @PathVariable Long tokenId) {
        portfolioService.removeTokenFromPortfolio(userId, portfolioId, tokenId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{portfolioId}/refresh")
    public ResponseEntity<PortfolioResponse> refreshPortfolio(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long portfolioId) {
        portfolioService.updatePortfolioTokenPrices(portfolioId);
        PortfolioResponse response = portfolioService.getPortfolio(userId, portfolioId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long portfolioId) {
        portfolioService.deletePortfolio(userId, portfolioId);
        return ResponseEntity.ok().build();
    }
}
