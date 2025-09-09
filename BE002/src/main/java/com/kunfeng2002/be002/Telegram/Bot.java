package com.kunfeng2002.be002.Telegram;

import com.kunfeng2002.be002.event.TelegramMessageEvent;
import com.kunfeng2002.be002.service.TelegramBotService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {

    private final String botUsername;
    private final String botToken;
    private final TelegramBotService telegramBotService;

    private final Map<String, BiConsumer<Long, String>> commandHandlers = new HashMap<>();

    public Bot(@Value("${telegram.bot.username}") String botUsername,
               @Value("${telegram.bot.token}") String botToken,
               TelegramBotService telegramBotService) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.telegramBotService = telegramBotService;
    }

    @PostConstruct
    public void init() {
        commandHandlers.put("/start", (telegramId, arg) -> handleStartCommand(telegramId));
        commandHandlers.put("/follow", this::handleFollowCommand);
        commandHandlers.put("/link", this::handleLinkCommand);
        log.info("Bot init");
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
        log.info(String.valueOf(update.getMessage()));
        Message msg = update.getMessage();
        Long telegramId = msg.getFrom().getId();
        String text = msg.getText().trim();

        log.info("Received message from {}: {}", telegramId, text);

        String[] parts = text.split(" ", 2);
        String command = parts[0];
        String argument = parts.length > 1 ? parts[1] : "";

        if (commandHandlers.containsKey(command)) {
            commandHandlers.get(command).accept(telegramId, argument);
        } else {
            sendText(telegramId, "Invalid command. Please use /start, /follow <wallet_address>, or /link <linking_code>.");
        }

    }

    private void handleStartCommand(Long telegramId) {
        try {
            telegramBotService.startBot(telegramId);
            sendText(telegramId, "Welcome to the bot! Use /follow <wallet_address> to start following a wallet or /link <linking_code> to connect your web account.");
        } catch (Exception e) {
            log.error("Failed to handle /start command for telegramId {}", telegramId, e);
            sendText(telegramId, "Failed to connect bot.");
        }
    }

    private void handleFollowCommand(Long telegramId, String walletAddress) {
        if (walletAddress.isEmpty() || !isValidAddress(walletAddress)) {
            sendText(telegramId, "Invalid wallet address format. Please use /follow <wallet_address>.");
            return;
        }

        try {
            telegramBotService.followWallet(telegramId, walletAddress);
        } catch (Exception e) {
            log.error("Failed to follow wallet {} for telegramId {}", walletAddress, telegramId, e);
            sendText(telegramId, "Failed to follow wallet. Try again later.");
        }
    }

    private void handleLinkCommand(Long telegramId, String linkingCode) {
        if (linkingCode.isEmpty()) {
            sendText(telegramId, "Please provide a linking code. Use /link <linking_code>.");
            return;
        }

        try {
            telegramBotService.linkUserToTelegram(telegramId, linkingCode);
            sendText(telegramId, "Your Telegram account has been successfully linked!");
        } catch (Exception e) {
            log.error("Failed to link user {} with code {}", telegramId, linkingCode, e);
            sendText(telegramId, "Failed to link account. The linking code may be invalid or expired.");
        }
    }

    @EventListener
    public void onTelegramMessage(TelegramMessageEvent event) {
        sendText(event.chatId(), event.message());
    }

    public void sendText(Long chatId, String text) {
        try {
            execute(SendMessage.builder().chatId(chatId.toString()).text(text).build());
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}: {}", chatId, e.getMessage(), e);
        }
    }

    private boolean isValidAddress(String address) {
        return address != null && address.matches("^0x[a-fA-F0-9]{40}$");
    }
}
