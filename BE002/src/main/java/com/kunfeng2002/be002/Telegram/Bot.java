package com.kunfeng2002.be002.Telegram;

import com.kunfeng2002.be002.dto.response.ChatMessageResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.kunfeng2002.be002.service.TelegramBotService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Bot extends TelegramLongPollingBot {

    private String botUsername;
    private String botToken;
    private ApplicationContext applicationContext;
    private Map<String, List<ChatMessageResponse>> recentTelegramMessages = new HashMap<>();

    @Autowired
    public Bot(@Value("${telegram.bot.username}") String botUsername,
               @Value("${telegram.bot.token}") String botToken,
               ApplicationContext applicationContext) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.applicationContext = applicationContext;
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
        var msg = update.getMessage();
        var user = msg.getFrom();
        var username = user.getUserName();
        var idTele = user.getId();

        TelegramBotService telegramBotService = applicationContext.getBean(TelegramBotService.class);

        if (msg.isCommand()) {
            if(msg.getText().equals("/start")) {
                sendText(idTele, "Welcome to the bot, " + user.getUserName() + "! \n " +
                        "Link your Web3 address with /link_ \n" +
                        "Ex: /link_0xYourAddressHere");
            } else if (msg.getText().startsWith("/link_")) {
                String walletAddress = msg.getText().substring(6).trim();
                if (isValidAddress(walletAddress)) {
                    applicationContext.getBean(TelegramBotService.class)
                            .linkUserToTelegram(walletAddress, idTele, username);
                } else {
                    sendText(idTele, "Address is not valid");
                }
            }
            return;
        }

        String userAddress = telegramBotService.findWalletIdByTelegramId(idTele);

        if (userAddress != null) {
            telegramBotService.storeTelegramMessage(userAddress, msg.getText(), true);
            String botResponse = generateBotResponse(msg.getText());
            sendText(idTele, botResponse);
            telegramBotService.storeTelegramMessage(userAddress, botResponse, false);
        } else {
            sendText(idTele, "Link address with first /link_YourAddress");
        }

        System.out.println(user.getFirstName() + " " + user.getLastName() + " - " + username + " - " + idTele + " wrote " + msg.getText());
    }

    private String generateBotResponse(String text) {
        return "Bot: " + text;
    }

    public void copyMessage(Long who, Integer msgId){
        CopyMessage cm = CopyMessage.builder()
                .fromChatId(who.toString())
                .chatId(who.toString())
                .messageId(msgId)
                .build();
        try {
            execute(cm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendText(@NotNull Long who, String what) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what).build();
        try {
            execute(sm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        recentTelegramMessages.put(userAddress, new ArrayList<>()); // Clear after retrieving
        return messages;
    }

    private boolean isValidAddress(String address) {
        return address != null && address.matches("^0x[a-fA-F0-9]{40}$");
    }

}
