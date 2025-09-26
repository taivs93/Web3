package com.kunfeng2002.be002.Telegram;

import com.kunfeng2002.be002.event.TelegramMessageEvent;
import com.kunfeng2002.be002.service.BSCScanService;
import com.kunfeng2002.be002.service.GasService;
import com.kunfeng2002.be002.service.TelegramBotService;
import com.kunfeng2002.be002.service.TelegramSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigInteger;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {

    private final String botUsername;
    private final String botToken;
    private final TelegramBotService telegramBotService;
    private final GasService gasService;
    private final TelegramSearchService telegramSearchService;
    private final BSCScanService bscScanService;

    public Bot(@Value("${telegram.bot.username}") String botUsername,
               @Value("${telegram.bot.token}") String botToken,
               TelegramBotService telegramBotService,
               GasService gasService,
               TelegramSearchService telegramSearchService,
               BSCScanService bscScanService) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.telegramBotService = telegramBotService;
        this.gasService = gasService;
        this.telegramSearchService = telegramSearchService;
        this.bscScanService = bscScanService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Message message = update.getMessage();
        Chat chat = message.getChat();
        User user = message.getFrom();
        String text = message.getText().trim();

        log.info("Received message from user {} in chat {}: {}",
                user.getId(), chat.getId(), text);

        MessageContext context = new MessageContext(
                user.getId(),
                chat.getId(),
                chat.getType(),
                user.getFirstName()
        );

        if (isPrivateChat(chat)) {
            processPrivateMessage(context, text);
        } else if (isGroupChat(chat)) {
            processGroupMessage(context, text, message);
        }
    }

    private void processPrivateMessage(MessageContext context, String text) {
        log.info("Processing private message from user {}", context.userId);
        executeCommand(context, text, context.firstName);
    }

    private void processGroupMessage(MessageContext context, String text, Message message) {
        log.info("Processing group message from user {} in chat {}", context.userId, context.chatId);
        if (isBotMentioned(text) || isReplyToBot(message) || text.startsWith("/")) {
            String cleanText = text.replace("@" + botUsername, "").trim();
            executeCommand(context, cleanText, message.getChat().getTitle());
        }
    }

    private void executeCommand(MessageContext context, String text, String chatTitle) {
        String[] parts = text.split(" ", 2);
        String command = parts[0].toLowerCase();
        String argument = parts.length > 1 ? parts[1] : "";

        String response;
        try {
            switch (command) {
                case "/start":
                    telegramBotService.startBot(context.userId, context.chatId, context.chatType, chatTitle);
                    response = isPrivateChat(context.chatType)
                            ? String.format("Welcome %s!\n\nAvailable commands:\n/follow <wallet_address>\n/unfollow <wallet_address>\n/link <linking_code>\n/help", context.firstName)
                            : String.format("Bot is now active in this group. Mention me with @%s /help to see available commands.", botUsername);
                    break;
                case "/follow":
                    response = (argument.isEmpty() || !isValidAddress(argument))
                            ? "Invalid wallet address format. Use: /follow <wallet_address>"
                            : telegramBotService.followWallet(context.chatId, argument);
                    break;
                case "/unfollow":
                    response = (argument.isEmpty() || !isValidAddress(argument))
                            ? "Invalid wallet address format. Use: /unfollow <wallet_address>"
                            : telegramBotService.unfollowWallet(context.chatId, argument);
                    break;
                case "/link":
                    response = !isPrivateChat(context.chatType)
                            ? "Use /link in private chat only."
                            : argument.isEmpty()
                            ? "Please provide a linking code. Use: /link <linking_code>"
                            : telegramBotService.linkUserToTelegram(context.userId, argument);
                    break;
                case "/gas":
                    response = argument.isEmpty()
                            ? gasService.getGasEstimate("bsc", null)
                            : telegramBotService.handleGasCommand(argument);
                    break;
                case "/search":
                case "/s":
                    response = argument.isEmpty()
                            ? "Sử dụng: /search <query> [network]\nVí dụ: /search 0x123... BSC"
                            : handleSearchCommand(argument, context.chatType);
                    break;
                case "/block":
                case "/b":
                    response = argument.isEmpty()
                            ? "Sử dụng: /block <block_number_or_hash> [network]\nVí dụ: /block 12345 BSC"
                            : handleBlockSearch(argument, context.chatType);
                    break;
                case "/tx":
                case "/transaction":
                    response = argument.isEmpty()
                            ? "Sử dụng: /tx <transaction_hash> [network]\nVí dụ: /tx 0xabc... BSC"
                            : handleTransactionSearch(argument, context.chatType);
                    break;
                case "/address":
                case "/addr":
                    response = argument.isEmpty()
                            ? "Sử dụng: /address <wallet_address> [network]\nVí dụ: /address 0xdef... BSC"
                            : handleAddressSearch(argument, context.chatType);
                    break;
                case "/token":
                case "/coin":
                    response = argument.isEmpty()
                            ? "Sử dụng: /token <token_name_or_address> [network]\nVí dụ: /token USDT BSC"
                            : handleTokenSearch(argument, context.chatType);
                    break;
                case "/stats":
                    response = handleStatsCommand(argument);
                    break;
                case "/toptokens":
                case "/top":
                    response = handleTopTokensCommand(argument);
                    break;
                case "/newtoken":
                    response = handleNewTokenCommand(argument);
                    break;
                case "/help":
                    response = isPrivateChat(context.chatType)
                            ? getHelpMessage()
                            : getGroupHelpMessage();
                    break;
                default:
                    if (isPrivateChat(context.chatType)) {
                        response = "Unknown command. Type /help for available commands.";
                    } else return;
                    break;
            }
            sendText(context.chatId, response);
        } catch (Exception e) {
            log.error("Error executing command {} for user {}", command, context.userId, e);
            sendText(context.chatId, "An error occurred while processing your request.");
        }
    }

    @EventListener
    public void onTelegramMessageEvent(TelegramMessageEvent event) {
        sendText(event.chatId(), event.message());
    }

    public void sendText(Long chatId, String text) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}: {}", chatId, e.getMessage(), e);
        }
    }

    private boolean isPrivateChat(String chatType) {
        return "private".equals(chatType);
    }

    private boolean isPrivateChat(Chat chat) {
        return isPrivateChat(chat.getType());
    }

    private boolean isGroupChat(Chat chat) {
        return "group".equals(chat.getType()) || "supergroup".equals(chat.getType());
    }

    private boolean isBotMentioned(String text) {
        return text.contains("@" + botUsername);
    }

    private boolean isReplyToBot(Message message) {
        return message.getReplyToMessage() != null &&
                message.getReplyToMessage().getFrom().getUserName().equals(botUsername);
    }

    private boolean isValidAddress(String address) {
        return address != null && address.matches("^0x[a-fA-F0-9]{40}$");
    }

    // Search command handlers
    private String handleSearchCommand(String argument, String chatType) {
        String[] parts = argument.split(" ", 2);
        String query = parts[0];
        String network = parts.length > 1 ? parts[1].toUpperCase() : "BSC";
        
        return telegramSearchService.generalSearch(query, network);
    }

    private String handleBlockSearch(String argument, String chatType) {
        String[] parts = argument.split(" ", 2);
        String query = parts[0];
        String network = parts.length > 1 ? parts[1].toUpperCase() : "BSC";
        
        return telegramSearchService.searchBlock(query, network);
    }

    private String handleTransactionSearch(String argument, String chatType) {
        String[] parts = argument.split(" ", 2);
        String query = parts[0];
        String network = parts.length > 1 ? parts[1].toUpperCase() : "BSC";
        
        return telegramSearchService.searchTransaction(query, network);
    }

    private String handleAddressSearch(String argument, String chatType) {
        String[] parts = argument.split(" ", 2);
        String query = parts[0];
        String network = parts.length > 1 ? parts[1].toUpperCase() : "BSC";
        
        if (!isValidAddress(query)) {
            return "ERROR Địa chỉ ví không hợp lệ. Vui lòng nhập địa chỉ đúng định dạng 0x...";
        }
        
        return telegramSearchService.searchAddress(query, network);
    }

    private String handleTokenSearch(String argument, String chatType) {
        String[] parts = argument.split(" ", 2);
        String query = parts[0];
        String network = parts.length > 1 ? parts[1].toUpperCase() : "BSC";
        
        return telegramSearchService.searchToken(query, network);
    }

    private String handleStatsCommand(String argument) {
        String network = argument.isEmpty() ? "BSC" : argument.toUpperCase();
        return telegramSearchService.getNetworkStats(network);
    }

    private String handleTopTokensCommand(String argument) {
        String[] parts = argument.split(" ");
        String network = "BSC";
        int limit = 10;
        
        if (parts.length > 0 && !parts[0].isEmpty()) {
            try {
                limit = Integer.parseInt(parts[0]);
                if (parts.length > 1) {
                    network = parts[1].toUpperCase();
                }
            } catch (NumberFormatException e) {
                network = parts[0].toUpperCase();
            }
        }
        
        return telegramSearchService.getTopTokens(network, Math.min(limit, 20));
    }

    private String handleNewTokenCommand(String argument) {
        int count = 1; // Default value
        
        if (!argument.isEmpty()) {
            try {
                count = Integer.parseInt(argument.trim());
                if (count < 1 || count > 5) {
                    return "ERROR Số lượng token phải từ 1 đến 5. Sử dụng: /newtoken [1-5]";
                }
            } catch (NumberFormatException e) {
                return "ERROR Số lượng không hợp lệ. Sử dụng: /newtoken [1-5]";
            }
        }
        
        return telegramSearchService.getNewTokens(count);
    }

    private String getHelpMessage() {
        return """
 **Web3 Search Bot Commands**
            
            ** Search Commands:**
            `/search <query> [network]` - Tìm kiếm tổng quát
            `/block <number_or_hash> [network]` - Tìm kiếm block
            `/tx <hash> [network]` - Tìm kiếm transaction
            `/address <wallet> [network]` - Tìm kiếm địa chỉ ví
            `/token <name_or_address> [network]` - Tìm kiếm token
            
            ** Info Commands:**
            `/stats [network]` - Thống kê network
            `/top [limit] [network]` - Top tokens
            `/newtoken [1-5]` - Token mới từ BSCScan
            `/gas [network]` - Gas price
            
            ** User Commands:**
            `/follow <wallet_address>` - Theo dõi ví
            `/unfollow <wallet_address>` - Bỏ theo dõi ví
            `/link <linking_code>` - Liên kết tài khoản
            
            **Ví dụ:**
            `/search 0x123... BSC`
            `/block 12345`
            `/tx 0xabc...`
            `/address 0xdef...`
            `/token USDT`
            `/stats BSC`
            `/top 5`
            """;
    }

    private String getGroupHelpMessage() {
        return String.format("""
 **Web3 Search Bot - Group Commands**
            
            ** Search Commands:**
            `@%s /search <query> [network]`
            `@%s /block <number_or_hash> [network]`
            `@%s /tx <hash> [network]`
            `@%s /address <wallet> [network]`
            `@%s /token <name_or_address> [network]`
            
            ** Info Commands:**
            `@%s /stats [network]`
            `@%s /top [limit] [network]`
            `@%s /newtoken [1-5]`
            `@%s /gas [network]`
            
            ** User Commands:**
            `@%s /follow <wallet_address>`
            `@%s /unfollow <wallet_address>`
            
            **Ví dụ:**
            `@%s /search 0x123... BSC`
            `@%s /block 12345`
            `@%s /token USDT`
            """,
            botUsername, botUsername, botUsername, botUsername, botUsername,
            botUsername, botUsername, botUsername, botUsername,
            botUsername, botUsername,
            botUsername, botUsername, botUsername
        );
    }
}
