package com.kunfeng2002.be002.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
public class NoLombokToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String address;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column
    private Integer decimals;
    
    @Column(name = "total_supply")
    private BigDecimal totalSupply;
    
    @Column
    private String network = "BSC";
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_verified")
    private Boolean isVerified = false;
    
    @Column(name = "is_scam")
    private Boolean isScam = false;
    
    @Column(name = "is_stablecoin")
    private Boolean isStablecoin = false;
    
    @Column(name = "price_usd")
    private BigDecimal priceUsd;
    
    @Column(name = "market_cap_usd")
    private BigDecimal marketCapUsd;
    
    @Column(name = "volume_24h_usd")
    private BigDecimal volume24hUsd;
    
    @Column
    private BigDecimal priceChange24h;
    
    @Column(name = "logo_url")
    private String logoUrl;
    
    @Column
    private String website;
    
    @Column
    private String twitter;
    
    @Column
    private String discord;
    
    @Column
    private String description;
    
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
    
    @Column(name = "last_price_update")
    private LocalDateTime lastPriceUpdate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_at_timestamp")
    private Long createdAtTimestamp;
    
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
    
    public NoLombokToken() {}
    
    public NoLombokToken(String address, String name, String symbol) {
        this.address = address;
        this.name = name;
        this.symbol = symbol;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdAtTimestamp = System.currentTimeMillis() / 1000;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public Integer getDecimals() { return decimals; }
    public void setDecimals(Integer decimals) { this.decimals = decimals; }
    
    public BigDecimal getTotalSupply() { return totalSupply; }
    public void setTotalSupply(BigDecimal totalSupply) { this.totalSupply = totalSupply; }
    
    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    
    public Boolean getIsScam() { return isScam; }
    public void setIsScam(Boolean isScam) { this.isScam = isScam; }
    
    public Boolean getIsStablecoin() { return isStablecoin; }
    public void setIsStablecoin(Boolean isStablecoin) { this.isStablecoin = isStablecoin; }
    
    public BigDecimal getPriceUsd() { return priceUsd; }
    public void setPriceUsd(BigDecimal priceUsd) { this.priceUsd = priceUsd; }
    
    public BigDecimal getMarketCapUsd() { return marketCapUsd; }
    public void setMarketCapUsd(BigDecimal marketCapUsd) { this.marketCapUsd = marketCapUsd; }
    
    public BigDecimal getVolume24hUsd() { return volume24hUsd; }
    public void setVolume24hUsd(BigDecimal volume24hUsd) { this.volume24hUsd = volume24hUsd; }
    
    public BigDecimal getPriceChange24h() { return priceChange24h; }
    public void setPriceChange24h(BigDecimal priceChange24h) { this.priceChange24h = priceChange24h; }
    
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getTwitter() { return twitter; }
    public void setTwitter(String twitter) { this.twitter = twitter; }
    
    public String getDiscord() { return discord; }
    public void setDiscord(String discord) { this.discord = discord; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Boolean getKycVerified() { return kycVerified; }
    public void setKycVerified(Boolean kycVerified) { this.kycVerified = kycVerified; }
    
    public Boolean getDoxxedTeam() { return doxxedTeam; }
    public void setDoxxedTeam(Boolean doxxedTeam) { this.doxxedTeam = doxxedTeam; }
    
    public Boolean getHoneypotCheck() { return honeypotCheck; }
    public void setHoneypotCheck(Boolean honeypotCheck) { this.honeypotCheck = honeypotCheck; }
    
    public Boolean getRugPullCheck() { return rugPullCheck; }
    public void setRugPullCheck(Boolean rugPullCheck) { this.rugPullCheck = rugPullCheck; }
    
    public Boolean getLiquidityLocked() { return liquidityLocked; }
    public void setLiquidityLocked(Boolean liquidityLocked) { this.liquidityLocked = liquidityLocked; }
    
    public LocalDateTime getLastPriceUpdate() { return lastPriceUpdate; }
    public void setLastPriceUpdate(LocalDateTime lastPriceUpdate) { this.lastPriceUpdate = lastPriceUpdate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getCreatedAtTimestamp() { return createdAtTimestamp; }
    public void setCreatedAtTimestamp(Long createdAtTimestamp) { this.createdAtTimestamp = createdAtTimestamp; }
    
    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
}
