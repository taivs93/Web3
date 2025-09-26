package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.entity.NoLombokToken;
import com.kunfeng2002.be002.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service bridge để tích hợp coincrawler module với dự án chính
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CoinCrawlerBridgeService {

    private final TokenRepository tokenRepository;
    private final RealTimeCoinCrawlerService realTimeCoinCrawlerService;

    /**
     * Lấy token mới từ coincrawler và đồng bộ với database chính
     */
    public List<NoLombokToken> getNewTokensFromCoincrawler(int limit) {
        try {
            log.info("Getting new tokens from coincrawler with limit: {}", limit);
            
            // Đảm bảo real-time crawling đang chạy
            if (!realTimeCoinCrawlerService.isCrawling()) {
                log.info("Starting real-time crawling for coincrawler integration");
                realTimeCoinCrawlerService.startRealTimeCrawling();
            }
            
            // Lấy token mới từ database (đã được cập nhật bởi real-time crawling)
            return tokenRepository.findByNetworkAndIsActiveTrueOrderByCreatedAtDesc(
                "BSC", 
                org.springframework.data.domain.PageRequest.of(0, limit)
            );
            
        } catch (Exception e) {
            log.error("Error getting new tokens from coincrawler", e);
            return List.of();
        }
    }

    /**
     * Tìm kiếm token với coincrawler
     */
    public List<NoLombokToken> searchTokensFromCoincrawler(String query, String network, int limit) {
        try {
            log.info("Searching tokens from coincrawler: query={}, network={}, limit={}", 
                    query, network, limit);
            
            // Đảm bảo real-time crawling đang chạy
            if (!realTimeCoinCrawlerService.isCrawling()) {
                log.info("Starting real-time crawling for search integration");
                realTimeCoinCrawlerService.startRealTimeCrawling();
            }
            
            // Tìm kiếm trong database
            if (query.startsWith("0x") && query.length() == 42) {
                // Tìm theo address
                Optional<NoLombokToken> token = tokenRepository.findByAddressAndNetwork(query, network);
                return token.map(List::of).orElse(List.of());
            } else {
                // Tìm theo symbol hoặc name
                return tokenRepository.findBySymbolIgnoreCaseAndNetwork(query, network)
                    .stream()
                    .limit(limit)
                    .toList();
            }
            
        } catch (Exception e) {
            log.error("Error searching tokens from coincrawler", e);
            return List.of();
        }
    }

    /**
     * Lấy top tokens từ coincrawler
     */
    public List<NoLombokToken> getTopTokensFromCoincrawler(String network, int limit) {
        try {
            log.info("Getting top tokens from coincrawler: network={}, limit={}", network, limit);
            
            // Đảm bảo real-time crawling đang chạy
            if (!realTimeCoinCrawlerService.isCrawling()) {
                log.info("Starting real-time crawling for top tokens");
                realTimeCoinCrawlerService.startRealTimeCrawling();
            }
            
            return tokenRepository.findByNetworkAndIsActiveTrueOrderByMarketCapUsdDesc(
                network, 
                org.springframework.data.domain.PageRequest.of(0, limit)
            );
            
        } catch (Exception e) {
            log.error("Error getting top tokens from coincrawler", e);
            return List.of();
        }
    }

    /**
     * Lấy thống kê từ coincrawler
     */
    public CoincrawlerStats getCoincrawlerStats() {
        try {
            RealTimeCoinCrawlerService.CrawlStats crawlStats = realTimeCoinCrawlerService.getCrawlStats();
            
            long totalTokens = tokenRepository.countByNetwork("BSC");
            long activeTokens = tokenRepository.countActiveTokensByNetwork("BSC");
            long verifiedTokens = tokenRepository.countByNetworkAndIsVerifiedTrue("BSC");
            
            return new CoincrawlerStats(
                crawlStats.isRunning(),
                crawlStats.getLastProcessedBlock(),
                crawlStats.getTotalNewTokens(),
                totalTokens,
                activeTokens,
                verifiedTokens
            );
                
        } catch (Exception e) {
            log.error("Error getting coincrawler stats", e);
            return new CoincrawlerStats(
                false,
                0L,
                0L,
                0L,
                0L,
                0L
            );
        }
    }

    /**
     * Lớp thống kê coincrawler
     */
    public static class CoincrawlerStats {
        private final boolean isCrawling;
        private final long lastProcessedBlock;
        private final long totalNewTokens;
        private final long totalTokens;
        private final long activeTokens;
        private final long verifiedTokens;
        
        public CoincrawlerStats(boolean isCrawling, long lastProcessedBlock, long totalNewTokens, 
                               long totalTokens, long activeTokens, long verifiedTokens) {
            this.isCrawling = isCrawling;
            this.lastProcessedBlock = lastProcessedBlock;
            this.totalNewTokens = totalNewTokens;
            this.totalTokens = totalTokens;
            this.activeTokens = activeTokens;
            this.verifiedTokens = verifiedTokens;
        }
        
        public boolean isCrawling() { return isCrawling; }
        public long getLastProcessedBlock() { return lastProcessedBlock; }
        public long getTotalNewTokens() { return totalNewTokens; }
        public long getTotalTokens() { return totalTokens; }
        public long getActiveTokens() { return activeTokens; }
        public long getVerifiedTokens() { return verifiedTokens; }
    }
}
