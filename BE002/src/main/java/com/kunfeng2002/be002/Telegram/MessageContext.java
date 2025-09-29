package com.kunfeng2002.be002.Telegram;

public class MessageContext {
    final Long userId;
    final Long chatId;
    final String chatType;
    final String firstName;

    MessageContext(Long userId, Long chatId, String chatType, String firstName) {
        this.userId = userId;
        this.chatId = chatId;
        this.chatType = chatType;
        this.firstName = firstName != null ? firstName : "User";
    }
}