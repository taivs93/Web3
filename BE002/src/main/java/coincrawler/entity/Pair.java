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
@Table(name = "pairs", indexes = {
    @Index(name = "idx_pair_token0", columnList = "token0_id"),
    @Index(name = "idx_pair_token1", columnList = "token1_id"),
    @Index(name = "idx_pair_address", columnList = "address"),
    @Index(name = "idx_pair_created_at", columnList = "created_at_timestamp"),
    @Index(name = "idx_pair_volume", columnList = "volume_usd")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pair {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Address cannot be blank")
    @Column(name = "address", nullable = false, length = 42, unique = true)
    private String address;
    
    @NotNull(message = "Token0 cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token0_id", nullable = false)
    private Coin token0;
    
    @NotNull(message = "Token1 cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token1_id", nullable = false)
    private Coin token1;
    
    @Column(name = "reserve0", precision = 36, scale = 18)
    private BigDecimal reserve0;
    
    @Column(name = "reserve1", precision = 36, scale = 18)
    private BigDecimal reserve1;
    
    @Column(name = "total_supply", precision = 36, scale = 18)
    private BigDecimal totalSupply;
    
    @Column(name = "price_usd", precision = 36, scale = 18)
    private BigDecimal priceUsd;
    
    @Column(name = "price_change_24h", precision = 36, scale = 18)
    private BigDecimal priceChange24h;
    
    @Column(name = "price_change_percentage_24h", precision = 36, scale = 18)
    private BigDecimal priceChangePercentage24h;
    
    @Column(name = "volume_usd", precision = 36, scale = 18)
    private BigDecimal volumeUsd;
    
    @Column(name = "volume_usd_24h", precision = 36, scale = 18)
    private BigDecimal volumeUsd24h;
    
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
    
    @Column(name = "is_stable")
    private Boolean isStable = false;
    
    @Column(name = "is_scam")
    private Boolean isScam = false;
    
    @Column(name = "dex_name", length = 100)
    private String dexName;
    
    @Column(name = "factory_address", length = 42)
    private String factoryAddress;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
    
    public String getPairSymbol() {
        if (token0 == null || token1 == null) {
            return "UNKNOWN/UNKNOWN";
        }
        return token0.getSymbol() + "/" + token1.getSymbol();
    }
    
    public boolean isNew() {
        if (createdAtTimestamp == null) {
            return false;
        }
        long now = System.currentTimeMillis() / 1000;
        return (now - createdAtTimestamp) < 86400;
    }
    
    public boolean isTrending() {
        return volumeUsd24h != null && volumeUsd24h.compareTo(BigDecimal.valueOf(1000000)) > 0;
    }
    
    public boolean involvesStablecoin() {
        if (token0 == null || token1 == null) {
            return false;
        }
        return isStablecoin(token0.getSymbol()) || isStablecoin(token1.getSymbol());
    }
    
    private boolean isStablecoin(String symbol) {
        if (symbol == null) return false;
        String upperSymbol = symbol.toUpperCase();
        return upperSymbol.equals("USDT") || upperSymbol.equals("USDC") || 
               upperSymbol.equals("BUSD") || upperSymbol.equals("DAI") ||
               upperSymbol.equals("TUSD") || upperSymbol.equals("USDP");
    }
}
