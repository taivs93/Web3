package com.kunfeng2002.be002.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LinkAccountRequest {

    @NotBlank(message = "Wallet address cannot be blank")
    private String walletAddress;

    // Default constructor
    public LinkAccountRequest() {}

    // Constructor with parameter
    public LinkAccountRequest(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    // Getter
    public String getWalletAddress() {
        return walletAddress;
    }

    // Setter
    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }
}