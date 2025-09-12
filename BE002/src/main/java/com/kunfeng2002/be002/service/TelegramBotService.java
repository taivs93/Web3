package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.request.WebCommandRequest;
import com.kunfeng2002.be002.dto.response.ChatMessageResponse;
import com.kunfeng2002.be002.entity.Chat;
import com.kunfeng2002.be002.entity.ChatType;
import com.kunfeng2002.be002.entity.User;
import com.kunfeng2002.be002.event.TelegramMessageEvent;
import com.kunfeng2002.be002.exception.DataNotFoundException;
import com.kunfeng2002.be002.repository.ChatRepository;
import com.kunfeng2002.be002.repository.UserRepository;
import com.kunfeng2002.be002.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotService {

    private final ApplicationEventPublisher eventPublisher;
    private final ChatRepository chatRepository;
    private final FollowService followService;
    private final UserRepository userRepository;

    @Transactional
    public void startBot(Long telegramUserId, Long chatId, String chatType, String title) {
        chatRepository.findById(chatId)
                .ifPresentOrElse(
                        chat -> log.info("Chat {} already exists.", chatId),
                        () -> {
                            Chat newChat = Chat.builder()
                                    .chatId(chatId)
                                    .chatType(ChatType.fromTelegramType(chatType))
                                    .title(title)
                                    .isActive(true)
                                    .build();
                            if (newChat.isPrivate()) {
                                userRepository.findByTelegramUserId(telegramUserId).ifPresent(newChat::setUser);
                            }
                            chatRepository.save(newChat);
                            log.info("Telegram chat {} started the bot.", chatId);
                        }
                );
    }

    @Transactional
    public String followWallet(Long chatId, String walletAddress) {
        if (!chatRepository.existsById(chatId)) throw new DataNotFoundException("Chat not found");
        followService.follow(chatId, walletAddress);
        return "You are now following: " + walletAddress;
    }

    @Transactional
    public String unfollowWallet(Long chatId, String walletAddress) {
        followService.unfollow(chatId, walletAddress);
        return "You have unfollowed: " + walletAddress;
    }

    @Transactional
    public ChatMessageResponse processWebCommand(WebCommandRequest request) {
        String command = request.getCommand() != null ? request.getCommand().trim().toLowerCase() : "";
        String walletAddress = request.getWalletAddress();
        String argument = request.getArgument();

        if (walletAddress == null || walletAddress.isEmpty()) {
            return buildResponse("Wallet address is required.");
        }

        User user = userRepository.findByWalletAddress(walletAddress)
                .orElseThrow(() -> new DataNotFoundException("User not found with wallet: " + walletAddress));

        if (user.getTelegramUserId() == null) {
            return buildResponse("Your wallet is not linked to Telegram. Please link it first.");
        }

        Chat chat = chatRepository.findByChatId(user.getTelegramUserId())
                .orElseThrow(() -> new DataNotFoundException("Chat not found for Telegram user."));

        switch (command) {
            case "follow":
                if (!isValidAddress(argument)) {
                    return buildResponse("Invalid wallet address format. Example: follow 0x123...");
                }
                followService.follow(chat.getChatId(), argument);
                return buildResponse("Followed address successfully: " + argument);

            case "unfollow":
                if (isValidAddress(argument)) {
                    return buildResponse("Invalid wallet address format. Example: unfollow 0x123...");
                }
                followService.unfollow(chat.getChatId(), argument);
                return buildResponse("Unfollowed address successfully: " + argument);

            case "list":
                List<String> addresses = followService.getFollowedAddressesByChatId(chat.getChatId());
                String reply = addresses.isEmpty()
                        ? "You are not following any addresses."
                        : "Your followed addresses:\n" + String.join("\n", addresses);
                return buildResponse(reply);

            default:
                return buildResponse("Unknown command. Available: follow, unfollow, list");
        }
    }

    public void notifyUser(Long chatId, String message) {
        eventPublisher.publishEvent(new TelegramMessageEvent(chatId, message));
    }

    @Transactional
    public String linkUserToTelegram(Long telegramUserId, String linkingCode) {
        User user = userRepository.findByTelegramLinkingCode(linkingCode)
                .orElseThrow(() -> new DataNotFoundException("Invalid or expired linking code."));

        if (user.getTelegramLinkingCodeExpiresAt() != null && user.getTelegramLinkingCodeExpiresAt().isBefore(LocalDateTime.now())) {
            user.setTelegramLinkingCode(null);
            user.setTelegramLinkingCodeExpiresAt(null);
            userRepository.save(user);
            return "The linking code has expired.";
        }

        Chat privateChat = chatRepository.findById(telegramUserId)
                .orElseGet(() -> Chat.builder()
                        .chatId(telegramUserId)
                        .chatType(ChatType.PRIVATE)
                        .title(user.getUsername() != null ? user.getUsername() : "Unnamed User")
                        .build());

        privateChat.setUser(user);
        user.setTelegramUserId(telegramUserId);
        user.setTelegramLinkingCode(null);
        user.setTelegramLinkingCodeExpiresAt(null);

        userRepository.save(user);
        chatRepository.save(privateChat);
        log.info("User {} successfully linked to Telegram ID {}", user.getId(), telegramUserId);
        return "Your Telegram account has been successfully linked!";
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

    private ChatMessageResponse buildResponse(String message) {
        return ChatMessageResponse.builder()
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private boolean isValidAddress(String address) {
        return address != null && address.matches("^0x[a-fA-F0-9]{40}$");
    }
}