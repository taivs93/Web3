package com.kunfeng2002.be002.entity;

public enum ChatType {
    PRIVATE, GROUP, SUPERGROUP, CHANNEL;

    public static ChatType fromTelegramType(String telegramType) {
        return switch (telegramType.toLowerCase()) {
            case "private" -> ChatType.PRIVATE;
            case "group" -> ChatType.GROUP;
            case "supergroup" -> ChatType.SUPERGROUP;
            case "channel" -> ChatType.CHANNEL;
            default -> ChatType.PRIVATE;
        };
    }
}
