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
                gasPrice = BigInteger.valueOf(30_000_000_000L);
            }

            // Cache the result
            redisTemplate.opsForValue().set(cacheKey, gasPrice.toString(), CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            return gasPrice;

        } catch (Exception e) {
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
                gasPrice = BigInteger.valueOf(5_000_000_000L);
            }

            // Cache the result
            redisTemplate.opsForValue().set(cacheKey, gasPrice.toString(), CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            return gasPrice;

        } catch (Exception e) {
            return BigInteger.valueOf(5_000_000_000L);
        }
    }

    private BigInteger getEtherscanGasPrice() {
        try {
            if (etherscanApiKey == null || etherscanApiKey.isEmpty()) {
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
                long gasPriceGwei = result.get("ProposeGasPrice").asLong();
                BigInteger gasPriceWei = BigInteger.valueOf(gasPriceGwei).multiply(BigInteger.valueOf(1_000_000_000L));
                return gasPriceWei;
            }

        } catch (Exception e) {
        }
        
        return null;
    }

    private BigInteger getOneInchGasPrice() {
        try {
            String url = "https://gas-price-api.1inch.io/v1.4/1";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            long gasPriceWei = jsonNode.get("standard").asLong();
            BigInteger gasPrice = BigInteger.valueOf(gasPriceWei);
            return gasPrice;

        } catch (Exception e) {
        }
        
        return null;
    }

    private BigInteger getBscGasStationPrice() {
        try {
            String url = String.format(
                "https://api.bscscan.com/api?module=proxy&action=eth_gasPrice&apikey=%s",
                bscscanApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("result")) {
                String gasPriceHex = jsonNode.get("result").asText();
                BigInteger gasPrice = new BigInteger(gasPriceHex.substring(2), 16);
                return gasPrice;
            }

        } catch (Exception e) {
        }
        
        return null;
    }

    public void clearCache() {
        try {
            redisTemplate.delete(CACHE_KEY_PREFIX + "ethereum");
            redisTemplate.delete(CACHE_KEY_PREFIX + "bsc");
        } catch (Exception e) {
        }
    }
}
