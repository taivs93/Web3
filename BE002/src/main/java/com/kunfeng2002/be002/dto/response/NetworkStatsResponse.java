package com.kunfeng2002.be002.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkStatsResponse {

    private String network;
    private long totalBlocks;
    private long totalTransactions;
    private long totalTokens;
    private BigInteger latestBlockNumber;
    private int blocks24h;
    private int transactions24h;
    private String status; // ACTIVE, INACTIVE, MAINTENANCE
    private LocalDateTime lastUpdate;
}
