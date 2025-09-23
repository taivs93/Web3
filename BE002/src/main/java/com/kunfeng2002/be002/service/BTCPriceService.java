package com.kunfeng2002.be002.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
@Slf4j
public class BTCPriceService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String COINGECKO_API = "https://api.coingecko.com/api/v3/simple/price";

    public double fetchBTCPriceFromAPI() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(COINGECKO_API)
                    .queryParam("ids", "bitcoin")
                    .queryParam("vs_currencies", "usd")
                    .toUriString();

            Map<String, Map<String, Double>> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("bitcoin")) {
                Double price = (Double) response.get("bitcoin").get("usd");
                if (price != null) return price;
            }
        } catch (Exception e) {
            log.error("Failed to fetch BTC price from CoinGecko", e);
        }

        return 36000.0;
    }
}
