package com.kunfeng2002.be002.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkAccountRequest {

    @NotBlank(message = "Wallet address cannot be blank")
    private String walletAddress;
}
