package com.kunfeng2002.be002.dto.request;

import java.math.BigInteger;

public class WebCommandRequest {
    private String command;
    private String network;
    private BigInteger gasLimit;
    private String walletAddress;
    private String argument;

    // Default constructor
    public WebCommandRequest() {}

    // Constructor with all parameters
    public WebCommandRequest(String command, String network, BigInteger gasLimit, String walletAddress, String argument) {
        this.command = command;
        this.network = network;
        this.gasLimit = gasLimit;
        this.walletAddress = walletAddress;
        this.argument = argument;
    }

    // Getters
    public String getCommand() {
        return command;
    }

    public String getNetwork() {
        return network;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public String getArgument() {
        return argument;
    }

    // Setters
    public void setCommand(String command) {
        this.command = command;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }
}