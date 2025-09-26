package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.entity.NoLombokToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramSearchService {

    private final SearchService searchService;
    private final BSCScanService bscScanService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final int MAX_RESULTS = 5;
    public String searchBlock(String query, String network) {
        try {
            return "ERROR: Block search not implemented yet";
        } catch (Exception e) {
            log.error("Error searching block", e);
            return "ERROR: Lỗi khi tìm kiếm block: " + e.getMessage();
        }
    }

    public String searchTransaction(String query, String network) {
        try {
            return "ERROR: Transaction search not implemented yet";
        } catch (Exception e) {
            log.error("Error searching transaction", e);
            return "ERROR Lỗi khi tìm kiếm transaction: " + e.getMessage();
        }
    }

    public String searchAddress(String query, String network) {
        try {
            return "ERROR: Address search not implemented yet";
        } catch (Exception e) {
            log.error("Error searching address", e);
            return "ERROR Lỗi khi tìm kiếm địa chỉ: " + e.getMessage();
        }
    }

    public String searchToken(String query, String network) {
        try {
            List<NoLombokToken> tokens = searchService.searchToken(query, network, 0, MAX_RESULTS);
            
            if (tokens.isEmpty()) {
                return "ERROR Không tìm thấy token: " + query;
            }

            StringBuilder response = new StringBuilder();
            response.append(" **Tokens Found**\n\n");
            
            for (int i = 0; i < Math.min(MAX_RESULTS, tokens.size()); i++) {
                var token = tokens.get(i);
                response.append(String.format("""
                    **%s** (%s)
                    Address: `%s`
                    Market Cap: %s
                    Price: $%s
                    
                    """,
                    token.getName(),
                    token.getSymbol(),
                    token.getAddress(),
                    formatDollar(token.getMarketCapUsd()),
                    formatDollar(token.getPriceUsd())
                ));
            }
            
            return response.toString();
        } catch (Exception e) {
            log.error("Error searching token", e);
            return "ERROR Lỗi khi tìm kiếm token: " + e.getMessage();
        }
    }

    public String generalSearch(String query, String network) {
        try {
            return "ERROR: General search not implemented yet";
        } catch (Exception e) {
            log.error("Error in general search", e);
            return "ERROR Lỗi khi tìm kiếm: " + e.getMessage();
        }
    }

    public String getNetworkStats(String network) {
        try {
            return "ERROR: Network stats not implemented yet";
        } catch (Exception e) {
            log.error("Error getting network stats", e);
            return "ERROR Lỗi khi lấy thống kê network: " + e.getMessage();
        }
    }

    public String getTopTokens(String network, int limit) {
        try {
            List<NoLombokToken> tokens = searchService.getTopTokens(network, limit);
            
            if (tokens.isEmpty()) {
                return "ERROR Không có token nào trong network: " + network;
            }

            StringBuilder response = new StringBuilder();
            response.append(" **Top Tokens - ").append(network).append("**\n\n");
            
            for (int i = 0; i < tokens.size(); i++) {
                var token = tokens.get(i);
                response.append(String.format("""
                    **%d. %s** (%s)
                    Market Cap: %s
                    Price: $%s
                    Change 24h: %s%s%%
                    
                    """,
                    i + 1,
                    token.getName(),
                    token.getSymbol(),
                    formatDollar(token.getMarketCapUsd()),
                    formatDollar(token.getPriceUsd()),
                    token.getPriceChange24h() != null && token.getPriceChange24h().compareTo(BigDecimal.ZERO) >= 0 ? "Tăng +" : "Giảm ",
                    token.getPriceChange24h() != null ? String.format("%.2f", token.getPriceChange24h().doubleValue() / 1e18) : "N/A"
                ));
            }

            return response.toString();
        } catch (Exception e) {
            log.error("Error getting top tokens", e);
            return "ERROR Lỗi khi lấy top tokens: " + e.getMessage();
        }
    }

    public String getNewTokens(int count) {
        try {
            log.info("Fetching {} new tokens with real-time crawling", count);
            
            bscScanService.ensureRealTimeCrawling();
            
            List<NoLombokToken> newTokens = bscScanService.getNewTokensWithRealTime(count);
            
            if (newTokens.isEmpty()) {
                log.info("No new tokens found, trying fallback to BSCScan API");
                newTokens = bscScanService.getNewTokens(count);
            }
            
            if (newTokens.isEmpty()) {
                return "ERROR Không tìm thấy token mới nào trên BSCScan mainnet";
            }

            StringBuilder response = new StringBuilder();
            response.append(" **New Tokens - BSC Mainnet**\n\n");
            
            for (int i = 0; i < newTokens.size(); i++) {
                var token = newTokens.get(i);
                response.append(String.format("""
                    **%d. %s** (%s)
                    Address: `%s`
                    Decimals: %d
                    Total Supply: %s
                    Market Cap: %s
                    Price: $%s
                    Network: %s
                    Verified: %s
                    
                    """,
                    i + 1,
                    token.getName(),
                    token.getSymbol(),
                    token.getAddress(),
                    token.getDecimals(),
                    formatTokenAmount(token.getTotalSupply(), token.getDecimals()),
                    formatDollar(token.getMarketCapUsd()),
                    formatDollar(token.getPriceUsd()),
                    "BSC",
                    token.getIsVerified() ? "OK" : "ERROR"
                ));
            }

            return response.toString();
        } catch (Exception e) {
            log.error("Error getting new tokens", e);
            return "ERROR Lỗi khi lấy token mới: " + e.getMessage();
        }
    }

    private String formatNumber(BigInteger number) {
        if (number == null) return "N/A";
        return String.format("%,d", number);
    }

    private String formatEther(BigInteger wei) {
        if (wei == null) return "0";
        return String.format("%.6f", wei.doubleValue() / 1e18);
    }

    private String formatGwei(BigInteger wei) {
        if (wei == null) return "N/A";
        return String.format("%.2f", wei.doubleValue() / 1e9);
    }

    private String formatDollar(BigDecimal amount) {
        if (amount == null) return "N/A";
        double value = amount.doubleValue() / 1e18;
        if (value >= 1e9) {
            return String.format("$%.2fB", value / 1e9);
        } else if (value >= 1e6) {
            return String.format("$%.2fM", value / 1e6);
        } else if (value >= 1e3) {
            return String.format("$%.2fK", value / 1e3);
        } else {
            return String.format("$%.2f", value);
        }
    }

    private String formatTokenAmount(BigDecimal amount, Integer decimals) {
        if (amount == null || decimals == null) return "N/A";
        double value = amount.doubleValue() / Math.pow(10, decimals);
        if (value >= 1e9) {
            return String.format("%.2fB", value / 1e9);
        } else if (value >= 1e6) {
            return String.format("%.2fM", value / 1e6);
        } else if (value >= 1e3) {
            return String.format("%.2fK", value / 1e3);
        } else {
            return String.format("%.2f", value);
        }
    }

    private String getStatusEmoji(String status) {
        return switch (status) {
            case "SUCCESS" -> "OK";
            case "FAILED" -> "ERROR";
            case "PENDING" -> "PENDING";
            default -> "UNKNOWN";
        };
    }
}

