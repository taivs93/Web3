package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.entity.NoLombokToken;
import com.kunfeng2002.be002.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class RealTimeCoinCrawlerService {

    private final RestTemplate restTemplate;
    private final TokenRepository tokenRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${api.bscscan.base-url:https://api.bscscan.com/api}")
    private String bscScanApiUrl;
    
    @Value("${api.bscscan.api-key:}")
    private String bscScanApiKey;
    
    @Value("${crawler.realtime.enabled:true}")
    private boolean realtimeEnabled;
    
    @Value("${crawler.realtime.interval:30000}")
    private long crawlIntervalMs;
    
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicLong lastProcessedBlock = new AtomicLong(0);
    private final AtomicLong totalNewTokens = new AtomicLong(0);
    
    private static final String BSC_MAINNET = "BSC";
    private static final String BSCSCAN_GET_LATEST_BLOCK_URL = "?module=proxy&action=eth_blockNumber";
    private static final String BSCSCAN_GET_BLOCK_BY_NUMBER_URL = "?module=proxy&action=eth_getBlockByNumber&tag={blockNumber}&boolean=true";
    private static final String BSCSCAN_GET_TOKEN_INFO_URL = "?module=token&action=tokeninfo&contractaddress={address}";
    
    public void startRealTimeCrawling() {
        if (isRunning.compareAndSet(false, true)) {
            log.info("Starting real-time coin crawling...");
            initializeLastProcessedBlock();
            crawlNewTokensAsync();
        } else {
            log.warn("Real-time crawling is already running");
        }
    }
    
    public void stopRealTimeCrawling() {
        if (isRunning.compareAndSet(true, false)) {
            log.info("Stopping real-time coin crawling...");
        } else {
            log.warn("Real-time crawling is not running");
        }
    }
    
    public boolean isCrawling() {
        return isRunning.get();
    }
    
    public CrawlStats getCrawlStats() {
        return new CrawlStats(
                isRunning.get(),
                lastProcessedBlock.get(),
                totalNewTokens.get()
        );
    }
    
    private void initializeLastProcessedBlock() {
        try {
            String url = bscScanApiUrl + BSCSCAN_GET_LATEST_BLOCK_URL;
            if (!bscScanApiKey.isEmpty()) {
                url += "&apikey=" + bscScanApiKey;
            }
            
            String response = restTemplate.getForObject(url, String.class);
            if (response != null) {
                JsonNode jsonResponse = objectMapper.readTree(response);
                if (jsonResponse.has("result")) {
                    String blockHex = jsonResponse.get("result").asText();
                    long currentBlock = Long.parseLong(blockHex.substring(2), 16);
                    lastProcessedBlock.set(currentBlock);
                    log.info("Initialized last processed block: {}", currentBlock);
                }
            }
        } catch (Exception e) {
            log.error("Error initializing last processed block", e);
        }
    }
    
    @Async
    public void crawlNewTokensAsync() {
        while (isRunning.get()) {
            try {
                crawlNewTokensFromBlocks();
                Thread.sleep(crawlIntervalMs);
            } catch (InterruptedException e) {
                log.warn("Real-time crawling interrupted");
                break;
            } catch (Exception e) {
                log.error("Error in real-time crawling", e);
                try {
                    Thread.sleep(crawlIntervalMs);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
        log.info("Real-time crawling stopped");
    }
    
    private void crawlNewTokensFromBlocks() {
        try {
            long currentBlock = getCurrentBlockNumber();
            if (currentBlock <= lastProcessedBlock.get()) {
                return;
            }
            
            log.info("Processing blocks from {} to {}", lastProcessedBlock.get() + 1, currentBlock);
            for (long blockNum = lastProcessedBlock.get() + 1; blockNum <= currentBlock; blockNum++) {
                if (!isRunning.get()) {
                    break;
                }
                
                processBlock(blockNum);
                lastProcessedBlock.set(blockNum);
            }
            
        } catch (Exception e) {
            log.error("Error crawling new tokens from blocks", e);
        }
    }
    
    private long getCurrentBlockNumber() {
        try {
            String url = bscScanApiUrl + BSCSCAN_GET_LATEST_BLOCK_URL;
            if (!bscScanApiKey.isEmpty()) {
                url += "&apikey=" + bscScanApiKey;
            }
            
            String response = restTemplate.getForObject(url, String.class);
            if (response != null) {
                log.debug("BSCScan response: {}", response);
                JsonNode jsonResponse = objectMapper.readTree(response);
                if (jsonResponse.has("result")) {
                    String blockHex = jsonResponse.get("result").asText();
                    // Kiểm tra xem có phải là hex number không
                    if (blockHex.startsWith("0x") && blockHex.length() > 2) {
                        return Long.parseLong(blockHex.substring(2), 16);
                    } else {
                        log.warn("Invalid block number format: {}", blockHex);
                    }
                } else {
                    log.warn("No result in BSCScan response: {}", response);
                }
            }
        } catch (Exception e) {
            log.error("Error getting current block number", e);
        }
        return lastProcessedBlock.get();
    }
    
    private void processBlock(long blockNumber) {
        try {
            // Chỉ log block được xử lý, không tạo token từ block nữa
            log.debug("Processing block {} - Token creation from blocks is disabled", blockNumber);
            
            // Vẫn giữ việc đọc block để theo dõi, nhưng không tạo token
            String blockHex = "0x" + Long.toHexString(blockNumber);
            String url = bscScanApiUrl + BSCSCAN_GET_BLOCK_BY_NUMBER_URL;
            if (!bscScanApiKey.isEmpty()) {
                url += "&apikey=" + bscScanApiKey;
            }
            
            String response = restTemplate.getForObject(url, String.class, blockHex);
            if (response == null) {
                return;
            }
            
            JsonNode jsonResponse = objectMapper.readTree(response);
            if (!jsonResponse.has("result")) {
                return;
            }
            
            JsonNode block = jsonResponse.get("result");
            if (!block.has("transactions")) {
                return;
            }
            
            JsonNode transactions = block.get("transactions");
            if (!transactions.isArray()) {
                return;
            }
            
            // Chỉ log thông tin block, không tạo token
            log.debug("Block {} has {} transactions - Token creation disabled", blockNumber, transactions.size());
            
            // Không tạo token từ block nữa - chỉ theo dõi blockchain
            
        } catch (Exception e) {
            log.error("Error processing block {}", blockNumber, e);
        }
    }
    
    private NoLombokToken createTokenFromContract(String contractAddress, long blockNumber) {
        try {
            String url = bscScanApiUrl + BSCSCAN_GET_TOKEN_INFO_URL;
            if (!bscScanApiKey.isEmpty()) {
                url += "&apikey=" + bscScanApiKey;
            }
            
            String response = restTemplate.getForObject(url, String.class, contractAddress);
            if (response == null) {
                return null;
            }
            
            JsonNode jsonResponse = objectMapper.readTree(response);
            if (!jsonResponse.get("status").asText().equals("1")) {
                return null;
            }
            
            JsonNode result = jsonResponse.get("result");
            if (!result.isArray() || result.size() == 0) {
                return null;
            }
            
            JsonNode tokenData = result.get(0);
            
            String name = tokenData.get("tokenName").asText();
            String symbol = tokenData.get("symbol").asText();
            int decimals = tokenData.get("divisor").asInt();
            
            NoLombokToken token = new NoLombokToken(contractAddress, name, symbol);
            token.setDecimals(decimals);
            token.setIsVerified(true);
            token.setCreatedAtTimestamp(System.currentTimeMillis() / 1000);
            token.setLastSyncedAt(LocalDateTime.now());
            
            if (tokenData.has("totalSupply") && !tokenData.get("totalSupply").asText().isEmpty()) {
                try {
                    BigInteger totalSupply = new BigInteger(tokenData.get("totalSupply").asText());
                    token.setTotalSupply(new BigDecimal(totalSupply));
                } catch (NumberFormatException e) {
                    log.debug("Invalid total supply format for token {}", contractAddress);
                }
            }
            
            return token;
            
        } catch (Exception e) {
            log.error("Error creating token from contract {}", contractAddress, e);
            return null;
        }
    }
    
    @Scheduled(fixedDelayString = "${crawler.realtime.interval:30000}")
    public void scheduledCrawl() {
        if (realtimeEnabled && !isRunning.get()) {
            startRealTimeCrawling();
        }
    }
    
    public static class CrawlStats {
        private final boolean isRunning;
        private final long lastProcessedBlock;
        private final long totalNewTokens;
        
        public CrawlStats(boolean isRunning, long lastProcessedBlock, long totalNewTokens) {
            this.isRunning = isRunning;
            this.lastProcessedBlock = lastProcessedBlock;
            this.totalNewTokens = totalNewTokens;
        }
        
        public boolean isRunning() { return isRunning; }
        public long getLastProcessedBlock() { return lastProcessedBlock; }
        public long getTotalNewTokens() { return totalNewTokens; }
    }
}
