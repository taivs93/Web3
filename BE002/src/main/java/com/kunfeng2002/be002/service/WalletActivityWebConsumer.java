package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.request.WalletActivityEvent;
import com.kunfeng2002.be002.entity.Follow;
import com.kunfeng2002.be002.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletActivityWebConsumer {

    private final ObjectMapper objectMapper;
    private final WebSocketNotifyService webSocketNotifyService;
    private final FollowRepository followRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @KafkaListener(topics = "wallet-activity", groupId = "wallet-websocket")
    public void consume(String message) {
        log.info("[WebConsumer] Received wallet activity: {}", message);
        try {
            WalletActivityEvent event = objectMapper.readValue(message, WalletActivityEvent.class);
            sendWebNotify(event);
        } catch (Exception e) {
            log.error("[WebConsumer] Failed to process wallet activity message: {}", message, e);
        }
    }

    private void sendWebNotify(WalletActivityEvent event) {
        List<String> addresses = new ArrayList<>();
        if (event.getFromAddress() != null) addresses.add(event.getFromAddress());
        if (event.getToAddress() != null) addresses.add(event.getToAddress());

        List<Follow> followers = followRepository.findByWalletAddresses(addresses);

        Set<Long> notifiedUserIds = new HashSet<>();
        for (Follow follow : followers) {
            if (follow.getChat() == null) {
                log.warn("[WebConsumer] Follow {} has no chat. Skipping.", follow.getId());
                continue;
            }

            if (follow.getChat().getUser() == null) {
                log.warn("[WebConsumer] Chat {} has no user linked. Skipping.", follow.getChat().getChatId());
                continue;
            }

            Long userId = follow.getChat().getUser().getId();
            if (notifiedUserIds.add(userId)) {
                try {
                    String key = "user:notifications:" + userId;
                    String payload = objectMapper.writeValueAsString(event);
                    redisTemplate.opsForList().rightPush(key, payload);
                    webSocketNotifyService.notifyUser(userId, event);
                } catch (Exception e) {
                    log.error("[WebConsumer] Failed to save/notify wallet activity for user {}", userId, e);
                }
            }
        }
    }
}

