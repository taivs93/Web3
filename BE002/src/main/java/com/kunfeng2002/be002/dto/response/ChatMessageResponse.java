package com.kunfeng2002.be002.dto.response;

public class ChatMessageResponse {
    
    private String message;
    private String botName;
    private Long timestamp;
    private boolean isBot;
    private String sessionId;
    private boolean sentToTelegram;
    private String telegramMessageId;

    // Default constructor
    public ChatMessageResponse() {}

    // Constructor with parameters
    public ChatMessageResponse(String message, String botName, Long timestamp, boolean isBot, 
                              String sessionId, boolean sentToTelegram, String telegramMessageId) {
        this.message = message;
        this.botName = botName;
        this.timestamp = timestamp;
        this.isBot = isBot;
        this.sessionId = sessionId;
        this.sentToTelegram = sentToTelegram;
        this.telegramMessageId = telegramMessageId;
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public String getBotName() {
        return botName;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public boolean isBot() {
        return isBot;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isSentToTelegram() {
        return sentToTelegram;
    }

    public String getTelegramMessageId() {
        return telegramMessageId;
    }

    // Setters
    public void setMessage(String message) {
        this.message = message;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setBot(boolean bot) {
        isBot = bot;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setSentToTelegram(boolean sentToTelegram) {
        this.sentToTelegram = sentToTelegram;
    }

    public void setTelegramMessageId(String telegramMessageId) {
        this.telegramMessageId = telegramMessageId;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String message;
        private String botName;
        private Long timestamp;
        private boolean isBot;
        private String sessionId;
        private boolean sentToTelegram;
        private String telegramMessageId;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder botName(String botName) {
            this.botName = botName;
            return this;
        }

        public Builder timestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder isBot(boolean isBot) {
            this.isBot = isBot;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder sentToTelegram(boolean sentToTelegram) {
            this.sentToTelegram = sentToTelegram;
            return this;
        }

        public Builder telegramMessageId(String telegramMessageId) {
            this.telegramMessageId = telegramMessageId;
            return this;
        }

        public ChatMessageResponse build() {
            return new ChatMessageResponse(message, botName, timestamp, isBot, 
                                        sessionId, sentToTelegram, telegramMessageId);
        }
    }
}