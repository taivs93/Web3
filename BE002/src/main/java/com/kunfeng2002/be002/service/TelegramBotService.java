package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.Telegram.Bot;
import com.kunfeng2002.be002.dto.response.ChatMessageResponse;
import com.kunfeng2002.be002.entity.User;
import com.kunfeng2002.be002.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.*;

import static org.web3j.crypto.WalletUtils.isValidAddress;

@Service
@Slf4j
public class TelegramBotService {

    private final Bot bot;
    private final UserRepository userRepository;
    private final Map<String, Long> userAddressToTelegramId = new HashMap<>();

    private final Map<String, List<ChatMessageResponse>> telegramToWebMessages = new HashMap<>();

    public TelegramBotService(Bot bot, UserRepository userRepository) {
        this.bot = bot;
        this.userRepository = userRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            log.info("Telegram bot started successfully.");
        } catch (TelegramApiException e) {
            log.error("Telegram API Error: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to start Telegram Bot: {}", e.getMessage());
        }
    }

    public ChatMessageResponse processWebChatMessage(String message, String userAddress, String sessionId) {
        String botResponse = generateBotResponse(message);
        boolean sentToTelegram = false;
        String telegramMessageId = null;

        if (userAddressToTelegramId.containsKey(userAddress)) {
            Long telegramUserId = userAddressToTelegramId.get(userAddress);
            try {
                bot.sendText(telegramUserId, "Web Chat: " + message);
                bot.sendText(telegramUserId, "Bot: " + botResponse);
                sentToTelegram = true;
                telegramMessageId = "sent_" + System.currentTimeMillis();
            } catch (Exception e) {
                System.err.println("Failed to send to  Telegram: " + e.getMessage());
            }
        }

        return ChatMessageResponse.builder()
                .message(botResponse)
                .botName("Web3 Support Bot")
                .timestamp(System.currentTimeMillis())
                .isBot(true)
                .sessionId(sessionId)
                .sentToTelegram(sentToTelegram)
                .telegramMessageId(telegramMessageId)
                .build();
    }

    public ChatMessageResponse processChatWebMessage(String message, String userAddress, String sessionId) {
        String botResponse = this.generateBotResponse(message);

        boolean sentToTelegram = false;
        String telegramMessageId = null;

        if (userAddressToTelegramId.containsKey(userAddress)) {
            Long telegramUserId = userAddressToTelegramId.get(userAddress);
            try {
                bot.sendText(telegramUserId, "Web: " + message);
                bot.sendText(telegramUserId, "Bot: " + botResponse);
                sentToTelegram = true;
                telegramMessageId = "sent_" + System.currentTimeMillis();
            } catch (Exception e) {
                System.err.println("Failed to send to Telegram: " + e.getMessage());
            }
        }

        return ChatMessageResponse.builder()
                .message(botResponse)
                .botName("Web3 Support Bot")
                .timestamp(System.currentTimeMillis())
                .isBot(true)
                .sessionId(sessionId)
                .sentToTelegram(sentToTelegram)
                .telegramMessageId(telegramMessageId)
                .build();
    }

    private String generateBotResponse(String userText) {
        String text = userText.toLowerCase();

        if (text.contains("/help")) {
            return "Call help";
        }

        if (text.contains("/status")) {
            return "Call status";
        }
        return "Re Send!";
    }

    public List<ChatMessageResponse> getTelegramMessages(String userAddress) {
        List<ChatMessageResponse> messages = telegramToWebMessages.getOrDefault(userAddress, new ArrayList<>());
        telegramToWebMessages.put(userAddress, new ArrayList<>());
        return messages;
    }

    public String findUserAddressByTelegramId(Long telegramUserId) {
        for (Map.Entry<String, Long> entry : userAddressToTelegramId.entrySet()) {
            if (entry.getValue().equals(telegramUserId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Transactional
    public void linkUserToTelegram(String userAddress, Long telegramUserId, String telegramUsername) {
        if (!isValidAddress(userAddress)) {
            throw new IllegalArgumentException("Invalid Ethereum address: " + userAddress);
        }
        userAddressToTelegramId.put(userAddress, telegramUserId);

        Optional<User> userOpt = userRepository.findByWalletAddressQuery(userAddress);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setTelegramUserId(telegramUserId);
            userRepository.save(user);

            bot.sendText(telegramUserId, "Linked address: " + userAddress);
        } else {
            bot.sendText(telegramUserId, "ERROR: No user found for address " + userAddress);
        }
    }

    private Map<String, List<ChatMessageResponse>> recentTelegramMessages = new HashMap<>();

    public void storeTelegramMessage(String userAddress, String message, boolean isFromUser) {
        if (!recentTelegramMessages.containsKey(userAddress)) {
            recentTelegramMessages.put(userAddress, new ArrayList<>());
        }

        List<ChatMessageResponse> messages = recentTelegramMessages.get(userAddress);

        ChatMessageResponse telegramMessage = ChatMessageResponse.builder()
                .message(message)
                .botName(isFromUser ? "Telegram User" : "Telegram Bot")
                .timestamp(System.currentTimeMillis())
                .isBot(!isFromUser)
                .sessionId("telegram_sync")
                .sentToTelegram(true)
                .build();

        messages.add(telegramMessage);

        if (messages.size() > 50) {
            messages.remove(0);
        }
    }

    public List<ChatMessageResponse> getRecentMessagesForUser(String userAddress) {
        List<ChatMessageResponse> messages = recentTelegramMessages.getOrDefault(userAddress, new ArrayList<>());
        recentTelegramMessages.put(userAddress, new ArrayList<>());
        return messages;
    }

    public String findWalletIdByTelegramId(Long telegramUserId) {
        for (Map.Entry<String, Long> entry : userAddressToTelegramId.entrySet()) {
            if (entry.getValue().equals(telegramUserId)) {
                return entry.getKey();
            }
        }

        Optional<User> userOpt = userRepository.findByTelegramUserId(telegramUserId);
        if (userOpt.isPresent()) {
            String walletAddress = userOpt.get().getWallet().getAddress();
            userAddressToTelegramId.put(walletAddress, telegramUserId);
            return walletAddress;
        }

        return null;
    }


    public Optional<Long> getTelegramIdForAddress(String walletAddress) {

        Long telegramId = userAddressToTelegramId.get(walletAddress.toLowerCase());
        if (telegramId != null) {
            return Optional.of(telegramId);
        }


        Optional<User> userOpt = userRepository.findByWalletAddressQuery(walletAddress);
        if (userOpt.isPresent() && userOpt.get().getTelegramUserId() != null) {
            Long userId = userOpt.get().getTelegramUserId();
            userAddressToTelegramId.put(walletAddress.toLowerCase(), userId);
            return Optional.of(userId);
        }

        return Optional.empty();
    }

    public void sendNotification(Long telegramId, String message) {
        try {
            bot.sendText(telegramId, message);
            log.info("Notification sent to Telegram user {}: {}", telegramId, message);
        } catch (Exception e) {
            log.error("Failed to send notification to Telegram user {}: {}", telegramId, e.getMessage());
        }
    }
}
