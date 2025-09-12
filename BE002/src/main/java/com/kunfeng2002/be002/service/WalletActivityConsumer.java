package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.request.WalletActivityEvent;
import com.kunfeng2002.be002.entity.Follow;
import com.kunfeng2002.be002.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

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
public class WalletActivityConsumer {

    private final TelegramBotService telegramBotService;
    private final FollowRepository followRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "wallet-activity", groupId = "wallet-notifier")
    public void consume(String message) {
        log.info("Received wallet activity: {}", message);
        try {
            WalletActivityEvent event = objectMapper.readValue(message, WalletActivityEvent.class);
            String fromAddress = event.getFromAddress();
            String toAddress = event.getToAddress();

            List<String> addresses = new ArrayList<>();
            if (fromAddress != null) addresses.add(fromAddress);
            if (toAddress != null) addresses.add(toAddress);

            List<Follow> followers = followRepository.findByWalletAddresses(addresses);

            Set<Long> notifiedChatIds = new HashSet<>();
            String fromAction = "Send a transaction";
            String toAction = "Receive a transaction";

            for (Follow follow : followers) {
                Long chatId = follow.getChat().getChatId();

                if (notifiedChatIds.add(chatId)) {
                    String notificationMessage;
                    if (follow.getWallet().getAddress().equalsIgnoreCase(fromAddress)) {
                        notificationMessage = buildNotificationMessage(event, fromAction);
                    } else {
                        notificationMessage = buildNotificationMessage(event, toAction);
                    }
                    telegramBotService.notifyUser(chatId, notificationMessage);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse wallet activity event from Kafka message: {}", message, e);
        } catch (Exception e) {
            log.error("Error processing wallet activity message: {}", message, e);
        }
    }

    private String buildNotificationMessage(WalletActivityEvent event, String action) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        String formattedTime = formatter.format(Instant.ofEpochMilli(event.getTimestamp()));
        return String.format(
                "Wallet activity \n" +
                        "Network: %s\n" +
                        "From: %s\n" +
                        "To: %s\n" +
                        "Value: %s\n" +
                        "Hash: %s\n"
                    +   "Timestamp: %s"
                ,
                event.getNetwork().toUpperCase(),
                event.getFromAddress(),
                event.getToAddress(),
                event.getValue(),
                event.getTransactionHash(),
                formattedTime
        );
    }
}