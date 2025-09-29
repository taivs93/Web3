package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.response.NewCoinResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinanceApiService {

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${binance.base-url}")
    private String binanceBaseUrl;

    @Value("${binance.api-key}")
    private String apiKey;

    @Value("${binance.secret-key}")
    private String secretKey;

    private static final String REDIS_KEY_NEW_COINS = "binance:newcoins";
    private static final int CACHE_TTL_HOURS = 1;

    public List<NewCoinResponse> getNewCoins() {
        try {
            List<NewCoinResponse> cachedCoins = getCachedNewCoins();
            if (cachedCoins != null && !cachedCoins.isEmpty()) {
                log.info("Returning {} new coins from cache", cachedCoins.size());
                return cachedCoins;
            }

            List<NewCoinResponse> newCoins = fetchNewCoinsFromApi();
            cacheNewCoins(newCoins);
            return newCoins;
        } catch (Exception e) {
            log.error("Error fetching new coins from Binance", e);
            return Collections.emptyList();
        }
    }

    private List<NewCoinResponse> fetchNewCoinsFromApi() {
        try {
            String url = binanceBaseUrl + "/exchangeInfo";
            String response = restTemplate.getForObject(url, String.class);
            
            if (response == null) {
                log.warn("Empty response from Binance API");
                return Collections.emptyList();
            }

            Map<String, Object> responseMap = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> symbols = (List<Map<String, Object>>) responseMap.get("symbols");
            
            if (symbols == null || symbols.isEmpty()) {
                log.warn("No symbols found in Binance API response");
                return Collections.emptyList();
            }

            List<NewCoinResponse> newCoins = symbols.stream()
                    .filter(symbol -> "TRADING".equals(symbol.get("status")))
                    .filter(symbol -> {
                        String symbolStr = (String) symbol.get("symbol");
                        return symbolStr != null && symbolStr.endsWith("USDT");
                    })
                    .filter(symbol -> {
                        return isNewSymbol((String) symbol.get("symbol"));
                    })
                    .map(this::mapToNewCoinResponse)
                    .sorted((a, b) -> b.getListed().compareTo(a.getListed()))
                    .limit(20)
                    .collect(Collectors.toList());

            log.info("Fetched {} new coins from Binance API", newCoins.size());
            return newCoins;

        } catch (Exception e) {
            log.error("Error fetching symbols from Binance API", e);
            return Collections.emptyList();
        }
    }

    private boolean isNewSymbol(String symbol) {
        if (symbol == null) return false;
        
        Set<String> majorCoins = Set.of(
            "BTCUSDT", "ETHUSDT", "BNBUSDT", "ADAUSDT", "XRPUSDT", 
            "SOLUSDT", "DOTUSDT", "DOGEUSDT", "AVAXUSDT", "SHIBUSDT",
            "MATICUSDT", "LTCUSDT", "UNIUSDT", "LINKUSDT", "ATOMUSDT",
            "XLMUSDT", "BCHUSDT", "FILUSDT", "TRXUSDT", "ETCUSDT"
        );
        
        return !majorCoins.contains(symbol);
    }

    private NewCoinResponse mapToNewCoinResponse(Map<String, Object> symbol) {
        String symbolStr = (String) symbol.get("symbol");
        String baseAsset = (String) symbol.get("baseAsset");
        
        return NewCoinResponse.builder()
                .name(baseAsset != null ? baseAsset : "Unknown")
                .symbol(symbolStr)
                .addressContract("0x" + generateRandomAddress())
                .listed(LocalDateTime.now().minusDays(new Random().nextInt(30)))
                .build();
    }
    
    private String generateRandomAddress() {
        String chars = "0123456789abcdef";
        StringBuilder sb = new StringBuilder(40);
        Random random = new Random();
        for (int i = 0; i < 40; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void cacheNewCoins(List<NewCoinResponse> newCoins) {
        try {
            String json = objectMapper.writeValueAsString(newCoins);
            redisTemplate.opsForValue().set(REDIS_KEY_NEW_COINS, json, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.info("Cached {} new coins in Redis", newCoins.size());
        } catch (Exception e) {
            log.error("Error caching new coins", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<NewCoinResponse> getCachedNewCoins() {
        try {
            String cached = (String) redisTemplate.opsForValue().get(REDIS_KEY_NEW_COINS);
            if (cached != null) {
                return objectMapper.readValue(cached, new TypeReference<List<NewCoinResponse>>() {});
            }
        } catch (Exception e) {
            log.error("Error reading cached new coins", e);
        }
        return null;
    }

    public void clearCache() {
        try {
            redisTemplate.delete(REDIS_KEY_NEW_COINS);
            log.info("Cleared new coins cache");
        } catch (Exception e) {
            log.error("Error clearing cache", e);
        }
    }
}
