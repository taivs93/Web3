package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GasTrackerService {

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${etherscan.api-key:}")
    private String etherscanApiKey;

    @Value("${bscscan.api-key}")
    private String bscscanApiKey;

    private static final String CACHE_KEY_PREFIX = "gas_tracker:";
    private static final int CACHE_TTL_SECONDS = 30; // 30 seconds cache

    public BigInteger getEthereumGasPrice() {
        String cacheKey = CACHE_KEY_PREFIX + "ethereum";
        
        try {
            String cached = (String) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return new BigInteger(cached);
            }

            // Try Etherscan Gas Tracker first
            BigInteger gasPrice = getEtherscanGasPrice();
            
            // Fallback to 1inch Gas API
            if (gasPrice == null) {
                gasPrice = getOneInchGasPrice();
            }
            
            // Final fallback
            if (gasPrice == null) {
                gasPrice = BigInteger.valueOf(30_000_000_000L); // 30 Gwei
                log.warn("Using fallback Ethereum gas price: {} Gwei", gasPrice.divide(BigInteger.valueOf(1_000_000_000)));
            }

            // Cache the result
            redisTemplate.opsForValue().set(cacheKey, gasPrice.toString(), CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            return gasPrice;

        } catch (Exception e) {
            log.error("Error getting Ethereum gas price", e);
            return BigInteger.valueOf(30_000_000_000L);
        }
    }

    public BigInteger getBscGasPrice() {
        String cacheKey = CACHE_KEY_PREFIX + "bsc";
        
        try {
            String cached = (String) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return new BigInteger(cached);
            }

            // Try BSC Gas Station
            BigInteger gasPrice = getBscGasStationPrice();
            
            // Fallback
            if (gasPrice == null) {
                gasPrice = BigInteger.valueOf(5_000_000_000L); // 5 Gwei
                log.warn("Using fallback BSC gas price: {} Gwei", gasPrice.divide(BigInteger.valueOf(1_000_000_000)));
            }

            // Cache the result
            redisTemplate.opsForValue().set(cacheKey, gasPrice.toString(), CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            return gasPrice;

        } catch (Exception e) {
            log.error("Error getting BSC gas price", e);
            return BigInteger.valueOf(5_000_000_000L);
        }
    }

    private BigInteger getEtherscanGasPrice() {
        try {
            if (etherscanApiKey == null || etherscanApiKey.isEmpty()) {
                log.debug("Etherscan API key not configured");
                return null;
            }

            String url = String.format(
                "https://api.etherscan.io/api?module=gastracker&action=gasoracle&apikey=%s",
                etherscanApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            if ("1".equals(jsonNode.get("status").asText())) {
                JsonNode result = jsonNode.get("result");
                // Get standard gas price in Gwei and convert to Wei
                long gasPriceGwei = result.get("ProposeGasPrice").asLong();
                BigInteger gasPriceWei = BigInteger.valueOf(gasPriceGwei).multiply(BigInteger.valueOf(1_000_000_000L));
                
                log.info("Etherscan gas price: {} Gwei", gasPriceGwei);
                return gasPriceWei;
            }

        } catch (Exception e) {
            log.warn("Failed to get Etherscan gas price: {}", e.getMessage());
        }
        
        return null;
    }

    private BigInteger getOneInchGasPrice() {
        try {
            String url = "https://gas-price-api.1inch.io/v1.4/1";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            // Get standard gas price
            long gasPriceWei = jsonNode.get("standard").asLong();
            BigInteger gasPrice = BigInteger.valueOf(gasPriceWei);
            
            log.info("1inch gas price: {} Gwei", gasPrice.divide(BigInteger.valueOf(1_000_000_000)));
            return gasPrice;

        } catch (Exception e) {
            log.warn("Failed to get 1inch gas price: {}", e.getMessage());
        }
        
        return null;
    }

    private BigInteger getBscGasStationPrice() {
        try {
            // BSC doesn't have a reliable gas station API like Ethereum
            // We can use BSCScan API for current gas price
            String url = String.format(
                "https://api.bscscan.com/api?module=proxy&action=eth_gasPrice&apikey=%s",
                bscscanApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("result")) {
                String gasPriceHex = jsonNode.get("result").asText();
                BigInteger gasPrice = new BigInteger(gasPriceHex.substring(2), 16);
                
                log.info("BSCScan gas price: {} Gwei", gasPrice.divide(BigInteger.valueOf(1_000_000_000)));
                return gasPrice;
            }

        } catch (Exception e) {
            log.warn("Failed to get BSCScan gas price: {}", e.getMessage());
        }
        
        return null;
    }

    public void clearCache() {
        try {
            redisTemplate.delete(CACHE_KEY_PREFIX + "ethereum");
            redisTemplate.delete(CACHE_KEY_PREFIX + "bsc");
            log.info("Gas tracker cache cleared");
        } catch (Exception e) {
            log.error("Error clearing gas tracker cache", e);
        }
    }
}
