package com.kunfeng2002.be002.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kunfeng2002.be002.entity.NetworkType;

import java.math.BigInteger;

public class WalletActivityEvent {

    @JsonProperty("chain_id")
    private Long chainId;

    @JsonProperty("network")
    private NetworkType network;

    @JsonProperty("transaction_hash")
    private String transactionHash;

    @JsonProperty("from_address")
    private String fromAddress;

    @JsonProperty("to_address")
    private String toAddress;

    @JsonProperty("value")
    private BigInteger value;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("block_number")
    private String blockNumber;

    @JsonProperty("gas_price")
    private BigInteger gasPrice;

    @JsonProperty("gas_used")
    private BigInteger gasUsed;

    @JsonProperty("gas_limit")
    private BigInteger gasLimit;

    @JsonProperty("l1_fee")
    private BigInteger l1Fee;

    @JsonProperty("status")
    private Boolean status;

    // Default constructor
    public WalletActivityEvent() {}

    // Constructor with all parameters
    public WalletActivityEvent(Long chainId, NetworkType network, String transactionHash, 
                              String fromAddress, String toAddress, BigInteger value, 
                              Long timestamp, String blockNumber, BigInteger gasPrice, 
                              BigInteger gasUsed, BigInteger gasLimit, BigInteger l1Fee, 
                              Boolean status) {
        this.chainId = chainId;
        this.network = network;
        this.transactionHash = transactionHash;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.value = value;
        this.timestamp = timestamp;
        this.blockNumber = blockNumber;
        this.gasPrice = gasPrice;
        this.gasUsed = gasUsed;
        this.gasLimit = gasLimit;
        this.l1Fee = l1Fee;
        this.status = status;
    }

    // Getters
    public Long getChainId() {
        return chainId;
    }

    public NetworkType getNetwork() {
        return network;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public BigInteger getValue() {
        return value;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getBlockNumber() {
        return blockNumber;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public BigInteger getL1Fee() {
        return l1Fee;
    }

    public Boolean getStatus() {
        return status;
    }

    // Setters
    public void setChainId(Long chainId) {
        this.chainId = chainId;
    }

    public void setNetwork(NetworkType network) {
        this.network = network;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public void setGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public void setL1Fee(BigInteger l1Fee) {
        this.l1Fee = l1Fee;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getNetworkAsString() {
        return network != null ? network.name() : "UNKNOWN";
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long chainId;
        private NetworkType network;
        private String transactionHash;
        private String fromAddress;
        private String toAddress;
        private BigInteger value;
        private Long timestamp;
        private String blockNumber;
        private BigInteger gasPrice;
        private BigInteger gasUsed;
        private BigInteger gasLimit;
        private BigInteger l1Fee;
        private Boolean status;

        public Builder chainId(Long chainId) {
            this.chainId = chainId;
            return this;
        }

        public Builder network(NetworkType network) {
            this.network = network;
            return this;
        }

        public Builder transactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
            return this;
        }

        public Builder fromAddress(String fromAddress) {
            this.fromAddress = fromAddress;
            return this;
        }

        public Builder toAddress(String toAddress) {
            this.toAddress = toAddress;
            return this;
        }

        public Builder value(BigInteger value) {
            this.value = value;
            return this;
        }

        public Builder timestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder blockNumber(String blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public Builder gasPrice(BigInteger gasPrice) {
            this.gasPrice = gasPrice;
            return this;
        }

        public Builder gasUsed(BigInteger gasUsed) {
            this.gasUsed = gasUsed;
            return this;
        }

        public Builder gasLimit(BigInteger gasLimit) {
            this.gasLimit = gasLimit;
            return this;
        }

        public Builder l1Fee(BigInteger l1Fee) {
            this.l1Fee = l1Fee;
            return this;
        }

        public Builder status(Boolean status) {
            this.status = status;
            return this;
        }

        public WalletActivityEvent build() {
            return new WalletActivityEvent(chainId, network, transactionHash, fromAddress, 
                                        toAddress, value, timestamp, blockNumber, gasPrice, 
                                        gasUsed, gasLimit, l1Fee, status);
        }
    }
}