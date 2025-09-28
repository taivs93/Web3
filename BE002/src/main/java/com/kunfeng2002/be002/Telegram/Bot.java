package com.kunfeng2002.be002.Telegram;

import com.kunfeng2002.be002.event.TelegramMessageEvent;
import com.kunfeng2002.be002.service.GasService;
import com.kunfeng2002.be002.service.TelegramBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


@Component
public class Bot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    
    private final String botUsername;
    private final String botToken;
    private final TelegramBotService telegramBotService;
    private final GasService gasService;

    public Bot(@Value("${telegram.bot.username}") String botUsername,
               @Value("${telegram.bot.token}") String botToken,
               TelegramBotService telegramBotService,GasService gasService) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.telegramBotService = telegramBotService;
        this.gasService = gasService;
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

        logger.info("Received message from user {} in chat {}: {}",
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
        logger.info("Processing private message from user {}", context.userId);
        executeCommand(context, text, context.firstName);
    }

    private void processGroupMessage(MessageContext context, String text, Message message) {
        logger.info("Processing group message from user {} in chat {}", context.userId, context.chatId);
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
                            ? String.format("Welcome %s!\n\nAvailable commands:\n/follow <wallet_address>\n/unfollow <wallet_address>\n/search <query>\n/link <linking_code>\n/help", context.firstName)
                            : String.format("Bot is now active in this group. Mention me with @%s /help to see available commands.", botUsername);
                    break;
                case "/follow":
                    response = (argument.isEmpty() || !isValidAddress(argument))
                            ? "Invalid wallet address format. Use: /follow <wallet_address>"
                            : telegramBotService.followWallet(context.chatId, argument, context.chatType, chatTitle);
                    break;
                case "/unfollow":
                    response = (argument.isEmpty() || !isValidAddress(argument))
                            ? "Invalid wallet address format. Use: /unfollow <wallet_address>"
                            : telegramBotService.unfollowWallet(context.chatId, argument, context.chatType, chatTitle);
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
                    response = argument.isEmpty()
                            ? "Vui lòng nhập từ khóa tìm kiếm. Ví dụ: /search BTC hoặc /search Bitcoin"
                            : telegramBotService.searchCoins(argument);
                    break;
                case "/help":
                    response = isPrivateChat(context.chatType)
                            ? "Available commands:\n/start\n/follow <wallet_address>\n/unfollow <wallet_address>\n/search <query>\n/link <linking_code>\n/help"
                            : String.format("Group commands:\n@%s /follow <wallet_address>\n@%s /unfollow <wallet_address>\n@%s /search <query>\n@%s /help", botUsername, botUsername, botUsername, botUsername);
                    break;
                default:
                    if (isPrivateChat(context.chatType)) {
                        response = "Unknown command. Type /help for available commands.";
                    } else return;
                    break;
            }
            sendText(context.chatId, response);
        } catch (Exception e) {
            logger.error("Error executing command {} for user {}", command, context.userId, e);
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
            logger.error("Failed to send message to {}: {}", chatId, e.getMessage(), e);
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
}
