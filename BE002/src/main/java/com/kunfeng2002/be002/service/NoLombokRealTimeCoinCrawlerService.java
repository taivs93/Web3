package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.entity.NoLombokToken;
import com.kunfeng2002.be002.repository.NoLombokTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class NoLombokRealTimeCoinCrawlerService {
    
    @Autowired
    private NoLombokTokenRepository tokenRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicLong lastProcessedBlock = new AtomicLong(0);
    private final AtomicLong totalNewTokens = new AtomicLong(0);
    
    private static final String BSCSCAN_API_KEY = "YourBSCScanAPIKey";
    private static final String BSCSCAN_GET_LATEST_BLOCK_URL = "https://api.bscscan.com/api?module=proxy&action=eth_blockNumber&apikey=" + BSCSCAN_API_KEY;
    private static final String BSCSCAN_GET_BLOCK_URL = "https://api.bscscan.com/api?module=proxy&action=eth_getBlockByNumber&tag={blockNumber}&boolean=true&apikey=" + BSCSCAN_API_KEY;
    
    public void startCrawling() {
        if (isRunning.compareAndSet(false, true)) {
            System.out.println("Starting real-time coin crawling...");
            initializeLastProcessedBlock();
            runCrawlingLoop();
        } else {
            System.out.println("Crawling is already running");
        }
    }
    
    public void stopCrawling() {
        if (isRunning.compareAndSet(true, false)) {
            System.out.println("Stopping real-time coin crawling...");
        } else {
            System.out.println("Crawling is not running");
        }
    }
    
    public boolean isRunning() {
        return isRunning.get();
    }
    
    public long getLastProcessedBlock() {
        return lastProcessedBlock.get();
    }
    
    public long getTotalNewTokens() {
        return totalNewTokens.get();
    }
    
    private void initializeLastProcessedBlock() {
        try {
            String response = restTemplate.getForObject(BSCSCAN_GET_LATEST_BLOCK_URL, String.class);
            if (response != null && response.contains("\"result\"")) {
                String result = extractResultFromResponse(response);
                if (result != null && !result.contains("deprecated") && !result.contains("error") && result.startsWith("0x")) {
                    long currentBlock = Long.parseLong(result.substring(2), 16);
                    lastProcessedBlock.set(currentBlock - 100);
                    System.out.println("Initialized last processed block: " + lastProcessedBlock.get());
                } else {
                    long estimatedBlock = System.currentTimeMillis() / 1000 / 3;
                    lastProcessedBlock.set(estimatedBlock - 100);
                    System.out.println("Using estimated block number: " + lastProcessedBlock.get());
                }
            } else {
                long estimatedBlock = System.currentTimeMillis() / 1000 / 3;
                lastProcessedBlock.set(estimatedBlock - 100);
                System.out.println("Using estimated block number: " + lastProcessedBlock.get());
            }
        } catch (Exception e) {
            long estimatedBlock = System.currentTimeMillis() / 1000 / 3;
            lastProcessedBlock.set(estimatedBlock - 100);
            System.out.println("Error initializing block number, using estimated: " + lastProcessedBlock.get());
        }
    }
    
    private void runCrawlingLoop() {
        new Thread(() -> {
            while (isRunning.get()) {
                try {
                    processNewBlocks();
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in crawling loop: " + e.getMessage());
                }
            }
            System.out.println("Crawling loop stopped");
        }).start();
    }
    
    private void processNewBlocks() {
        try {
            long currentBlock = getCurrentBlockNumber();
            long lastBlock = lastProcessedBlock.get();
            
            if (currentBlock > lastBlock) {
                System.out.println("Processing blocks from " + (lastBlock + 1) + " to " + currentBlock);
                
                for (long blockNumber = lastBlock + 1; blockNumber <= currentBlock; blockNumber++) {
                    processBlock(blockNumber);
                }
                
                lastProcessedBlock.set(currentBlock);
            }
        } catch (Exception e) {
            System.err.println("Error processing new blocks: " + e.getMessage());
        }
    }
    
    private long getCurrentBlockNumber() {
        try {
            String response = restTemplate.getForObject(BSCSCAN_GET_LATEST_BLOCK_URL, String.class);
            if (response != null && response.contains("\"result\"")) {
                String result = extractResultFromResponse(response);
                if (result != null && !result.contains("deprecated") && !result.contains("error") && result.startsWith("0x")) {
                    return Long.parseLong(result.substring(2), 16);
                }
            }
            return lastProcessedBlock.get() + 1;
        } catch (Exception e) {
            return lastProcessedBlock.get() + 1;
        }
    }
    
    private void processBlock(long blockNumber) {
        try {
            String blockHex = "0x" + Long.toHexString(blockNumber);
            String url = BSCSCAN_GET_BLOCK_URL.replace("{blockNumber}", blockHex);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getBody() != null) {
                Map<String, Object> blockData = response.getBody();
                List<Map<String, Object>> transactions = (List<Map<String, Object>>) blockData.get("transactions");
                
                if (transactions != null) {
                    List<NoLombokToken> newTokens = new ArrayList<>();
                    
                    for (Map<String, Object> tx : transactions) {
                        String to = (String) tx.get("to");
                        if (to != null && to.startsWith("0x") && to.length() == 42) {
                            if (isTokenContract(to)) {
                                NoLombokToken token = createTokenFromContract(to, blockNumber);
                                if (token != null) {
                                    newTokens.add(token);
                                }
                            }
                        }
                    }
                    
                    if (!newTokens.isEmpty()) {
                        tokenRepository.saveAll(newTokens);
                        totalNewTokens.addAndGet(newTokens.size());
                        System.out.println("Saved " + newTokens.size() + " new tokens from block " + blockNumber);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing block " + blockNumber + ": " + e.getMessage());
        }
    }
    
    private boolean isTokenContract(String address) {
        try {
            String url = "https://api.bscscan.com/api?module=proxy&action=eth_call&to=" + address + 
                        "&data=0x06fdde03&tag=latest&apikey=" + BSCSCAN_API_KEY;
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                String data = (String) result.get("result");
                return data != null && !data.equals("0x") && data.length() > 2;
            }
        } catch (Exception e) {
            System.err.println("Error checking token contract: " + e.getMessage());
        }
        return false;
    }
    
    private NoLombokToken createTokenFromContract(String contractAddress, long blockNumber) {
        try {
            String name = getTokenName(contractAddress);
            String symbol = getTokenSymbol(contractAddress);
            Integer decimals = getTokenDecimals(contractAddress);
            BigDecimal totalSupply = getTokenTotalSupply(contractAddress);
            
            if (name != null && symbol != null) {
                NoLombokToken token = new NoLombokToken(contractAddress, name, symbol);
                token.setDecimals(decimals);
                token.setTotalSupply(totalSupply);
                token.setCreatedAtTimestamp(blockNumber);
                token.setLastSyncedAt(LocalDateTime.now());
                
                return token;
            }
        } catch (Exception e) {
            System.err.println("Error creating token from contract " + contractAddress + ": " + e.getMessage());
        }
        return null;
    }
    
    private String getTokenName(String contractAddress) {
        try {
            String url = "https://api.bscscan.com/api?module=proxy&action=eth_call&to=" + contractAddress + 
                        "&data=0x06fdde03&tag=latest&apikey=" + BSCSCAN_API_KEY;
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                String data = (String) result.get("result");
                if (data != null && data.length() > 2) {
                    return decodeString(data);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting token name: " + e.getMessage());
        }
        return null;
    }
    
    private String getTokenSymbol(String contractAddress) {
        try {
            String url = "https://api.bscscan.com/api?module=proxy&action=eth_call&to=" + contractAddress + 
                        "&data=0x95d89b41&tag=latest&apikey=" + BSCSCAN_API_KEY;
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                String data = (String) result.get("result");
                if (data != null && data.length() > 2) {
                    return decodeString(data);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting token symbol: " + e.getMessage());
        }
        return null;
    }
    
    private Integer getTokenDecimals(String contractAddress) {
        try {
            String url = "https://api.bscscan.com/api?module=proxy&action=eth_call&to=" + contractAddress + 
                        "&data=0x313ce567&tag=latest&apikey=" + BSCSCAN_API_KEY;
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                String data = (String) result.get("result");
                if (data != null && data.length() > 2) {
                    return Integer.parseInt(data.substring(2), 16);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting token decimals: " + e.getMessage());
        }
        return 18;
    }
    
    private BigDecimal getTokenTotalSupply(String contractAddress) {
        try {
            String url = "https://api.bscscan.com/api?module=proxy&action=eth_call&to=" + contractAddress + 
                        "&data=0x18160ddd&tag=latest&apikey=" + BSCSCAN_API_KEY;
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                String data = (String) result.get("result");
                if (data != null && data.length() > 2) {
                    return new BigDecimal(new java.math.BigInteger(data.substring(2), 16));
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting token total supply: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
    
    private String extractResultFromResponse(String response) {
        try {
            int start = response.indexOf("\"result\":\"") + 10;
            int end = response.indexOf("\"", start);
            if (start > 9 && end > start) {
                return response.substring(start, end);
            }
        } catch (Exception e) {
            System.err.println("Error extracting result from response: " + e.getMessage());
        }
        return null;
    }
    
    private String decodeString(String hexData) {
        try {
            if (hexData.length() < 66) return null;
            
            String lengthHex = hexData.substring(2, 66);
            int length = Integer.parseInt(lengthHex, 16);
            
            if (length == 0) return "";
            
            String dataHex = hexData.substring(66, 66 + length * 2);
            StringBuilder result = new StringBuilder();
            
            for (int i = 0; i < dataHex.length(); i += 2) {
                String hex = dataHex.substring(i, i + 2);
                int charCode = Integer.parseInt(hex, 16);
                if (charCode > 0) {
                    result.append((char) charCode);
                }
            }
            
            return result.toString().trim();
        } catch (Exception e) {
            System.err.println("Error decoding string: " + e.getMessage());
            return null;
        }
    }
}
