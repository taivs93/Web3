package com.kunfeng2002.be002.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ChatMessageRequest {
    private String message;
    private String walletAddress;
}
