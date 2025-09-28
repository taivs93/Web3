package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoPriceService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Double> fetchAllPricesFromAPI() {
        Map<String, Double> prices = new HashMap<>();
        try {
            String url = "https://api.coingecko.com/api/v3/simple/price?ids=ethereum,binancecoin,avalanche-2&vs_currencies=usd";
            String json = restTemplate.getForObject(url, String.class);
            JsonNode arr = objectMapper.readTree(json);

            for (JsonNode node : arr) {
                String symbol = node.get("symbol").asText();
                if (symbol.equalsIgnoreCase("BTCUSDT")) {
                    prices.put("btc", node.get("price").asDouble());
                } else if (symbol.equalsIgnoreCase("ETHUSDT")) {
                    prices.put("eth", node.get("price").asDouble());
                } else if (symbol.equalsIgnoreCase("SOLUSDT")) {
                    prices.put("sol", node.get("price").asDouble());
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch prices from Binance", e);
        }
        return prices;
    }
}
