package com.kunfeng2002.be002.event;

public record TelegramMessageEvent(Long chatId, String message) {
}
