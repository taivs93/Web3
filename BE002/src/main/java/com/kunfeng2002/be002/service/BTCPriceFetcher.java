package com.kunfeng2002.be002.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BTCPriceFetcher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final BTCPriceService btcPriceService;

    @Scheduled(fixedRate = 5000)
    public void fetchPrice() {
        try {
            Double price = btcPriceService.fetchBTCPriceFromAPI();
            kafkaTemplate.send("btc-price", String.valueOf(price));
            log.info("BTC price sent to Kafka: {}", price);
        } catch (Exception e) {
            log.error("Failed to fetch BTC price", e);
        }
    }
}

