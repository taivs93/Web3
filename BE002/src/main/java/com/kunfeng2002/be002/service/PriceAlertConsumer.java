package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.request.PriceMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceAlertConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TelegramBotService telegramBotService;
    private final WebSocketNotifyService webSocketNotifyService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "prices", groupId = "crypto-alerts")
    public void consume(String messageJson) {
        try {
            PriceMessage msg = objectMapper.readValue(messageJson, PriceMessage.class);
            processAlerts(msg.symbol(), msg.price());
        } catch (Exception e) {
            log.error("Invalid message received from Kafka: {}", messageJson, e);
        }
    }

    private void processAlerts(String symbol, double price) {
        String redisPattern = symbol + ":alerts:*";
        Set<String> chatKeys = redisTemplate.keys(redisPattern);
        if (chatKeys == null || chatKeys.isEmpty()) return;

        for (String key : chatKeys) {
            try {
                String[] parts = key.split(":");
                if (parts.length != 3) continue;
                Long chatId = Long.parseLong(parts[2]);

                Set<Object> triggeredAlerts = redisTemplate.opsForZSet()
                        .rangeByScore(key, 0, price, 0, 10);

                if (triggeredAlerts != null && !triggeredAlerts.isEmpty()) {
                    for (Object alertObj : triggeredAlerts) {
                        String alertStr = String.valueOf(alertObj);
                        String message = symbol.toUpperCase() + " Alert! Price reached $" + alertStr;
                        try {
                            telegramBotService.notifyUser(chatId, message);
                            webSocketNotifyService.notifyPriceAlert(chatId, symbol, price);
                        } catch (Exception e) {
                            log.error("Failed to notify user {}: {}", chatId, e.getMessage());
                        }
                        redisTemplate.opsForZSet().remove(key, alertObj);
                    }
                }
            } catch (Exception e) {
                log.error("Error processing key {}: {}", key, e.getMessage());
            }
        }
    }
}
