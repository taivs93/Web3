package com.kunfeng2002.web3.coincrawler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens", indexes = {
    @Index(name = "idx_token_address", columnList = "address"),
    @Index(name = "idx_token_symbol", columnList = "symbol"),
    @Index(name = "idx_token_created_at", columnList = "created_at_timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Address cannot be blank")
    @Column(name = "address", nullable = false, length = 42, unique = true)
    private String address;
    
    @NotBlank(message = "Symbol cannot be blank")
    @Column(name = "symbol", nullable = false, length = 50)
    private String symbol;
    
    @NotBlank(message = "Name cannot be blank")
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "decimals")
    private Integer decimals;
    
    @Column(name = "total_supply", precision = 36, scale = 18)
    private BigDecimal totalSupply;
    
    @Column(name = "price_usd", precision = 36, scale = 18)
    private BigDecimal priceUsd;
    
    @Column(name = "market_cap_usd", precision = 36, scale = 18)
    private BigDecimal marketCapUsd;
    
    @Column(name = "volume_24h_usd", precision = 36, scale = 18)
    private BigDecimal volume24hUsd;
    
    @Column(name = "price_change_24h", precision = 36, scale = 18)
    private BigDecimal priceChange24h;
    
    @Column(name = "price_change_percentage_24h", precision = 36, scale = 18)
    private BigDecimal priceChangePercentage24h;
    
    @Column(name = "created_at_timestamp")
    private Long createdAtTimestamp;
    
    @Column(name = "is_verified")
    private Boolean isVerified = false;
    
    @Column(name = "is_scam")
    private Boolean isScam = false;
    
    @Column(name = "is_stablecoin")
    private Boolean isStablecoin = false;
    
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
    
    @Column(name = "contract_abi", columnDefinition = "TEXT")
    private String contractAbi;
    
    @Column(name = "source_code_url", length = 500)
    private String sourceCodeUrl;
    
    @Column(name = "audit_report_url", length = 500)
    private String auditReportUrl;
    
    @Column(name = "kyc_verified")
    private Boolean kycVerified = false;
    
    @Column(name = "doxxed_team")
    private Boolean doxxedTeam = false;
    
    @Column(name = "honeypot_check")
    private Boolean honeypotCheck = false;
    
    @Column(name = "rug_pull_check")
    private Boolean rugPullCheck = false;
    
    @Column(name = "liquidity_locked")
    private Boolean liquidityLocked = false;
    
    @Column(name = "liquidity_lock_duration_days")
    private Integer liquidityLockDurationDays;
    
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
        return volume24hUsd != null && volume24hUsd.compareTo(BigDecimal.valueOf(1000000)) > 0;
    }
    
    public int getSecurityScore() {
        int score = 0;
        
        if (Boolean.TRUE.equals(isVerified)) score += 20;
        if (Boolean.TRUE.equals(kycVerified)) score += 15;
        if (Boolean.TRUE.equals(doxxedTeam)) score += 15;
        if (Boolean.TRUE.equals(honeypotCheck)) score += 10;
        if (Boolean.TRUE.equals(rugPullCheck)) score += 10;
        if (Boolean.TRUE.equals(liquidityLocked)) score += 15;
        if (auditReportUrl != null && !auditReportUrl.isEmpty()) score += 15;
        
        return Math.min(score, 100);
    }
    
    public boolean isSafe() {
        return !Boolean.TRUE.equals(isScam) && 
               getSecurityScore() >= 50 && 
               Boolean.TRUE.equals(honeypotCheck) && 
               Boolean.TRUE.equals(rugPullCheck);
    }
}
