package com.kunfeng2002.be002.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDto {
    private Long id;

    private String username;
    private String email;

    @JsonProperty("avatar_url")
    private String avatarUrl;
    private String bio;

    @JsonProperty("is_active")
    private Integer isActive;

    @JsonProperty("last_login_at")
    private LocalDateTime lastLoginAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("wallet_address")
    private String walletAddress;

    @JsonProperty("telegram_user_id")
    private Long telegramUserId;
}