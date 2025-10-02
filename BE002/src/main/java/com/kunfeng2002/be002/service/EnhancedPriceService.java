package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedPriceService {

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final TokenPriceService tokenPriceService; // Fallback to existing service

    @Value("${moralis.api-key:}")
    private String moralisApiKey;

    @Value("${debank.access-key:}")
    private String debankAccessKey;

    private static final String CACHE_KEY_PREFIX = "enhanced_price:";
    private static final int CACHE_TTL_MINUTES = 2; // 2 minutes cache

    public Map<String, BigDecimal> getEnhancedTokenPrices(String[] symbols) {
        Map<String, BigDecimal> prices = new HashMap<>();
        
        // Try multiple sources for better reliability
        Map<String, BigDecimal> moralisPrices = getMoralisPrices(symbols);
        Map<String, BigDecimal> debankPrices = getDebankPrices(symbols);
        Map<String, BigDecimal> fallbackPrices = tokenPriceService.getTokenPrices(symbols);

        for (String symbol : symbols) {
            BigDecimal price = null;
            
            // Priority: Moralis > DeBank > CoinGecko (existing)
            if (moralisPrices.containsKey(symbol) && moralisPrices.get(symbol).compareTo(BigDecimal.ZERO) > 0) {
                price = moralisPrices.get(symbol);
                log.debug("Using Moralis price for {}: {}", symbol, price);
            } else if (debankPrices.containsKey(symbol) && debankPrices.get(symbol).compareTo(BigDecimal.ZERO) > 0) {
                price = debankPrices.get(symbol);
                log.debug("Using DeBank price for {}: {}", symbol, price);
            } else if (fallbackPrices.containsKey(symbol) && fallbackPrices.get(symbol).compareTo(BigDecimal.ZERO) > 0) {
                price = fallbackPrices.get(symbol);
                log.debug("Using CoinGecko price for {}: {}", symbol, price);
            }
            
            if (price != null) {
                prices.put(symbol, price);
                // Cache individual price
                cachePrice(symbol, price);
            } else {
                log.warn("No price found for symbol: {}", symbol);
            }
        }

        return prices;
    }

    public BigDecimal getEnhancedTokenPrice(String symbol) {
        // Check cache first
        BigDecimal cachedPrice = getCachedPrice(symbol);
        if (cachedPrice != null) {
            return cachedPrice;
        }

        Map<String, BigDecimal> prices = getEnhancedTokenPrices(new String[]{symbol});
        return prices.getOrDefault(symbol, BigDecimal.ZERO);
    }

    private Map<String, BigDecimal> getMoralisPrices(String[] symbols) {
        Map<String, BigDecimal> prices = new HashMap<>();
        
        if (moralisApiKey == null || moralisApiKey.isEmpty()) {
            log.debug("Moralis API key not configured");
            return prices;
        }

        try {
            for (String symbol : symbols) {
                // Moralis requires token addresses, not symbols
                // This is a simplified implementation
                String tokenAddress = getTokenAddressForSymbol(symbol);
                if (tokenAddress != null) {
                    BigDecimal price = getMoralisTokenPrice(tokenAddress);
                    if (price != null) {
                        prices.put(symbol, price);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error fetching Moralis prices: {}", e.getMessage());
        }

        return prices;
    }

    private BigDecimal getMoralisTokenPrice(String tokenAddress) {
        try {
            String url = String.format(
                "https://deep-index.moralis.io/api/v2/erc20/%s/price?chain=eth",
                tokenAddress
            );

            // Add Moralis API key to headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-API-Key", moralisApiKey);
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            String response = restTemplate.exchange(
                url, 
                org.springframework.http.HttpMethod.GET, 
                entity, 
                String.class
            ).getBody();

            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.has("usdPrice")) {
                return new BigDecimal(jsonNode.get("usdPrice").asText());
            }

        } catch (Exception e) {
            log.debug("Failed to get Moralis price for {}: {}", tokenAddress, e.getMessage());
        }
        
        return null;
    }

    private Map<String, BigDecimal> getDebankPrices(String[] symbols) {
        Map<String, BigDecimal> prices = new HashMap<>();
        
        if (debankAccessKey == null || debankAccessKey.isEmpty()) {
            log.debug("DeBank Access Key not configured");
            return prices;
        }

        try {
            // DeBank API example implementation
            for (String symbol : symbols) {
                BigDecimal price = getDebankTokenPrice(symbol);
                if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                    prices.put(symbol, price);
                }
            }
        } catch (Exception e) {
            log.warn("Error fetching DeBank prices: {}", e.getMessage());
        }

        return prices;
    }

    private BigDecimal getDebankTokenPrice(String symbol) {
        try {
            // DeBank API endpoint for token price
            String tokenAddress = getTokenAddressForSymbol(symbol);
            if (tokenAddress == null) {
                return null;
            }

            String url = String.format(
                "https://pro-openapi.debank.com/v1/token?chain_id=eth&id=%s",
                tokenAddress
            );

            // Add DeBank Access Key to headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("AccessKey", debankAccessKey);
            headers.set("Content-Type", "application/json");
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            String response = restTemplate.exchange(
                url, 
                org.springframework.http.HttpMethod.GET, 
                entity, 
                String.class
            ).getBody();

            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.has("price")) {
                return new BigDecimal(jsonNode.get("price").asText());
            }

        } catch (Exception e) {
            log.debug("Failed to get DeBank price for {}: {}", symbol, e.getMessage());
        }
        
        return null;
    }

    private String getTokenAddressForSymbol(String symbol) {
        // Common token addresses for major tokens
        Map<String, String> symbolToAddress = new HashMap<>();
        symbolToAddress.put("USDT", "0xdAC17F958D2ee523a2206206994597C13D831ec7");
        symbolToAddress.put("USDC", "0xA0b86a33E6441c8C06DD2b7c8C3e9F8F8C2A1e2E");
        symbolToAddress.put("WETH", "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2");
        symbolToAddress.put("UNI", "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984");
        symbolToAddress.put("LINK", "0x514910771AF9Ca656af840dff83E8264EcF986CA");
        
        return symbolToAddress.get(symbol.toUpperCase());
    }

    private void cachePrice(String symbol, BigDecimal price) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + symbol.toLowerCase();
            redisTemplate.opsForValue().set(cacheKey, price.toString(), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.debug("Failed to cache price for {}: {}", symbol, e.getMessage());
        }
    }

    private BigDecimal getCachedPrice(String symbol) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + symbol.toLowerCase();
            String cached = (String) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return new BigDecimal(cached);
            }
        } catch (Exception e) {
            log.debug("Failed to get cached price for {}: {}", symbol, e.getMessage());
        }
        
        return null;
    }

    public void clearPriceCache() {
        try {
            // Clear all enhanced price cache entries
            redisTemplate.delete(redisTemplate.keys(CACHE_KEY_PREFIX + "*"));
            log.info("Enhanced price cache cleared");
        } catch (Exception e) {
            log.error("Error clearing enhanced price cache", e);
        }
    }
}
