package com.kunfeng2002.be002.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens", indexes = {
    @Index(name = "idx_token_address", columnList = "tokenAddress"),
    @Index(name = "idx_symbol", columnList = "symbol"),
    @Index(name = "idx_network", columnList = "network"),
    @Index(name = "idx_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_address", nullable = false, length = 42)
    private String tokenAddress;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "decimals", nullable = false)
    private Integer decimals;

    @Column(name = "total_supply", precision = 36, scale = 18)
    private BigInteger totalSupply;

    @Column(name = "network", nullable = false, length = 20)
    private String network;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "website", length = 500)
    private String website;

    @Column(name = "twitter", length = 500)
    private String twitter;

    @Column(name = "telegram", length = 500)
    private String telegram;

    @Column(name = "discord", length = 500)
    private String discord;

    @Column(name = "market_cap", precision = 36, scale = 18)
    private BigInteger marketCap;

    @Column(name = "price_usd", precision = 36, scale = 18)
    private BigInteger priceUsd;

    @Column(name = "volume_24h", precision = 36, scale = 18)
    private BigInteger volume24h;

    @Column(name = "price_change_24h", precision = 10, scale = 4)
    private BigDecimal priceChange24h;

    @Column(name = "last_price_update")
    private LocalDateTime lastPriceUpdate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
