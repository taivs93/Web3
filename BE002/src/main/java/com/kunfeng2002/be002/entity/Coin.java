package com.kunfeng2002.be002.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address", nullable = false, unique = true, length = 42)
    private String address;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "deploy_at")
    private LocalDateTime deployAt;

    @Column(name = "decimals")
    private Integer decimals;

    @Column(name = "total_supply", precision = 36, scale = 18)
    private BigDecimal totalSupply;

    @Column(name = "current_price", precision = 36, scale = 18)
    private BigDecimal currentPrice;

    @Column(name = "market_cap", precision = 36, scale = 18)
    private BigDecimal marketCap;

    @Column(name = "volume_24h", precision = 36, scale = 18)
    private BigDecimal volume24h;

    @Column(name = "price_change_24h", precision = 36, scale = 18)
    private BigDecimal priceChange24h;

    @Column(name = "price_change_percentage_24h", precision = 36, scale = 18)
    private BigDecimal priceChangePercentage24h;

    @Column(name = "circulating_supply", precision = 36, scale = 18)
    private BigDecimal circulatingSupply;

    @Column(name = "max_supply", precision = 36, scale = 18)
    private BigDecimal maxSupply;

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

    @Column(name = "reddit", length = 500)
    private String reddit;

    @Column(name = "github", length = 500)
    private String github;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
