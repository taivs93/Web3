package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.request.PriceMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceFetcher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final CryptoPriceService cryptoPriceService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedRate = 5000)
    public void fetchPrices() {
        Map<String, Double> prices = cryptoPriceService.fetchAllPricesFromAPI();
        if (prices.isEmpty()) {
            log.warn("No prices fetched this round, skip sending to Kafka.");
            return;
        }

        prices.forEach((symbol, price) -> {
            try {
                PriceMessage msg = new PriceMessage(symbol, price);
                kafkaTemplate.send("prices", symbol, objectMapper.writeValueAsString(msg));
                log.info("{} price sent to Kafka: {}", symbol.toUpperCase(), price);
            } catch (Exception e) {
                log.error("Failed to send {} price to Kafka", symbol, e);
            }
        });
    }
}
