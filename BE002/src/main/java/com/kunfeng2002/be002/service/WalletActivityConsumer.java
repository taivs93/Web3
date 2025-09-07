package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.request.WalletActivityEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletActivityConsumer {

    private final TelegramBotService telegramBotService;
    private final FollowService followService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "wallet-activity", groupId = "wallet-notifier")
    public void consume(String message) {
        log.info("Received wallet activity: {}", message);
        try {
            WalletActivityEvent event = objectMapper.readValue(message, WalletActivityEvent.class);
            Set<Long> notifiedTelegramIds = new HashSet<>();

            String network = event.getNetwork().toUpperCase();
            String fromAddress = event.getFromAddress();
            String toAddress = event.getToAddress();
            String transactionHash = event.getTransactionHash();
            if (followService.isAddressFollowed(fromAddress, network)) {
                Optional<Long> telegramIdOpt = telegramBotService.getTelegramIdForAddress(fromAddress);
                telegramIdOpt.ifPresent(telegramId -> {
                    notifiedTelegramIds.add(telegramId);
                    telegramBotService.sendNotification(telegramId, buildNotificationMessage(event, "sent a transaction"));
                });
            }

            if (followService.isAddressFollowed(toAddress, network)) {
                Optional<Long> telegramIdOpt = telegramBotService.getTelegramIdForAddress(toAddress);
                telegramIdOpt.ifPresent(telegramId -> {
                    if (!notifiedTelegramIds.contains(telegramId)) {
                        telegramBotService.sendNotification(telegramId, buildNotificationMessage(event, "received a transaction"));
                    }
                });
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to parse wallet activity event from Kafka message: {}", message, e);
        } catch (Exception e) {
            log.error("Error processing wallet activity message: {}", message, e);
        }
    }

    private String buildNotificationMessage(WalletActivityEvent event, String action) {
        return String.format(
                        "Network: %s\n" +
                        "Address: %s\n" +
                        "Action: %s\n" +
                                "Value: %s\n" +
                        "Transaction Hash: %s",
                event.getNetwork().toUpperCase(),
                event.getFromAddress(),
                action,
                event.getValue(),
                event.getTransactionHash()
        );
    }
}
