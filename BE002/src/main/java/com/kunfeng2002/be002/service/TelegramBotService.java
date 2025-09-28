package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.request.AddTokenRequest;
import com.kunfeng2002.be002.dto.request.FeeRequest;
import com.kunfeng2002.be002.dto.request.CommandRequest;
import com.kunfeng2002.be002.dto.request.PortfolioRequest;
import com.kunfeng2002.be002.dto.response.ChatMessageResponse;
import com.kunfeng2002.be002.dto.response.FeeResponse;
import com.kunfeng2002.be002.dto.response.PortfolioResponse;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final FollowService followService;
    private final GasService gasService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final WebSocketNotifyService webSocketNotifyService;
    private final PortfolioService portfolioService;

    @Transactional
    public ChatMessageResponse processCommand(CommandRequest request) {
        String command = request.getCommand() != null ? request.getCommand().trim().toLowerCase() : "";
        String walletAddress = request.getWalletAddress();
        String argument = request.getArgument();

        if (walletAddress == null) {
            return buildResponse("This command requires a linked wallet. Please login first.");
        }

        User user = userRepository.findByWalletAddress(walletAddress)
                .orElseThrow(() -> new DataNotFoundException("User with wallet not found"));

        
        Chat chat = chatRepository.findByUserId(user.getId())
                .orElse(null);

        if (chat == null) {
            return buildResponse("Your account is not linked to Telegram. Please:\n1. Click 'Lấy Linking Code' button\n2. Send /link <code> in Telegram bot\n3. Then you can use commands here");
        }

        return processCommandInternal(command, argument, chat.getChatId());
    }

    public ChatMessageResponse processTelegramCommand(CommandRequest request, Long telegramChatId) {
        String command = request.getCommand() != null ? request.getCommand().trim().toLowerCase() : "";
        String argument = request.getArgument();
        return processCommandInternal(command, argument, telegramChatId);
    }

    private ChatMessageResponse processCommandInternal(String command, String argument, Long chatId) {
        switch (command) {
            case "/start":
            case "/help":
                return buildResponse(getHelpText());

            case "/follow":
                if (!isValidAddress(argument)) {
                    return buildResponse("Invalid wallet address format.\n\nExample: /follow 0x123...");
                }
                followService.follow(chatId, argument);
                
                webSocketNotifyService.notifyUser(getUserIdByChatId(chatId), "Đã theo dõi ví: " + argument);
                return buildResponse("Followed address successfully: " + argument);

            case "/unfollow":
                if (!isValidAddress(argument)) {
                    return buildResponse("Invalid wallet address format.\n\nExample: /unfollow 0x123...");
                }
                followService.unfollow(chatId, argument);
                
                webSocketNotifyService.notifyUser(getUserIdByChatId(chatId), "Đã bỏ theo dõi ví: " + argument);
                return buildResponse("Unfollowed address successfully: " + argument);

            case "/list":
                List<String> addresses = followService.getFollowedAddressesByChatId(chatId);
                String reply = addresses.isEmpty()
                        ? "You are not following any addresses.\nUse /follow <wallet_address> to start following."
                        : "Your followed addresses:\n" + String.join("\n", addresses);
                return buildResponse(reply);

            case "/gas":
                FeeResponse gasResponse = (argument == null || argument.isEmpty())
                        ? gasService.getFeeEstimate("ETH", FeeRequest.builder().build())
                        : parseGasArgument(argument);
                return buildResponse(formatFeeResponse(gasResponse));

            case "/setalert":
                if (argument == null || !argument.contains(" ")) {
                    return buildResponse("Invalid format. Use: /setalert BTC/ETH 35000");
                }
                try {
                    String[] parts = argument.split(" ");
                    String symbol = parts[0].toUpperCase();
                    double alertPrice = Double.parseDouble(parts[1]);
                    redisTemplate.opsForZSet().add(symbol.toLowerCase() + ":alerts:" + chatId, String.valueOf(alertPrice), alertPrice);
                    return buildResponse("Alert set for " + symbol + " ≥ $" + alertPrice);
                } catch (NumberFormatException e) {
                    return buildResponse("Invalid price format. Use: /setalert BTC/ETH 35000");
                }

            case "/listalerts":
                String listKey = "btc:alerts:" + chatId;
                Set<Object> alerts = redisTemplate.opsForZSet().range(listKey, 0, -1);
                if (alerts == null || alerts.isEmpty()) return buildResponse("No active alerts.");
                return buildResponse("Your alerts for BTC:\n" + alerts);

            case "/delalert":
                String delKey = "btc:alerts:" + chatId;
                redisTemplate.opsForZSet().remove(delKey, argument);
                return buildResponse("Deleted alert: " + argument);

            case "/link":
                
                if (argument == null || argument.trim().isEmpty()) {
                    return buildResponse("Please provide a linking code. Use: /link <code>");
                }
                return buildResponse(linkUserToTelegram(chatId, argument.trim()));

            case "/portfolio":
                return handlePortfolioCommand(chatId, argument);

            case "/addtoken":
                return handleAddTokenCommand(chatId, argument);

            case "/myportfolios":
                return handleMyPortfoliosCommand(chatId);

            default:
                return buildResponse("Unknown command.\nType /help to see available commands.");
        }
    }

    private String getHelpText() {
        return """
                Welcome to Web3 Chat Bot!

                Commands:
                /start - Show welcome message
                /help - Show this help
                /follow <wallet_address> - Follow a wallet
                /unfollow <wallet_address> - Unfollow a wallet
                /list - Show followed wallets
                /gas <network> [gasLimit] - Show gas estimate
                /setalert <symbol> <price> - Set price alert (e.g. /setalert BTC 35000)
                /listalerts - List your alerts
                /delalert <price> - Delete alert
                /link <linking code> - Link tele to web
                
                Portfolio Commands:
                /portfolio <name> - Create new portfolio
                /addtoken <symbol> <amount> <price> - Add token to portfolio
                /myportfolios - List your portfolios
                """;
    }

    @Transactional
    public String generateLinkingCode(String walletAddress) {
        User user = userRepository.findByWalletAddress(walletAddress)
                .orElseThrow(() -> new DataNotFoundException("User with this wallet address not found."));
        String linkingCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        user.setTelegramLinkingCode(linkingCode);
        user.setTelegramLinkingCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        return linkingCode;
    }

    @Transactional
    public String linkUserToTelegram(Long chatId, String linkingCode) {
        User user = userRepository.findByTelegramLinkingCode(linkingCode)
                .orElseThrow(() -> new DataNotFoundException("Invalid or expired linking code."));

        if (user.getTelegramLinkingCodeExpiresAt() != null
                && user.getTelegramLinkingCodeExpiresAt().isBefore(LocalDateTime.now())) {
            user.setTelegramLinkingCode(null);
            user.setTelegramLinkingCodeExpiresAt(null);
            userRepository.save(user);
            return "Linking code expired.";
        }
        if (!linkingCode.equals(user.getTelegramLinkingCode())) return "Linked code not match";

        Chat privateChat = chatRepository.findByChatId(chatId)
                .orElseGet(() -> {
                    Chat newChat = Chat.builder()
                            .chatId(chatId)
                            .chatType(ChatType.PRIVATE)
                            .title("Telegram Chat")
                            .build();
                    return chatRepository.save(newChat);
                });

        if (!privateChat.isPrivate()) return "Group chat can not be linked";

        privateChat.setUser(user);
        chatRepository.save(privateChat);

        user.setTelegramLinkingCode(null);
        user.setTelegramLinkingCodeExpiresAt(null);
        userRepository.save(user);
        
        return "Your Telegram account has been successfully linked!";
    }

    public void notifyUser(Long chatId, String message) {
        eventPublisher.publishEvent(new TelegramMessageEvent(chatId, message));
    }

    public void notifyUserWeb(Long userId, String message) {
        webSocketNotifyService.notifyUser(userId, message);
    }

    private FeeResponse parseGasArgument(String argument) {
        String[] parts = argument.split(" ");
        String network = parts[0];
        BigInteger gasLimit = parts.length > 1 ? new BigInteger(parts[1]) : null;
        FeeRequest feeRequest = FeeRequest.builder().gasLimit(gasLimit).build();
        return gasService.getFeeEstimate(network, feeRequest);
    }

    private String formatFeeResponse(FeeResponse res) {
        StringBuilder sb = new StringBuilder();
        sb.append("Gas Estimate (").append(res.getNetwork().toUpperCase()).append(")\n\n");
        if (res.getSlow() != null)
            sb.append("Slow: ").append(res.getSlow().getMaxFeePerGas()).append(" wei → Total: ").append(res.getSlow().getTotalFee()).append("\n");
        if (res.getRecommended() != null)
            sb.append("Recommended: ").append(res.getRecommended().getMaxFeePerGas()).append(" wei → Total: ").append(res.getRecommended().getTotalFee()).append("\n");
        if (res.getFast() != null)
            sb.append("Fast: ").append(res.getFast().getMaxFeePerGas()).append(" wei → Total: ").append(res.getFast().getTotalFee()).append("\n");
        return sb.toString();
    }

    private ChatMessageResponse buildResponse(String message) {
        return ChatMessageResponse.builder()
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private Long getUserIdByChatId(Long chatId) {
        try {
            Chat chat = chatRepository.findByChatId(chatId).orElse(null);
            return chat != null && chat.getUser() != null ? chat.getUser().getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isValidAddress(String address) {
        return address != null && address.matches("^0x[a-fA-F0-9]{40}$");
    }

    private ChatMessageResponse handlePortfolioCommand(Long chatId, String argument) {
        if (argument == null || argument.trim().isEmpty()) {
            return buildResponse("Please provide a portfolio name.\n\nExample: /portfolio My Crypto Portfolio");
        }
        
        try {
            Long userId = getUserIdByChatId(chatId);
            if (userId == null) {
                return buildResponse("Portfolio commands require account linking!\n\n" +
                    "To use portfolio features:\n" +
                    "1. Go to web interface: http://localhost:8080\n" +
                    "2. Login with your wallet\n" +
                    "3. Click 'Lấy Linking Code'\n" +
                    "4. Send /link <code> in this chat\n" +
                    "5. Then you can use /portfolio commands");
            }
            
            PortfolioRequest request = PortfolioRequest.builder()
                    .name(argument.trim())
                    .description("Portfolio created via Telegram")
                    .build();
            
            portfolioService.createPortfolio(userId, request);
            return buildResponse("Portfolio '" + argument.trim() + "' created successfully!");
        } catch (Exception e) {
            return buildResponse("Error creating portfolio: " + e.getMessage());
        }
    }

    private ChatMessageResponse handleAddTokenCommand(Long chatId, String argument) {
        if (argument == null || !argument.contains(" ")) {
            return buildResponse("Invalid format. Use: /addtoken BTC 0.5 45000");
        }
        
        try {
            String[] parts = argument.split(" ");
            if (parts.length != 3) {
                return buildResponse("Invalid format. Use: /addtoken <symbol> <amount> <price>");
            }
            
            String symbol = parts[0].toUpperCase();
            BigDecimal amount = new BigDecimal(parts[1]);
            BigDecimal price = new BigDecimal(parts[2]);
            
            Long userId = getUserIdByChatId(chatId);
            if (userId == null) {
                return buildResponse(" Portfolio commands require account linking!\n\n" +
                    "To use portfolio features:\n" +
                    "1. Go to web interface: http://localhost:8080\n" +
                    "2. Login with your wallet\n" +
                    "3. Click 'Lấy Linking Code'\n" +
                    "4. Send /link <code> in this chat\n" +
                    "5. Then you can use /portfolio commands");
            }
            
            List<PortfolioResponse> portfolios = portfolioService.getUserPortfolios(userId);
            
            if (portfolios.isEmpty()) {
                return buildResponse("No portfolios found. Create one first with /portfolio <name>");
            }
            
            PortfolioResponse firstPortfolio = portfolios.get(0);
            AddTokenRequest request = AddTokenRequest.builder()
                    .portfolioId(firstPortfolio.getId())
                    .tokenSymbol(symbol)
                    .amount(amount)
                    .buyPrice(price)
                    .build();
            
            portfolioService.addTokenToPortfolio(userId, request);
            return buildResponse("Added " + amount + " " + symbol + " at $" + price + " to portfolio '" + firstPortfolio.getName() + "'");
        } catch (Exception e) {
            return buildResponse("Error adding token: " + e.getMessage());
        }
    }

    private ChatMessageResponse handleMyPortfoliosCommand(Long chatId) {
        try {
            Long userId = getUserIdByChatId(chatId);
            if (userId == null) {
                return buildResponse("Portfolio commands require account linking!\n\n" +
                    "To use portfolio features:\n" +
                    "1. Go to web interface: http://localhost:8080\n" +
                    "2. Login with your wallet\n" +
                    "3. Click 'Lấy Linking Code'\n" +
                    "4. Send /link <code> in this chat\n" +
                    "5. Then you can use /portfolio commands");
            }
            
            List<PortfolioResponse> portfolios = portfolioService.getUserPortfolios(userId);
            
            if (portfolios.isEmpty()) {
                return buildResponse("No portfolios found. Create one with /portfolio <name>");
            }
            
            StringBuilder response = new StringBuilder("Your Portfolios:\n\n");
            for (PortfolioResponse portfolio : portfolios) {
                response.append(portfolio.getName()).append("\n");
                response.append("Total Value: $").append(portfolio.getTotalValue()).append("\n");
                response.append("PnL: $").append(portfolio.getTotalPnl()).append(" (").append(portfolio.getTotalPnlPercentage()).append("%)\n");
                response.append("Tokens: ").append(portfolio.getTokenCount()).append("\n\n");
            }
            
            return buildResponse(response.toString());
        } catch (Exception e) {
            return buildResponse("Error fetching portfolios: " + e.getMessage());
        }
    }
}
