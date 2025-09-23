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
            
            return TokenInfo.builder()
                    .id(token.getId())
                    .tokenAddress(token.getTokenAddress())
                    .name(token.getName())
                    .symbol(token.getSymbol())
                    .decimals(token.getDecimals())
                    .totalSupply(token.getTotalSupply())
                    .network(token.getNetwork())
                    .isVerified(token.getIsVerified())
                    .logoUrl(token.getLogoUrl())
                    .description(token.getDescription())
                    .website(token.getWebsite())
                    .twitter(token.getTwitter())
                    .telegram(token.getTelegram())
                    .discord(token.getDiscord())
                    .marketCap(token.getMarketCap())
                    .priceUsd(token.getPriceUsd())
                    .volume24h(token.getVolume24h())
                    .priceChange24h(token.getPriceChange24h())
                    .lastPriceUpdate(token.getLastPriceUpdate())
                    .isActive(token.getIsActive())
                    .createdAt(token.getCreatedAt())
                    .updatedAt(token.getUpdatedAt())
                    .build();
        }
    }
}
