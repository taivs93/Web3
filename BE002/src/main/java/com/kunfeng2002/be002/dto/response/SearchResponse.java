package com.kunfeng2002.be002.dto.response;

import com.kunfeng2002.be002.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    private String query;
    private String network;
    private List<BlockDto> blocks;
    private List<TransactionDto> transactions;
    private List<TokenDto> tokens;
    private List<AddressActivityDto> addressActivities;
    private long totalBlocks;
    private long totalTransactions;
    private long totalTokens;
    private long totalActivities;
    private boolean found;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockDto {
        private Long id;
        private BigInteger blockNumber;
        private String blockHash;
        private String parentHash;
        private String network;
        private LocalDateTime timestamp;
        private BigInteger gasLimit;
        private BigInteger gasUsed;
        private BigInteger difficulty;
        private BigInteger totalDifficulty;
        private Long size;
        private Integer transactionCount;
        private BigInteger baseFeePerGas;
        private String extraData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDto {
        private Long id;
        private String transactionHash;
        private BigInteger blockNumber;
        private String fromAddress;
        private String toAddress;
        private BigInteger value;
        private BigInteger gas;
        private BigInteger gasPrice;
        private BigInteger gasUsed;
        private BigInteger nonce;
        private String network;
        private LocalDateTime timestamp;
        private String status;
        private String inputData;
        private Integer transactionIndex;
        private Boolean isContractCreation;
        private String contractAddress;
        private Integer logsCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenDto {
        private Long id;
        private String tokenAddress;
        private String name;
        private String symbol;
        private Integer decimals;
        private BigInteger totalSupply;
        private String network;
        private Boolean isVerified;
        private String logoUrl;
        private String description;
        private String website;
        private String twitter;
        private String telegram;
        private String discord;
        private BigInteger marketCap;
        private BigInteger priceUsd;
        private BigInteger volume24h;
        private BigDecimal priceChange24h;
        private LocalDateTime lastPriceUpdate;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressActivityDto {
        private Long id;
        private String address;
        private String network;
        private String activityType;
        private String transactionHash;
        private BigInteger blockNumber;
        private BigInteger value;
        private String tokenAddress;
        private String tokenSymbol;
        private String fromAddress;
        private String toAddress;
        private LocalDateTime timestamp;
        private BigInteger gasUsed;
        private BigInteger gasPrice;
        private Boolean isContract;
        private String contractName;
        private String methodName;
    }
}
