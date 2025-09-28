package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.request.WalletActivityEvent;
import com.kunfeng2002.be002.entity.Follow;
import com.kunfeng2002.be002.entity.NetworkType;
import com.kunfeng2002.be002.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletActivityTeleConsumer {

    private final TelegramBotService telegramBotService;
    private final FollowRepository followRepository;
    private final ObjectMapper objectMapper;
    private final WebSocketNotifyService webSocketNotifyService;

    @KafkaListener(topics = "wallet-activity", groupId = "wallet-tele")
    public void consume(String message) {
        WalletActivityEvent event = null;
        try {
            event = objectMapper.readValue(message, WalletActivityEvent.class);
        } catch (JsonProcessingException e) {
            return;
        }
        
        this.processNotifications(event);
    }

    private void processNotifications(WalletActivityEvent event) {
        String fromAddress = event.getFromAddress();
        String toAddress = event.getToAddress();

        List<String> addresses = new ArrayList<>();
        if (fromAddress != null) addresses.add(fromAddress);
        if (toAddress != null) addresses.add(toAddress);

        List<Follow> followers = followRepository.findByWalletAddresses(addresses);
        
        Set<Long> notifiedChatIds = new HashSet<>();
        Set<Long> notifiedUserIds = new HashSet<>();
        String fromAction = "Send a transaction";
        String toAction = "Receive a transaction";

        for (Follow follow : followers) {
            Long chatId = follow.getChat().getChatId();
            Long userId = follow.getChat().getUser().getId();
            
            if (notifiedChatIds.add(chatId)) {
                String notificationMessage;
                if (follow.getWallet().getAddress().equalsIgnoreCase(fromAddress)) {
                    notificationMessage = buildNotificationMessage(event, fromAction);
                } else {
                    notificationMessage = buildNotificationMessage(event, toAction);
                }
                telegramBotService.notifyUser(chatId, notificationMessage);
            }
            
            if (notifiedUserIds.add(userId)) {
                webSocketNotifyService.notifyUser(userId, event);
            }
        }
    }


    private String buildNotificationMessage(WalletActivityEvent event, String action) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        String formattedTime = formatter.format(Instant.ofEpochMilli(event.getTimestamp()));
        
        String currency = getCurrencyForNetwork(event.getNetwork());
        BigDecimal value = new BigDecimal(event.getValue()).divide(BigDecimal.TEN.pow(18), 4, RoundingMode.HALF_UP);
        
        return String.format(
                "Wallet activity \n" +
                        "Network: %s\n" +
                        "From: %s\n" +
                        "To: %s\n" +
                        "Value: %s %s\n" +
                        "Hash: %s\n" +
                        "Timestamp: %s",
                event.getNetworkAsString(),
                event.getFromAddress(),
                event.getToAddress(),
                value.toString(),
                currency,
                event.getTransactionHash(),
                formattedTime
        );

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
