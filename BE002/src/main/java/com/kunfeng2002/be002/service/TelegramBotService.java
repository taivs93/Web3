package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.response.ChatMessageResponse;
import com.kunfeng2002.be002.entity.BotEntity;
import com.kunfeng2002.be002.entity.User;
import com.kunfeng2002.be002.event.TelegramMessageEvent;
import com.kunfeng2002.be002.exception.DataNotFoundException;
import com.kunfeng2002.be002.repository.BotRepository;
import com.kunfeng2002.be002.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotService {

    private final ApplicationEventPublisher eventPublisher;
    private final BotRepository botRepository;
    private final FollowService followService;
    private final UserRepository userRepository;

    @Transactional
    public void startBot(Long telegramId) {
        botRepository.findByTelegramId(telegramId)
                .orElseGet(() -> botRepository.save(BotEntity.builder()
                        .telegramId(telegramId)
                        .user(null)
                        .build()));
        log.info("Telegram ID {} started the bot.", telegramId);
    }

    @Transactional
    public void followWallet(Long telegramId, String walletAddress) {
        botRepository.findByTelegramId(telegramId)
                .orElseGet(() -> botRepository.save(BotEntity.builder()
                        .telegramId(telegramId)
                        .user(null)
                        .build()));

        followService.follow(telegramId, walletAddress);
        eventPublisher.publishEvent(new TelegramMessageEvent(telegramId, "You are now following: " + walletAddress));

        log.info("Telegram ID {} started following {}", telegramId, walletAddress);
    }

    public ChatMessageResponse processChatWebMessage(String message, String walletAddress) {
        if (!message.startsWith("follow_0x")) {
            return buildResponse("Command not found");
        }

        String address = message.substring(7);
        User user = userRepository.findByWalletAddress(walletAddress)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        if (user.getTelegramUserId() == null) {
            return buildResponse("User not linked to Telegram bot. Please link your account first.");
        }

        followWallet(user.getTelegramUserId(), address);
        return buildResponse("Followed successfully");
    }

    private ChatMessageResponse buildResponse(String message) {
        return ChatMessageResponse.builder()
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public void notifyUser(Long telegramId, String message) {
        eventPublisher.publishEvent(new TelegramMessageEvent(telegramId, message));
    }

    @Transactional
    public void linkUserToTelegram(Long telegramId, String linkingCode) {
        User user = userRepository.findByTelegramLinkingCode(linkingCode)
                .orElseThrow(() -> new DataNotFoundException("Invalid or expired linking code."));

        if (user.getTelegramLinkingCodeExpiresAt() != null && user.getTelegramLinkingCodeExpiresAt().isBefore(LocalDateTime.now())) {
            user.setTelegramLinkingCode(null);
            user.setTelegramLinkingCodeExpiresAt(null);
            userRepository.save(user);
            throw new IllegalStateException("The linking code has expired.");
        }

        BotEntity bot = botRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new DataNotFoundException("Bot not found. Please use the /start command first."));

        user.setTelegramUserId(telegramId);
        bot.setUser(user);
        user.setTelegramLinkingCode(null);
        user.setTelegramLinkingCodeExpiresAt(null);

        userRepository.save(user);
        botRepository.save(bot);

        log.info("User {} successfully linked to Telegram ID {}", user.getId(), telegramId);
    }

    @Transactional
    public String generateLinkingCode(String walletAddress) {
        User user = userRepository.findByWalletAddress(walletAddress)
                .orElseThrow(() -> new DataNotFoundException("User with this wallet address not found."));

        String linkingCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        user.setTelegramLinkingCode(linkingCode);
        user.setTelegramLinkingCodeExpiresAt(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        log.info("Generated linking code {} for user with wallet address {}", linkingCode, walletAddress);

        return linkingCode;
    }
}
