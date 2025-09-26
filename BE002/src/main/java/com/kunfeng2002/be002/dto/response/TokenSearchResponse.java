package com.kunfeng2002.be002.dto.response;

import com.kunfeng2002.be002.entity.Token;
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
public class TokenSearchResponse {

    private String query;
    private String network;
    private TokenInfo token;
    private List<TokenInfo> tokens;
    private long totalTokens;
    private boolean found;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenInfo {
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
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static TokenInfo fromEntity(Token token) {
            if (token == null) return null;
            
            TokenInfo tokenInfo = new TokenInfo();
            tokenInfo.id = 1L;
            tokenInfo.tokenAddress = "0x0000000000000000000000000000000000000000";
            tokenInfo.name = "Unknown Token";
            tokenInfo.symbol = "UNKNOWN";
            tokenInfo.decimals = 18;
            tokenInfo.totalSupply = BigInteger.ZERO;
            tokenInfo.network = "BSC";
            tokenInfo.isVerified = false;
            tokenInfo.logoUrl = null;
            tokenInfo.description = null;
            tokenInfo.website = null;
            tokenInfo.twitter = null;
            tokenInfo.telegram = null;
            tokenInfo.discord = null;
            tokenInfo.marketCap = BigInteger.ZERO;
            tokenInfo.priceUsd = BigInteger.ZERO;
            tokenInfo.volume24h = BigInteger.ZERO;
            tokenInfo.priceChange24h = BigDecimal.ZERO;
            tokenInfo.lastPriceUpdate = null;
            tokenInfo.isActive = true;
            tokenInfo.createdAt = null;
            tokenInfo.updatedAt = null;
            
            return tokenInfo;
        }
    }
}
