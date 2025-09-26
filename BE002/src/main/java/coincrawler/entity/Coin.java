package com.kunfeng2002.web3.coincrawler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coins", indexes = {
    @Index(name = "idx_coin_symbol", columnList = "symbol"),
    @Index(name = "idx_coin_name", columnList = "name"),
    @Index(name = "idx_coin_address", columnList = "address"),
    @Index(name = "idx_coin_created_at", columnList = "created_at_timestamp"),
    @Index(name = "idx_coin_volume", columnList = "trade_volume_usd")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Symbol cannot be blank")
    @Column(name = "symbol", nullable = false, length = 50)
    private String symbol;
    
    @NotBlank(message = "Name cannot be blank")
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @NotBlank(message = "Address cannot be blank")
    @Column(name = "address", nullable = false, length = 42, unique = true)
    private String address;
    
    @Column(name = "decimals")
    private Integer decimals;
    
    @Column(name = "total_supply", precision = 36, scale = 18)
    private BigDecimal totalSupply;
    
    @Column(name = "price_usd", precision = 36, scale = 18)
    private BigDecimal priceUsd;
    
    @Column(name = "price_change_24h", precision = 36, scale = 18)
    private BigDecimal priceChange24h;
    
    @Column(name = "price_change_percentage_24h", precision = 36, scale = 18)
    private BigDecimal priceChangePercentage24h;
    
    @Column(name = "market_cap_usd", precision = 36, scale = 18)
    private BigDecimal marketCapUsd;
    
    @Column(name = "trade_volume_usd", precision = 36, scale = 18)
    private BigDecimal tradeVolumeUsd;
    
    @Column(name = "liquidity_usd", precision = 36, scale = 18)
    private BigDecimal liquidityUsd;
    
    @Column(name = "fees_24h", precision = 36, scale = 18)
    private BigDecimal fees24h;
    
    @Column(name = "tx_count_24h")
    private Long txCount24h;
    
    @Column(name = "unique_wallets_24h")
    private Long uniqueWallets24h;
    
    @Column(name = "created_at_timestamp")
    private Long createdAtTimestamp;
    
    @Column(name = "is_verified")
    private Boolean isVerified = false;
    
    @Column(name = "is_scam")
    private Boolean isScam = false;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "website_url", length = 500)
    private String websiteUrl;
    
    @Column(name = "twitter_url", length = 500)
    private String twitterUrl;
    
    @Column(name = "telegram_url", length = 500)
    private String telegramUrl;
    
    @Column(name = "discord_url", length = 500)
    private String discordUrl;
    
    @Column(name = "logo_url", length = 500)
    private String logoUrl;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
    
    public String getFirstLetter() {
        if (symbol == null || symbol.isEmpty()) {
            return "OTHER";
        }
        char firstChar = Character.toUpperCase(symbol.charAt(0));
        return Character.isLetter(firstChar) ? String.valueOf(firstChar) : "OTHER";
    }
    
    public boolean isNew() {
        if (createdAtTimestamp == null) {
            return false;
        }
        long now = System.currentTimeMillis() / 1000;
        return (now - createdAtTimestamp) < 86400;
    }
    
    public boolean isTrending() {
        return tradeVolumeUsd != null && tradeVolumeUsd.compareTo(BigDecimal.valueOf(1000000)) > 0;
    }
}
