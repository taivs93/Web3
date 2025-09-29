package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.dto.request.WalletActivityEvent;
import com.kunfeng2002.be002.entity.NetworkType;
import com.kunfeng2002.be002.service.WebSocketNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final WebSocketNotifyService webSocketNotifyService;

    @PostMapping("/notify/{userId}")
    public String testNotify(@PathVariable Long userId, @RequestParam String message) {
        webSocketNotifyService.notifyUser(userId, message);
        return "Notification sent to user " + userId;
    }

    @PostMapping("/wallet-activity/{userId}")
    public String testWalletActivity(@PathVariable Long userId) {
        
        
        WalletActivityEvent event = WalletActivityEvent.builder()
                .network(NetworkType.BSC)
                .transactionHash("0x1234567890abcdef")
                .fromAddress("0x1234567890123456789012345678901234567890")
                .toAddress("0x0987654321098765432109876543210987654321")
                .value(BigInteger.valueOf(1000000000000000000L)) 
                .blockNumber("0x12345")
                .gasPrice(BigInteger.valueOf(20000000000L))
                .timestamp(System.currentTimeMillis())
                .build();
        
        webSocketNotifyService.notifyUser(userId, event);
        return "Wallet activity notification sent to user " + userId;
    }

    @GetMapping("/sessions")
    public String getSessions() {
        return "Active sessions: " + webSocketNotifyService.getUserSessions().toString();
    }
}
