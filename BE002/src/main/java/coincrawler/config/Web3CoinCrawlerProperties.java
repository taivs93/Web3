package com.kunfeng2002.web3.coincrawler.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Data
@Validated
@ConfigurationProperties(prefix = "web3.coin-crawler")
public class Web3CoinCrawlerProperties {
    
    private boolean enabled = true;
    
    private CrawlConfig crawl = new CrawlConfig();
    
    private ApiConfig api = new ApiConfig();
    
    private DatabaseConfig database = new DatabaseConfig();
    
    private CacheConfig cache = new CacheConfig();
    
    private SecurityConfig security = new SecurityConfig();
    
    @Data
    public static class CrawlConfig {
        
        @Min(10)
        private int intervalSeconds = 30;
        
        @Min(1)
        private int maxTokensPerRequest = 200;
        
        @Min(1)
        private int maxPairsPerRequest = 100;
        
        @NotEmpty
        private List<String> popularAddresses = List.of(
            "0x10ED43C718714eb63d5aA57B78B54704E256024E", // PancakeSwap Router
            "0xcA143Ce0Fe65960E6Aa4D42C8D3cE161c2B6604F", // PancakeSwap Factory
            "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c", // WBNB
            "0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56", // BUSD
            "0x55d398326f99059fF775485246999027B3197955"  // USDT
        );
        
        private boolean autoCleanup = true;
        
        @Min(1)
        private int retentionDays = 30;
        
        private boolean parallelCrawling = true;
        
        @Min(1)
        private int maxParallelThreads = 5;
    }
    
    @Data
    public static class ApiConfig {
        
        private TheGraphConfig thegraph = new TheGraphConfig();
        
        private CoinMarketCapConfig coinmarketcap = new CoinMarketCapConfig();
        
        private BSCScanConfig bscscan = new BSCScanConfig();
        
        private CoinGeckoConfig coingecko = new CoinGeckoConfig();
        
        private PancakeSwapConfig pancakeswap = new PancakeSwapConfig();
        
        @Data
        public static class TheGraphConfig {
            private String token;
            private String apiKey;
            private String baseUrl = "https://token-api.thegraph.com";
            private String pancakeswapUrl = "https://api.thegraph.com/subgraphs/name/pancakeswap/exchange";
            private int timeoutSeconds = 30;
            private int retryAttempts = 3;
        }
        
        @Data
        public static class CoinMarketCapConfig {
            private String apiKey;
            private String baseUrl = "https://pro-api.coinmarketcap.com/v1";
            private int timeoutSeconds = 30;
            private int retryAttempts = 3;
        }
        
        @Data
        public static class BSCScanConfig {
            private String apiKey;
            private String baseUrl = "https://api.bscscan.com/api";
            private int timeoutSeconds = 30;
            private int retryAttempts = 3;
        }
        
        @Data
        public static class CoinGeckoConfig {
            private String apiKey;
            private String baseUrl = "https://api.coingecko.com/api/v3";
            private int timeoutSeconds = 30;
            private int retryAttempts = 3;
        }
        
        @Data
        public static class PancakeSwapConfig {
            private String baseUrl = "https://api.pancakeswap.info/api/v2";
            private int timeoutSeconds = 30;
            private int retryAttempts = 3;
        }
    }
    
    @Data
    public static class DatabaseConfig {
        
        private boolean createViews = true;
        
        private boolean createIndexes = true;
        
        @Min(1)
        private int batchSize = 100;
        
        private boolean optimizeQueries = true;
        
        private ConnectionPoolConfig connectionPool = new ConnectionPoolConfig();
        
        @Data
        public static class ConnectionPoolConfig {
            private int minimumIdle = 5;
            private int maximumPoolSize = 20;
            private long connectionTimeoutMs = 30000;
            private long idleTimeoutMs = 600000;
            private long maxLifetimeMs = 1800000;
        }
    }
    
    @Data
    public static class CacheConfig {
        
        private boolean enabled = true;
        
        @Min(1)
        private int ttlSeconds = 300;
        
        @Min(100)
        private int maxSize = 10000;
        
        private Map<String, Integer> cacheTtls = Map.of(
            "coins", 300,
            "pairs", 300,
            "tokens", 300,
            "statistics", 60
        );
    }
    
    @Data
    public static class SecurityConfig {
        
        private boolean enabled = true;
        
        private boolean honeypotCheck = true;
        
        private boolean rugPullCheck = true;
        
        private boolean scamCheck = true;
        
        @Min(0)
        private int minSecurityScore = 50;
        
        private List<String> blockedAddresses = List.of();
        
        private List<String> whitelistedAddresses = List.of();
    }
}
