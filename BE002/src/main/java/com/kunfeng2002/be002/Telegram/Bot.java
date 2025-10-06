package com.kunfeng2002.be002.Telegram;

import com.kunfeng2002.be002.dto.request.CommandRequest;
import com.kunfeng2002.be002.dto.response.ChatMessageResponse;
import com.kunfeng2002.be002.event.TelegramMessageEvent;
import com.kunfeng2002.be002.service.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {

    private final String botUsername;
    private final String botToken;
    private final TelegramBotService telegramBotService;

    public Bot(@Value("${telegram.bot.username}") String botUsername,
               @Value("${telegram.bot.token}") String botToken,
               TelegramBotService telegramBotService) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.telegramBotService = telegramBotService;
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
        Long chatId = message.getChatId();
        String text = message.getText().trim();

        log.info("Received message in chat {}: {}", chatId, text);

        CommandRequest request = new CommandRequest();
        String rawCommand = text.split(" ")[0];
        request.setCommand(extractCommand(rawCommand));
        request.setArgument(text.contains(" ") ? text.substring(text.indexOf(" ") + 1).trim() : "");

        ChatMessageResponse response;
        try {
            response = telegramBotService.processTelegramCommand(request, chatId);
        } catch (Exception e) {
            log.error("Error processing command {} for bot", text, e);
            response = ChatMessageResponse.builder()
                    .message("An error occurred while processing your command.")
                    .build();
        }

        if (response != null && response.getMessage() != null && !response.getMessage().isEmpty()) {
            sendText(chatId, response.getMessage());
        }
    }

    private void sendText(Long chatId, String text) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}: {}", chatId, e.getMessage(), e);
        }
    }

    private String extractCommand(String rawCommand) {
        if (rawCommand == null || rawCommand.isEmpty()) {
            return "";
        }
        
        int atIndex = rawCommand.indexOf("@");
        if (atIndex > 0) {
            return rawCommand.substring(0, atIndex);
        }
        
        return rawCommand;
    }

    @EventListener
    public void onTelegramMessageEvent(TelegramMessageEvent event) {
        sendText(event.chatId(), event.message());
    }
}
