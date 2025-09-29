package com.kunfeng2002.be002.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class LoginResponse {

    private UserDto user;

    @JsonProperty("wallet_address")
    private String walletAddress;
}