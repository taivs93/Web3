package com.kunfeng2002.be002.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CoinResponse {

    private Long id;
    private String address;
    private String name;
    private String symbol;
    private LocalDateTime deployAt;
    private Integer decimals;
    private BigDecimal totalSupply;
    private BigDecimal currentPrice;
    private BigDecimal marketCap;
    private BigDecimal volume24h;
    private BigDecimal priceChange24h;
    private BigDecimal priceChangePercentage24h;
    private BigDecimal circulatingSupply;
    private BigDecimal maxSupply;
    private String logoUrl;
    private String description;
    private String website;
    private String twitter;
    private String telegram;
    private String discord;
    private String reddit;
    private String github;
    private Boolean isActive;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public CoinResponse() {}

    // Constructor with all parameters
    public CoinResponse(Long id, String address, String name, String symbol, LocalDateTime deployAt,
                       Integer decimals, BigDecimal totalSupply, BigDecimal currentPrice, BigDecimal marketCap,
                       BigDecimal volume24h, BigDecimal priceChange24h, BigDecimal priceChangePercentage24h,
                       BigDecimal circulatingSupply, BigDecimal maxSupply, String logoUrl, String description,
                       String website, String twitter, String telegram, String discord, String reddit,
                       String github, Boolean isActive, LocalDateTime lastUpdated, LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.id = id;
        this.address = address;
        this.name = name;
        this.symbol = symbol;
        this.deployAt = deployAt;
        this.decimals = decimals;
        this.totalSupply = totalSupply;
        this.currentPrice = currentPrice;
        this.marketCap = marketCap;
        this.volume24h = volume24h;
        this.priceChange24h = priceChange24h;
        this.priceChangePercentage24h = priceChangePercentage24h;
        this.circulatingSupply = circulatingSupply;
        this.maxSupply = maxSupply;
        this.logoUrl = logoUrl;
        this.description = description;
        this.website = website;
        this.twitter = twitter;
        this.telegram = telegram;
        this.discord = discord;
        this.reddit = reddit;
        this.github = github;
        this.isActive = isActive;
        this.lastUpdated = lastUpdated;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public LocalDateTime getDeployAt() {
        return deployAt;
    }

    public Integer getDecimals() {
        return decimals;
    }

    public BigDecimal getTotalSupply() {
        return totalSupply;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getMarketCap() {
        return marketCap;
    }

    public BigDecimal getVolume24h() {
        return volume24h;
    }

    public BigDecimal getPriceChange24h() {
        return priceChange24h;
    }

    public BigDecimal getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    public BigDecimal getCirculatingSupply() {
        return circulatingSupply;
    }

    public BigDecimal getMaxSupply() {
        return maxSupply;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsite() {
        return website;
    }

    public String getTwitter() {
        return twitter;
    }

    public String getTelegram() {
        return telegram;
    }

    public String getDiscord() {
        return discord;
    }

    public String getReddit() {
        return reddit;
    }

    public String getGithub() {
        return github;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setDeployAt(LocalDateTime deployAt) {
        this.deployAt = deployAt;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
    }

    public void setTotalSupply(BigDecimal totalSupply) {
        this.totalSupply = totalSupply;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setMarketCap(BigDecimal marketCap) {
        this.marketCap = marketCap;
    }

    public void setVolume24h(BigDecimal volume24h) {
        this.volume24h = volume24h;
    }

    public void setPriceChange24h(BigDecimal priceChange24h) {
        this.priceChange24h = priceChange24h;
    }

    public void setPriceChangePercentage24h(BigDecimal priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    public void setCirculatingSupply(BigDecimal circulatingSupply) {
        this.circulatingSupply = circulatingSupply;
    }

    public void setMaxSupply(BigDecimal maxSupply) {
        this.maxSupply = maxSupply;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public void setTelegram(String telegram) {
        this.telegram = telegram;
    }

    public void setDiscord(String discord) {
        this.discord = discord;
    }

    public void setReddit(String reddit) {
        this.reddit = reddit;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String address;
        private String name;
        private String symbol;
        private LocalDateTime deployAt;
        private Integer decimals;
        private BigDecimal totalSupply;
        private BigDecimal currentPrice;
        private BigDecimal marketCap;
        private BigDecimal volume24h;
        private BigDecimal priceChange24h;
        private BigDecimal priceChangePercentage24h;
        private BigDecimal circulatingSupply;
        private BigDecimal maxSupply;
        private String logoUrl;
        private String description;
        private String website;
        private String twitter;
        private String telegram;
        private String discord;
        private String reddit;
        private String github;
        private Boolean isActive;
        private LocalDateTime lastUpdated;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder deployAt(LocalDateTime deployAt) {
            this.deployAt = deployAt;
            return this;
        }

        public Builder decimals(Integer decimals) {
            this.decimals = decimals;
            return this;
        }

        public Builder totalSupply(BigDecimal totalSupply) {
            this.totalSupply = totalSupply;
            return this;
        }

        public Builder currentPrice(BigDecimal currentPrice) {
            this.currentPrice = currentPrice;
            return this;
        }

        public Builder marketCap(BigDecimal marketCap) {
            this.marketCap = marketCap;
            return this;
        }

        public Builder volume24h(BigDecimal volume24h) {
            this.volume24h = volume24h;
            return this;
        }

        public Builder priceChange24h(BigDecimal priceChange24h) {
            this.priceChange24h = priceChange24h;
            return this;
        }

        public Builder priceChangePercentage24h(BigDecimal priceChangePercentage24h) {
            this.priceChangePercentage24h = priceChangePercentage24h;
            return this;
        }

        public Builder circulatingSupply(BigDecimal circulatingSupply) {
            this.circulatingSupply = circulatingSupply;
            return this;
        }

        public Builder maxSupply(BigDecimal maxSupply) {
            this.maxSupply = maxSupply;
            return this;
        }

        public Builder logoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder website(String website) {
            this.website = website;
            return this;
        }

        public Builder twitter(String twitter) {
            this.twitter = twitter;
            return this;
        }

        public Builder telegram(String telegram) {
            this.telegram = telegram;
            return this;
        }

        public Builder discord(String discord) {
            this.discord = discord;
            return this;
        }

        public Builder reddit(String reddit) {
            this.reddit = reddit;
            return this;
        }

        public Builder github(String github) {
            this.github = github;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder lastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public CoinResponse build() {
            return new CoinResponse(id, address, name, symbol, deployAt, decimals, totalSupply,
                    currentPrice, marketCap, volume24h, priceChange24h, priceChangePercentage24h,
                    circulatingSupply, maxSupply, logoUrl, description, website, twitter, telegram,
                    discord, reddit, github, isActive, lastUpdated, createdAt, updatedAt);
        }
    }
}