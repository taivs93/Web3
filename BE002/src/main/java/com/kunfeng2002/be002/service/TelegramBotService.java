package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.request.FeeRequest;
import com.kunfeng2002.be002.dto.request.WebCommandRequest;
import com.kunfeng2002.be002.dto.response.ChatMessageResponse;
import com.kunfeng2002.be002.dto.response.FeeResponse;
import com.kunfeng2002.be002.entity.Chat;
import com.kunfeng2002.be002.entity.ChatType;
import com.kunfeng2002.be002.entity.User;
import com.kunfeng2002.be002.event.TelegramMessageEvent;
import com.kunfeng2002.be002.exception.DataNotFoundException;
import com.kunfeng2002.be002.repository.ChatRepository;
import com.kunfeng2002.be002.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final OnlineSearchService onlineSearchService;

    @Transactional
    public void startBot(Long telegramUserId, Long chatId, String chatType, String title) {
        ensureChatExists(chatId, chatType, title, telegramUserId);
    }

    @Transactional
    public void ensureChatExists(Long chatId, String chatType, String title) {
        ensureChatExists(chatId, chatType, title, null);
    }

    @Transactional
    public void ensureChatExists(Long chatId, String chatType, String title, Long telegramUserId) {
        chatRepository.findByChatId(chatId)
                .ifPresentOrElse(
                        chat -> log.info("Chat {} already exists.", chatId),
                        () -> {
                            Chat newChat = new Chat();
                            newChat.setChatId(chatId);
                            newChat.setChatType(ChatType.fromTelegramType(chatType));
                            newChat.setTitle(title);
                            newChat.setIsActive(1);
                            
                            if (newChat.isPrivate() && telegramUserId != null) {
                                userRepository.findByTelegramUserId(telegramUserId).ifPresent(newChat::setUser);
                            }
                            chatRepository.save(newChat);
                            log.info("Auto-created chat {} for type {}", chatId, chatType);
                        }
                );
    }

    @Transactional
    public String followWallet(Long chatId, String walletAddress) {
        ensureChatExists(chatId, "PRIVATE", "Private Chat");
        followService.follow(chatId, walletAddress);
        return "You are now following: " + walletAddress;
    }

    @Transactional
    public String followWallet(Long chatId, String walletAddress, String chatType, String title) {
        ensureChatExists(chatId, chatType, title);
        followService.follow(chatId, walletAddress);
        return "You are now following: " + walletAddress;
    }

    @Transactional
    public String unfollowWallet(Long chatId, String walletAddress) {
        ensureChatExists(chatId, "PRIVATE", "Private Chat");
        followService.unfollow(chatId, walletAddress);
        return "You have unfollowed: " + walletAddress;
    }

    @Transactional
    public String unfollowWallet(Long chatId, String walletAddress, String chatType, String title) {
        ensureChatExists(chatId, chatType, title);
        followService.unfollow(chatId, walletAddress);
        return "You have unfollowed: " + walletAddress;
    }

    @Transactional
    public String searchCoins(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "Vui lòng nhập từ khóa tìm kiếm. Ví dụ: /search BTC hoặc /search Bitcoin";
        }

        query = query.trim();
        
        if (isValidAddress(query)) {
            return searchByAddress(query);
        } else {
            return searchBySymbolOrName(query);
        }
    }

    private String searchByAddress(String address) {
        try {
            log.info("Tìm kiếm địa chỉ trực tuyến cho: {}", address);
            try {
                var onlineResult = onlineSearchService.searchByAddressOnline(address).get();
                if (onlineResult != null && onlineResult.getAddress() != null && !onlineResult.getAddress().trim().isEmpty()) {
                    log.info("Tìm thấy địa chỉ trực tuyến: {} - {}", onlineResult.getName(), onlineResult.getSymbol());
                    
                    return String.format("Tìm thấy token:\n\n" +
                            "Name: %s\n" +
                            "Address_Contract: %s\n\n" +
                            "Sử dụng: /follow %s để theo dõi token này", 
                            onlineResult.getName() != null ? onlineResult.getName() : "Unknown",
                            address,
                            address);
                }
            } catch (Exception e) {
                log.error("Lỗi khi tìm kiếm trực tuyến: {}", e.getMessage(), e);
            }
            
            return String.format("Address: %s\n\n" +
                    "Không tìm thấy token trên BSCScan\n" +
                    "Đây có thể là:\n" +
                    "- Ví cá nhân (không phải token)\n" +
                    "- Token không tồn tại trên BSC\n" +
                    "- Address không hợp lệ\n\n" +
                    "Thử tìm kiếm theo tên hoặc symbol:\n" +
                    "/search BTC hoặc /search Bitcoin", address);
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm địa chỉ: {}", e.getMessage(), e);
            return "Lỗi khi tìm kiếm. Vui lòng thử lại.";
        }
    }

    private String searchBySymbolOrName(String query) {
        try {
            log.info("Tìm kiếm trực tuyến cho: {}", query);
            try {
                var onlineResults = onlineSearchService.searchOnline(query).get();
                if (!onlineResults.isEmpty()) {
                    log.info("Tìm thấy {} kết quả trực tuyến cho: {}", onlineResults.size(), query);
                    
                    StringBuilder result = new StringBuilder();
                    result.append(String.format("Tìm thấy %d coin(s):\n\n", onlineResults.size()));

                    for (int i = 0; i < Math.min(onlineResults.size(), 10); i++) {
                        var coin = onlineResults.get(i);
                        result.append(String.format("%d. Name: %s\n", 
                                i + 1, 
                                coin.getName() != null ? coin.getName() : "Unknown"));
                        result.append(String.format("   Address_Contract: %s\n\n", 
                                coin.getAddress() != null ? coin.getAddress() : "N/A"));
                    }

                    if (onlineResults.size() > 10) {
                        result.append(String.format("... và %d coin khác\n", onlineResults.size() - 10));
                    }

                    return result.toString();
                }
            } catch (Exception e) {
                log.error("Lỗi khi tìm kiếm trực tuyến: {}", e.getMessage(), e);
            }
            
            return String.format("Không tìm thấy coin nào với từ khóa: %s\n\n" +
                    "Gợi ý:\n" +
                    "- Kiểm tra chính tả\n" +
                    "- Thử tìm kiếm theo symbol (VD: BTC, ETH)\n" +
                    "- Thử tìm kiếm theo tên (VD: Bitcoin, Ethereum)\n" +
                    "- Thử tìm kiếm theo address contract", query);
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm: {}", e.getMessage(), e);
            return "Lỗi khi tìm kiếm. Vui lòng thử lại.";
        }
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
            case "/search":
                if (argument == null || argument.isEmpty()) {
                    return buildResponse("Please provide a search query.\n\nExample: /search BTC");
                }
                return buildResponse(searchCoins(argument));
            case "/gas":
                FeeResponse gasResponse;
                if (argument == null || argument.isEmpty()) {
                    gasResponse = gasService.getFeeEstimate(request.getNetwork(), new FeeRequest());
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

        FeeRequest feeRequest = new FeeRequest();
        feeRequest.setGasLimit(gasLimit);

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
                .orElseGet(() -> {
                    Chat newChat = new Chat();
                    newChat.setChatId(telegramUserId);
                    newChat.setChatType(ChatType.PRIVATE);
                    newChat.setTitle(user.getUsername() != null ? user.getUsername() : "Unnamed User");
                    return newChat;
                });

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
            String network = parts[0];
            BigInteger gasLimit = parts.length > 1 ? new BigInteger(parts[1]) : null;

            FeeRequest feeRequest = new FeeRequest();
            feeRequest.setGasLimit(gasLimit);

            FeeResponse response = gasService.getFeeEstimate(network, feeRequest);

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
        ChatMessageResponse response = new ChatMessageResponse();
        response.setMessage(message);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    public List<String> getFollowedAddresses(String walletAddress) {
        User user = userRepository.findByWalletAddress(walletAddress)
                .orElseThrow(() -> new DataNotFoundException("User not found with wallet: " + walletAddress));

        if (user.getTelegramUserId() == null) {
            return new ArrayList<>();
        }

        Chat chat = chatRepository.findByChatId(user.getTelegramUserId())
                .orElseThrow(() -> new DataNotFoundException("Chat not found for Telegram user."));

        return followService.getFollowedAddressesByChatId(chat.getChatId());
    }

    private boolean isValidAddress(String address) {
        return address != null && address.matches("^0x[a-fA-F0-9]{40}$");
    }
}