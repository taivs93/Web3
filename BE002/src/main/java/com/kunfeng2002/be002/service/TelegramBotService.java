package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.request.FeeRequest;
import com.kunfeng2002.be002.dto.request.WebCommandRequest;
import com.kunfeng2002.be002.dto.response.ChatMessageResponse;
import com.kunfeng2002.be002.dto.response.FeeResponse;
import com.kunfeng2002.be002.entity.Chat;
import com.kunfeng2002.be002.entity.ChatType;
import com.kunfeng2002.be002.entity.NetworkType;
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

import java.math.BigInteger;
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
    private final GasService gasService;

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
                                    .isActive(1)
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
            case "/start":
                return buildResponse("Welcome to Web3 Chat Bot!\n\n" +
                        "Available commands:\n" +
                        "/help - Show this help message\n" +
                        "/follow <wallet_address> - Follow a wallet address\n" +
                        "/unfollow <wallet_address> - Unfollow a wallet address\n" +
                        "/list - Show followed addresses\n" +
                        "/link - Link your Telegram account\n\n" +
                        "Note: You need to link your Telegram account first!");

            case "/help":
                return buildResponse("Available commands:\n\n" +
                        "/start - Welcome message\n" +
                        "/help - Show this help message\n" +
                        "/follow <wallet_address> - Follow a wallet address\n" +
                        "/unfollow <wallet_address> - Unfollow a wallet address\n" +
                        "/list - Show followed addresses\n" +
                        "/link - Link your Telegram account\n\n" +
                        "Example: /follow 0x1234567890abcdef1234567890abcdef12345678");

            case "/follow":
                if (!isValidAddress(argument)) {
                    return buildResponse("Invalid wallet address format.\n\nExample: /follow 0x1234567890abcdef1234567890abcdef12345678");
                }
                followService.follow(chat.getChatId(), argument);
                return buildResponse("Followed address successfully: " + argument);

            case "/unfollow":
                if (!isValidAddress(argument)) {
                    return buildResponse("Invalid wallet address format.\n\nExample: /unfollow 0x1234567890abcdef1234567890abcdef12345678");
                }
                followService.unfollow(chat.getChatId(), argument);
                return buildResponse("Unfollowed address successfully: " + argument);

            case "/list":
                List<String> addresses = followService.getFollowedAddressesByChatId(chat.getChatId());
                String reply = addresses.isEmpty()
                        ? "You are not following any addresses.\n\nUse /follow <wallet_address> to start following addresses."
                        : "Your followed addresses:\n\n" + String.join("\n", addresses);
                return buildResponse(reply);

            case "/link":
                return buildResponse("To link your Telegram account:\n\n" +
                        "1. Go to your Profile page\n" +
                        "2. Click 'Liên kết Telegram' button\n" +
                        "3. Follow the instructions to get linking code\n" +
                        "4. Send /link <linking_code> in private chat with bot\n\n" +
                        "Your wallet: " + walletAddress);
            case "/gas":
                FeeResponse gasResponse;
                if (argument == null || argument.isEmpty()) {
                    gasResponse = gasService.getFeeEstimate(request.getNetwork(), FeeRequest.builder().build());
                } else {
                    gasResponse = parseGasArgument(argument);
                }
                return buildResponse(formatFeeResponse(gasResponse));

            default:
                return buildResponse("Unknown command.\n\n" +
                        "Type /help to see available commands.");
        }
    }

    private FeeResponse parseGasArgument(String argument) {
        String[] parts = argument.split(" ");
        String network = parts[0];
        BigInteger gasLimit = parts.length > 1 ? new BigInteger(parts[1]) : null;

        FeeRequest feeRequest = FeeRequest.builder()
                .gasLimit(gasLimit)
                .build();

        return gasService.getFeeEstimate(network, feeRequest);
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
    public String handleGasCommand(String argument) {
        try {
            String[] parts = argument.split(" ");
            String networkStr = parts[0].toUpperCase();
            NetworkType network = NetworkType.valueOf(networkStr);
            BigInteger gasLimit = parts.length > 1 ? new BigInteger(parts[1]) : null;

            FeeRequest feeRequest = FeeRequest.builder()
                    .gasLimit(gasLimit)
                    .build();

            FeeResponse response = gasService.getFeeEstimate(network.name(), feeRequest);

            return formatFeeResponse(response);
        } catch (Exception e) {
            return "Invalid format. Use: /gas <network> [gasLimit]";
        }
    }


    private String formatFeeResponse(FeeResponse res) {
        StringBuilder sb = new StringBuilder();
        sb.append("Gas Estimate (").append(res.getNetwork().toUpperCase()).append(")\n\n");
        if (res.getSlow() != null) {
            sb.append("Slow: ").append(res.getSlow().getMaxFeePerGas()).append(" wei")
                    .append(" → Total: ").append(res.getSlow().getTotalFee()).append("\n");
        }
        if (res.getRecommended() != null) {
            sb.append("Recommended: ").append(res.getRecommended().getMaxFeePerGas()).append(" wei")
                    .append(" → Total: ").append(res.getRecommended().getTotalFee()).append("\n");
        }
        if (res.getFast() != null) {
            sb.append("Fast: ").append(res.getFast().getMaxFeePerGas()).append(" wei")
                    .append(" → Total: ").append(res.getFast().getTotalFee()).append("\n");
        }
        return sb.toString();
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