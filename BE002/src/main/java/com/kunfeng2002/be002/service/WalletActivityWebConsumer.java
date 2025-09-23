package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.request.WalletActivityEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletActivityWebConsumer {
    private final ObjectMapper objectMapper;
    private final WebSocketNotifyService webSocketNotifyService;

    @KafkaListener(topics = "wallet-activity", groupId = "wallet-websocket")
    public void consume(String message) {
        log.info("[WebConsumer] Received wallet activity: {}", message);
        try {
            WalletActivityEvent event = objectMapper.readValue(message, WalletActivityEvent.class);
            webSocketNotifyService.broadcastWalletActivity(event);
        } catch (Exception e) {
            log.error("[WebConsumer] Failed to process wallet activity message: {}", message, e);
        }
    }
}