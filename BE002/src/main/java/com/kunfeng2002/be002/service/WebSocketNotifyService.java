package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.request.WalletActivityEvent;
import com.kunfeng2002.be002.dto.request.WebSocketMessageDTO;
import com.kunfeng2002.be002.entity.NetworkType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotifyService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final WebSocketSessionManager sessionManager;

    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    public void notifyUser(Long userId, Object event) {
        String eventType = "notification";
        String message = "";
        try {
            if (event instanceof WalletActivityEvent) {
                eventType = "wallet-activity";
                WalletActivityEvent waEvent = (WalletActivityEvent) event;
                String currency = getCurrencyForNetwork(waEvent.getNetwork());
                BigDecimal value = new BigDecimal(waEvent.getValue()).divide(BigDecimal.TEN.pow(18), 4, RoundingMode.HALF_UP);
                message = String.format("Wallet activity: %s → %s (%s %s)",
                        waEvent.getFromAddress(), waEvent.getToAddress(), value, currency);
            } else if (event instanceof String) {
                message = (String) event;
            }
        } catch (Exception e) {
            message = "Unknown event";
        }

        Set<String> sessions = userSessions.get(userId);
        
        if (sessions != null && !sessions.isEmpty()) {
            for (String sessionId : sessions) {
                try {
                    String jsonMessage;
                    if (event instanceof WalletActivityEvent) {
                        WalletActivityEvent waEvent = (WalletActivityEvent) event;
                        String currency = getCurrencyForNetwork(waEvent.getNetwork());
                        BigDecimal value = new BigDecimal(waEvent.getValue()).divide(BigDecimal.TEN.pow(18), 4, RoundingMode.HALF_UP);
                        jsonMessage = String.format("{\"type\":\"wallet-activity\",\"data\":{\"networkAsString\":\"%s\",\"fromAddress\":\"%s\",\"toAddress\":\"%s\",\"value\":\"%s\",\"valueFormatted\":\"%s\",\"currency\":\"%s\",\"transactionHash\":\"%s\",\"blockNumber\":\"%s\",\"gasPrice\":\"%s\",\"timestamp\":%d}}",
                            waEvent.getNetworkAsString(),
                            waEvent.getFromAddress(),
                            waEvent.getToAddress(),
                            waEvent.getValue(),
                            value.toString(),
                            currency,
                            waEvent.getTransactionHash(),
                            waEvent.getBlockNumber(),
                            waEvent.getGasPrice(),
                            waEvent.getTimestamp());
                    } else {
                        jsonMessage = String.format("{\"type\":\"%s\",\"message\":\"%s\"}", 
                            eventType, message.replace("\"", "\\\"").replace("\n", "\\n"));
                    }
                    sessionManager.sendMessage(sessionId, jsonMessage);
                } catch (Exception e) {
                }
            }
        } else {
            String key = "user:notifications:" + userId;
            try {
                String payload = objectMapper.writeValueAsString(new WebSocketMessageDTO(eventType, event));
                redisTemplate.opsForList().rightPush(key, payload);
            } catch (JsonProcessingException e) {
            }
        }
    }

    public void registerUserSession(Long userId, String sessionId) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public void unregisterUserSession(Long userId, String sessionId) {
        Set<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }

    public void notifyPriceAlert(Long userId, String symbol, double price) {
        String message = symbol.toUpperCase() + " Alert! Price reached $" + price;
        Set<String> sessions = userSessions.get(userId);
        
        if (sessions != null && !sessions.isEmpty()) {
            for (String sessionId : sessions) {
                try {
                    String jsonMessage = String.format("{\"type\":\"price-alert\",\"symbol\":\"%s\",\"price\":%.2f,\"message\":\"%s\"}", 
                        symbol, price, message);
                    sessionManager.sendMessage(sessionId, jsonMessage);
                } catch (Exception e) {
                }
            }
        } else {
            String key = "user:price-alerts:" + userId;
            redisTemplate.opsForList().rightPush(key, message);
        }
    }

    public Map<Long, Set<String>> getUserSessions() {
        return userSessions;
    }
    
    private String getCurrencyForNetwork(NetworkType network) {
        switch (network) {
            case ETHEREUM:
                return "ETH";
            case BSC:
                return "BNB";
            case AVALANCHE:
                return "AVAX";
            case OPTIMISM:
            case ARBITRUM:
                return "ETH";
            default:
                return "ETH";
        }
    }
}
