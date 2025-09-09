package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.request.WalletActivityEvent;
import com.kunfeng2002.be002.entity.FollowedAddress;
import com.kunfeng2002.be002.repository.FollowedAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletActivityConsumer {

    private final TelegramBotService telegramBotService;
    private final FollowedAddressRepository followedAddressRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "wallet-activity", groupId = "wallet-notifier")
    public void consume(String message) {
        log.info("Received wallet activity: {}", message);
        try {
            WalletActivityEvent event = objectMapper.readValue(message, WalletActivityEvent.class);
            String fromAddress = event.getFromAddress();
            String toAddress = event.getToAddress();

            List<FollowedAddress> followers = followedAddressRepository.findByWalletAddressIn(fromAddress.toLowerCase(), toAddress.toLowerCase());

            Set<Long> notifiedTelegramIds = new HashSet<>();

            for (FollowedAddress fa : followers) {
                Long telegramId = fa.getBot().getTelegramId();

                if (notifiedTelegramIds.add(telegramId)) {
                    String notificationMessage;

                    if (fa.getWallet().getAddress().equalsIgnoreCase(fromAddress)) {
                        notificationMessage = buildNotificationMessage(event, "đã gửi một giao dịch");
                    } else {
                        notificationMessage = buildNotificationMessage(event, "đã nhận một giao dịch");
                    }
                    telegramBotService.notifyUser(telegramId, notificationMessage);
                }
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to parse wallet activity event from Kafka message: {}", message, e);
        } catch (Exception e) {
            log.error("Error processing wallet activity message: {}", message, e);
        }
    }

    private String buildNotificationMessage(WalletActivityEvent event, String action) {
        return String.format(
                "Wallet activity \n" +
                        "Network: %s\n" +
                        "From: %s\n" +
                        "To: %s\n" +
                        "Value: %s\n" +
                        "Hash: %s",
                event.getNetwork().toUpperCase(),
                event.getFromAddress(),
                event.getToAddress(),
                event.getValue(),
                event.getTransactionHash()
        );
    }
}