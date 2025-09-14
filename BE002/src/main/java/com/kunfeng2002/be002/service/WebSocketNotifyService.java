package com.kunfeng2002.be002.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotifyService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastWalletActivity(Object event) {
        messagingTemplate.convertAndSend("/topic/wallet-activity", event);
        log.info("send web notify topic");
    }
}
