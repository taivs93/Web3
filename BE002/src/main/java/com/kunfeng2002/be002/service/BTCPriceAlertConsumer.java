package com.kunfeng2002.be002.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BTCPriceAlertConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TelegramBotService telegramBotService;
    private final WebSocketNotifyService webSocketNotifyService;

    @KafkaListener(topics = "btc-price", groupId = "btc-alert")
    public void consume(String priceStr) {
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            log.error("Invalid BTC price: {}", priceStr);
            return;
        }

        Set<String> chatKeys = redisTemplate.keys("btc:alerts:*");
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
                        String message = "BTC Alert! Price reached $" + alertStr;
                        try {
                            telegramBotService.notifyUser(chatId, message);
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
