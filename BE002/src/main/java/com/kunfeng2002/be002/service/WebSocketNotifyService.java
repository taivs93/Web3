package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.request.WalletActivityEvent;
import com.kunfeng2002.be002.dto.response.PriceAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotifyService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    public void notifyUser(Long userId, WalletActivityEvent event) {
        Set<String> sessions = userSessions.get(userId);
        if (sessions != null && !sessions.isEmpty()) {
            for (String sessionId : sessions) {
                messagingTemplate.convertAndSendToUser(sessionId, "/queue/wallet-activity", event);
                log.info("Sent wallet activity to user {} session {}", userId, sessionId);
            }
        } else {
            String key = "user:notifications:" + userId;
            try {
                String payload = objectMapper.writeValueAsString(event);
                redisTemplate.opsForList().rightPush(key, payload);
                log.info("Saved offline wallet activity for user {}", userId);
            } catch (JsonProcessingException e) {
                log.warn("Fail to parse to JSON wallet event", e);
            }
        }
    }

    public void notifyPriceAlert(Long userId, String symbol, double price) {
        Set<String> sessions = userSessions.get(userId);
        PriceAlertEvent event = new PriceAlertEvent(symbol.toUpperCase(), price,
                symbol.toUpperCase() + " Alert! Price reached $" + price);

        if (sessions != null && !sessions.isEmpty()) {
            for (String sessionId : sessions) {
                messagingTemplate.convertAndSendToUser(sessionId, "/queue/price-alert", event);
                log.info("Sent price alert to user {} session {}", userId, sessionId);
            }
        } else {
            String key = "user:price-alerts:" + userId;
            try {
                String payload = objectMapper.writeValueAsString(event);
                redisTemplate.opsForList().rightPush(key, payload);
                log.info("Saved offline price alert for user {}", userId);
            } catch (JsonProcessingException e) {
                log.warn("Fail to parse to JSON price alert", e);
            }
        }
    }
}
