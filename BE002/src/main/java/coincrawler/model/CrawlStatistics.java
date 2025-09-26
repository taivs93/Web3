package coincrawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlStatistics {
    
    private long totalCoins;
    
    private long totalPairs;
    
    private long totalTokens;
    
    private long newCoins24h;
    
    private long newPairs24h;
    
    private long newTokens24h;
    
    private long newCoins1h;
    
    private long newPairs1h;
    
    private long newTokens1h;
    
    private List<CoinSummary> topCoinsByVolume;
    
    private List<PairSummary> topPairsByVolume;
    
    private List<TokenSummary> topTokensByVolume;
    
    private Map<String, Long> statisticsByLetter;
    
    private Map<String, Long> statisticsByCategory;
    
    private CrawlStatus status;
    
    private LocalDateTime lastCrawlTime;
    
    private LocalDateTime nextCrawlTime;
    
    private long crawlDurationSeconds;
    
    private double successRate;
    
    private long errorCount;
    
    private long warningCount;
    
    public enum CrawlStatus {
        RUNNING, STOPPED, PAUSED, ERROR
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoinSummary {
        private Long id;
        private String symbol;
        private String name;
        private String address;
        private java.math.BigDecimal priceUsd;
        private java.math.BigDecimal marketCapUsd;
        private java.math.BigDecimal volumeUsd;
        private java.math.BigDecimal priceChangePercentage24h;
        private boolean isNew;
        private boolean isTrending;
        private int securityScore;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PairSummary {
        private Long id;
        private String address;
        private String pairSymbol;
        private String token0Symbol;
        private String token1Symbol;
        private java.math.BigDecimal volumeUsd;
        private java.math.BigDecimal liquidityUsd;
        private java.math.BigDecimal priceChangePercentage24h;
        private boolean isNew;
        private boolean isTrending;
        private String dexName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenSummary {
        private Long id;
        private String symbol;
        private String name;
        private String address;
        private java.math.BigDecimal priceUsd;
        private java.math.BigDecimal marketCapUsd;
        private java.math.BigDecimal volume24hUsd;
        private java.math.BigDecimal priceChangePercentage24h;
        private boolean isNew;
        private boolean isTrending;
        private boolean isStablecoin;
        private int securityScore;
    }
    
    public long getTotalNew24h() {
        return newCoins24h + newPairs24h + newTokens24h;
    }
    
    public long getTotalNew1h() {
        return newCoins1h + newPairs1h + newTokens1h;
    }
    
    public long getTotalItems() {
        return totalCoins + totalPairs + totalTokens;
    }
    
    public boolean isCrawlingActive() {
        return status == CrawlStatus.RUNNING;
    }
    
    public double getSuccessRatePercentage() {
        return successRate * 100;
    }
}
