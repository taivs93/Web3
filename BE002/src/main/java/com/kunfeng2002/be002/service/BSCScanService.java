package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.entity.Token;
import com.kunfeng2002.be002.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BSCScanService {

    private final RestTemplate restTemplate;
    private final TokenRepository tokenRepository;
    private final RealTimeCoinCrawlerService realTimeCoinCrawlerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${bscscan.api.url:https://api.bscscan.com/api}")
    private String bscScanApiUrl;

    @Value("${bscscan.api.key:}")
    private String bscScanApiKey;

    private static final String BSC_MAINNET = "BSC";
    private static final String BSCSCAN_GET_TOKEN_LIST_URL = "?module=token&action=getTokenList&page=1&offset={offset}&sort=timestamp&order=desc";

    public List<Token> getNewTokens(int count) {
        try {
            log.info("Fetching {} new tokens from BSCScan", count);
            
            String url = bscScanApiUrl + BSCSCAN_GET_TOKEN_LIST_URL;
            if (!bscScanApiKey.isEmpty()) {
                url += "&apikey=" + bscScanApiKey;
            }

            String response = restTemplate.getForObject(url, String.class, count);
            if (response == null) {
                log.warn("Empty response from BSCScan API");
                return new ArrayList<>();
            }

            JsonNode jsonResponse = objectMapper.readTree(response);
            if (!jsonResponse.get("status").asText().equals("1")) {
                log.error("BSCScan API error: {}", jsonResponse.get("message").asText());
                return new ArrayList<>();
            }

            JsonNode result = jsonResponse.get("result");
            if (!result.isArray()) {
                log.warn("Invalid result format from BSCScan API");
                return new ArrayList<>();
            }

            List<Token> newTokens = new ArrayList<>();
            for (JsonNode tokenNode : result) {
                try {
                    Token token = parseTokenFromBSCScan(tokenNode);
                    if (token != null) {
                        if (!tokenRepository.findByTokenAddressAndNetwork(token.getTokenAddress(), BSC_MAINNET).isPresent()) {
                            newTokens.add(token);
                            if (newTokens.size() >= count) {
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error parsing token from BSCScan: {}", e.getMessage());
                }
            }

            if (!newTokens.isEmpty()) {
                tokenRepository.saveAll(newTokens);
                log.info("Saved {} new tokens to database", newTokens.size());
            }

            return newTokens;

        } catch (Exception e) {
            log.error("Error fetching new tokens from BSCScan", e);
            return new ArrayList<>();
        }
    }

    private Token parseTokenFromBSCScan(JsonNode tokenNode) {
        try {
            String contractAddress = tokenNode.get("contractAddress").asText();
            String name = tokenNode.get("tokenName").asText();
            String symbol = tokenNode.get("symbol").asText();
            int decimals = tokenNode.get("divisor").asInt();

            TokenInfo tokenInfo = getTokenInfo(contractAddress);
            
            return Token.builder()
                    .tokenAddress(contractAddress)
                    .name(name)
                    .symbol(symbol)
                    .decimals(decimals)
                    .network(BSC_MAINNET)
                    .isVerified(true)
                    .isActive(true)
                    .totalSupply(tokenInfo != null ? tokenInfo.totalSupply : null)
                    .marketCap(tokenInfo != null ? tokenInfo.marketCap : null)
                    .priceUsd(tokenInfo != null ? tokenInfo.priceUsd : null)
                    .volume24h(tokenInfo != null ? tokenInfo.volume24h : null)
                    .priceChange24h(tokenInfo != null ? tokenInfo.priceChange24h : null)
                    .lastPriceUpdate(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error parsing token from BSCScan response", e);
            return null;
        }
    }

    private TokenInfo getTokenInfo(String contractAddress) {
        try {
            String url = bscScanApiUrl + "?module=token&action=tokeninfo&contractaddress=" + contractAddress;
            if (!bscScanApiKey.isEmpty()) {
                url += "&apikey=" + bscScanApiKey;
            }

            String response = restTemplate.getForObject(url, String.class);
            if (response == null) return null;

            JsonNode jsonResponse = objectMapper.readTree(response);
            if (!jsonResponse.get("status").asText().equals("1")) {
                return null;
            }

            JsonNode result = jsonResponse.get("result");
            if (result == null || !result.isArray() || result.size() == 0) {
                return null;
            }

            JsonNode tokenData = result.get(0);
            TokenInfo tokenInfo = new TokenInfo();

            String totalSupplyStr = tokenData.get("totalSupply").asText();
            if (!totalSupplyStr.isEmpty()) {
                tokenInfo.totalSupply = new BigInteger(totalSupplyStr);
            }

            String marketCapStr = tokenData.get("marketCap").asText();
            if (!marketCapStr.isEmpty()) {
                try {
                    tokenInfo.marketCap = new BigInteger(marketCapStr);
                } catch (NumberFormatException e) {
                    log.debug("Invalid market cap format: {}", marketCapStr);
                }
            }

            return tokenInfo;

        } catch (Exception e) {
            log.debug("Error fetching token info for {}: {}", contractAddress, e.getMessage());
            return null;
        }
    }

    public List<Token> getLatestTokensFromDB(int count) {
        try {
            return tokenRepository.findByNetworkAndIsActiveTrueOrderByMarketCapDesc(
                    BSC_MAINNET, 
                    org.springframework.data.domain.PageRequest.of(0, count)
            );
        } catch (Exception e) {
            log.error("Error fetching latest tokens from database", e);
            return new ArrayList<>();
        }
    }

    /**
     * Lấy token mới với real-time crawling
     */
    public List<Token> getNewTokensWithRealTime(int count) {
        try {
            // Kiểm tra nếu real-time crawling đang chạy
            if (realTimeCoinCrawlerService.isCrawling()) {
                log.info("Real-time crawling is active, getting latest tokens from DB");
                return getLatestTokensFromDB(count);
            } else {
                // Fallback về API cũ
                log.info("Real-time crawling is not active, using BSCScan API");
                return getNewTokens(count);
            }
        } catch (Exception e) {
            log.error("Error getting new tokens with real-time", e);
            return new ArrayList<>();
        }
    }

    /**
     * Bắt đầu real-time crawling nếu chưa chạy
     */
    public void ensureRealTimeCrawling() {
        if (!realTimeCoinCrawlerService.isCrawling()) {
            log.info("Starting real-time crawling to ensure fresh data");
            realTimeCoinCrawlerService.startRealTimeCrawling();
        }
    }

    private static class TokenInfo {
        BigInteger totalSupply;
        BigInteger marketCap;
        BigInteger priceUsd;
        BigInteger volume24h;
        BigDecimal priceChange24h;
    }
}
