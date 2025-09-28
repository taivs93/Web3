package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TokenPriceService {

    @Autowired
    private RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${app.coingecko.api-key:}")
    private String coingeckoApiKey;

    public Map<String, BigDecimal> getTokenPrices(String[] symbols) {
        Map<String, BigDecimal> prices = new HashMap<>();
        
        try {
            String symbolsParam = String.join(",", symbols);
            String url = String.format(
                "https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=usd&include_24hr_change=true",
                symbolsParam
            );
            
            if (!coingeckoApiKey.isEmpty()) {
                url += "&x_cg_demo_api_key=" + coingeckoApiKey;
            }
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);
            
            for (String symbol : symbols) {
                String coinId = getCoinGeckoId(symbol);
                if (jsonNode.has(coinId)) {
                    JsonNode coinData = jsonNode.get(coinId);
                    if (coinData.has("usd")) {
                        BigDecimal price = new BigDecimal(coinData.get("usd").asText());
                        prices.put(symbol, price);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error fetching token prices: {}", e.getMessage());
        }
        
        return prices;
    }

    public BigDecimal getTokenPrice(String symbol) {
        Map<String, BigDecimal> prices = getTokenPrices(new String[]{symbol});
        return prices.getOrDefault(symbol, BigDecimal.ZERO);
    }

    public Map<String, TokenPriceData> getDetailedTokenData(String[] symbols) {
        Map<String, TokenPriceData> tokenData = new HashMap<>();
        
        try {
            String symbolsParam = String.join(",", symbols);
            String url = String.format(
                "https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=usd&include_24hr_change=true&include_market_cap=true&include_24hr_vol=true",
                symbolsParam
            );
            
            if (!coingeckoApiKey.isEmpty()) {
                url += "&x_cg_demo_api_key=" + coingeckoApiKey;
            }
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);
            
            for (String symbol : symbols) {
                String coinId = getCoinGeckoId(symbol);
                if (jsonNode.has(coinId)) {
                    JsonNode coinData = jsonNode.get(coinId);
                    TokenPriceData data = TokenPriceData.builder()
                        .symbol(symbol)
                        .price(new BigDecimal(coinData.get("usd").asText()))
                        .priceChange24h(coinData.has("usd_24h_change") ? 
                            new BigDecimal(coinData.get("usd_24h_change").asText()) : BigDecimal.ZERO)
                        .marketCap(coinData.has("usd_market_cap") ? 
                            new BigDecimal(coinData.get("usd_market_cap").asText()) : BigDecimal.ZERO)
                        .volume24h(coinData.has("usd_24h_vol") ? 
                            new BigDecimal(coinData.get("usd_24h_vol").asText()) : BigDecimal.ZERO)
                        .build();
                    tokenData.put(symbol, data);
                }
            }
            
        } catch (Exception e) {
            log.error("Error fetching detailed token data: {}", e.getMessage());
        }
        
        return tokenData;
    }

    private String getCoinGeckoId(String symbol) {
        Map<String, String> symbolToId = new HashMap<>();
        symbolToId.put("BTC", "bitcoin");
        symbolToId.put("ETH", "ethereum");
        symbolToId.put("BNB", "binancecoin");
        symbolToId.put("USDT", "tether");
        symbolToId.put("USDC", "usd-coin");
        symbolToId.put("AVAX", "avalanche-2");
        symbolToId.put("MATIC", "matic-network");
        symbolToId.put("ADA", "cardano");
        symbolToId.put("SOL", "solana");
        symbolToId.put("DOT", "polkadot");
        symbolToId.put("LINK", "chainlink");
        return symbolToId.getOrDefault(symbol.toUpperCase(), symbol.toLowerCase());
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenPriceData {
        private String symbol;
        private BigDecimal price;
        private BigDecimal priceChange24h;
        private BigDecimal marketCap;
        private BigDecimal volume24h;
    }
}
