package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.dto.response.ResponseDTO;
import com.kunfeng2002.be002.service.EnhancedPriceService;
import com.kunfeng2002.be002.service.GasTrackerService;
import com.kunfeng2002.be002.service.PortfolioPriceScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final PortfolioPriceScheduler portfolioPriceScheduler;
    private final GasTrackerService gasTrackerService;
    private final EnhancedPriceService enhancedPriceService;

    @PostMapping("/portfolio/update-prices")
    public ResponseEntity<ResponseDTO> triggerPortfolioPriceUpdate() {
        try {
            portfolioPriceScheduler.triggerManualUpdate();
            
            return ResponseEntity.ok(ResponseDTO.builder()
                    .status(200)
                    .message("Portfolio price update triggered successfully")
                    .data("Manual update completed")
                    .build());
                    
        } catch (Exception e) {
            log.error("Error triggering portfolio price update", e);
            return ResponseEntity.badRequest().body(ResponseDTO.builder()
                    .status(400)
                    .message("Failed to trigger portfolio price update: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/gas/enhanced/{network}")
    public ResponseEntity<ResponseDTO> getEnhancedGasPrice(@PathVariable String network) {
        try {
            BigInteger gasPrice;
            
            switch (network.toUpperCase()) {
                case "ETHEREUM":
                case "ETH":
                    gasPrice = gasTrackerService.getEthereumGasPrice();
                    break;
                case "BSC":
                case "BINANCE":
                    gasPrice = gasTrackerService.getBscGasPrice();
                    break;
                default:
                    return ResponseEntity.badRequest().body(ResponseDTO.builder()
                            .status(400)
                            .message("Unsupported network: " + network)
                            .data(null)
                            .build());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("network", network.toUpperCase());
            result.put("gasPriceWei", gasPrice.toString());
            result.put("gasPriceGwei", gasPrice.divide(BigInteger.valueOf(1_000_000_000)).toString());

            return ResponseEntity.ok(ResponseDTO.builder()
                    .status(200)
                    .message("Enhanced gas price retrieved successfully")
                    .data(result)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error getting enhanced gas price for network: {}", network, e);
            return ResponseEntity.badRequest().body(ResponseDTO.builder()
                    .status(400)
                    .message("Failed to get enhanced gas price: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/price/enhanced")
    public ResponseEntity<ResponseDTO> getEnhancedTokenPrices(@RequestParam String[] symbols) {
        try {
            Map<String, BigDecimal> prices = enhancedPriceService.getEnhancedTokenPrices(symbols);
            
            return ResponseEntity.ok(ResponseDTO.builder()
                    .status(200)
                    .message("Enhanced token prices retrieved successfully")
                    .data(prices)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error getting enhanced token prices", e);
            return ResponseEntity.badRequest().body(ResponseDTO.builder()
                    .status(400)
                    .message("Failed to get enhanced token prices: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @PostMapping("/cache/clear")
    public ResponseEntity<ResponseDTO> clearAllCaches() {
        try {
            gasTrackerService.clearCache();
            enhancedPriceService.clearPriceCache();
            
            return ResponseEntity.ok(ResponseDTO.builder()
                    .status(200)
                    .message("All caches cleared successfully")
                    .data("Gas tracker and enhanced price caches cleared")
                    .build());
                    
        } catch (Exception e) {
            log.error("Error clearing caches", e);
            return ResponseEntity.badRequest().body(ResponseDTO.builder()
                    .status(400)
                    .message("Failed to clear caches: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/health/services")
    public ResponseEntity<ResponseDTO> checkServicesHealth() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        try {
            // Test gas tracker
            BigInteger ethGas = gasTrackerService.getEthereumGasPrice();
            healthStatus.put("gasTracker", ethGas != null ? "OK" : "ERROR");
            
            // Test enhanced price service
            BigDecimal btcPrice = enhancedPriceService.getEnhancedTokenPrice("BTC");
            healthStatus.put("enhancedPrice", btcPrice.compareTo(BigDecimal.ZERO) > 0 ? "OK" : "ERROR");
            
            // Test portfolio scheduler
            healthStatus.put("portfolioScheduler", "OK"); // Always OK if no exception
            
            return ResponseEntity.ok(ResponseDTO.builder()
                    .status(200)
                    .message("Services health check completed")
                    .data(healthStatus)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error during health check", e);
            healthStatus.put("error", e.getMessage());
            
            return ResponseEntity.ok(ResponseDTO.builder()
                    .status(200)
                    .message("Health check completed with errors")
                    .data(healthStatus)
                    .build());
        }
    }
}
