package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.request.WalletActivityEvent;
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

    public void registerSession(Long userId, String sessionId) {
        userSessions.computeIfAbsent(userId, k -> new HashSet<>()).add(sessionId);
        log.info("Registered session {} for user {}", sessionId, userId);

        String key = "user:notifications:" + userId;
        List<String> payloads = redisTemplate.opsForList().range(key, 0, -1);
        if (payloads != null) {
            for (String payload : payloads) {
                try {
                    WalletActivityEvent event = objectMapper.readValue(payload, WalletActivityEvent.class);
                    messagingTemplate.convertAndSendToUser(sessionId, "/queue/wallet-activity", event);
                } catch (Exception e) {
                    log.error("Failed to send offline notification to user {} session {}", userId, sessionId, e);
                }
            }

            redisTemplate.delete(key);
        }
    }

    public void unregisterSession(Long userId, String sessionId) {
        Set<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) userSessions.remove(userId);
            log.info("Unregistered session {} for user {}", sessionId, userId);
        }
    }

    public void notifyUser(Long userId, WalletActivityEvent event) {
        Set<String> sessions = userSessions.get(userId);
        if (sessions != null && !sessions.isEmpty()) {

            for (String sessionId : sessions) {
                try {
                    messagingTemplate.convertAndSendToUser(sessionId, "/queue/wallet-activity", event);
                    log.info("Sent wallet activity to user {} session {}", userId, sessionId);
                } catch (Exception e) {
                    log.error("Failed to send wallet activity to user {} session {}", userId, sessionId, e);
                }
            }
        } else {

            try {
                String key = "user:notifications:" + userId;
                String payload = objectMapper.writeValueAsString(event);
                redisTemplate.opsForList().rightPush(key, payload);
                log.info("Saved offline wallet activity for user {}", userId);
            } catch (Exception e) {
                log.error("Failed to save offline wallet activity for user {}", userId, e);
            }
        }
    }
}
