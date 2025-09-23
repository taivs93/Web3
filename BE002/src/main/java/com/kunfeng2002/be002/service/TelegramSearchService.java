package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.response.*;
import com.kunfeng2002.be002.entity.Token;
import com.kunfeng2002.be002.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
            SearchService.BlockSearchResult result = searchService.searchBlock(query, network);
            
            if (!result.isFound()) {
                return "ERROR: Không tìm thấy block với query: " + query;
            }

            var block = result.getBlock();
            return String.format("""
                **Block Found**
                
                **Block Number:** `%s`
                **Block Hash:** `%s`
                **Network:** %s
                **Timestamp:** %s
                **Gas Used:** %s / %s
                **Transactions:** %d
                **Size:** %d bytes
                **Difficulty:** %s
                """,
                block.getBlockNumber(),
                block.getBlockHash(),
                block.getNetwork(),
                block.getTimestamp().format(DATE_FORMATTER),
                formatNumber(block.getGasUsed()),
                formatNumber(block.getGasLimit()),
                block.getTransactionCount(),
                block.getSize(),
                formatNumber(block.getDifficulty())
            );
        } catch (Exception e) {
            log.error("Error searching block", e);
            return "ERROR: Lỗi khi tìm kiếm block: " + e.getMessage();
        }
    }

    public String searchTransaction(String query, String network) {
        try {
            SearchService.TransactionSearchResult result = searchService.searchTransaction(query, network);
            
            if (!result.isFound()) {
                return "ERROR: Không tìm thấy transaction với hash: " + query;
            }

            var tx = result.getTransaction();
            return String.format("""
                **Transaction Found**
                
                **Hash:** `%s`
                **Block:** %s
                **From:** `%s`
                **To:** `%s`
                **Value:** %s ETH
                **Gas:** %s (Used: %s)
                **Gas Price:** %s Gwei
                **Status:** %s
                **Timestamp:** %s
                """,
                tx.getTransactionHash(),
                tx.getBlockNumber(),
                tx.getFromAddress() != null ? tx.getFromAddress() : "Contract Creation",
                tx.getToAddress() != null ? tx.getToAddress() : "Contract Creation",
                formatEther(tx.getValue()),
                formatNumber(tx.getGas()),
                formatNumber(tx.getGasUsed()),
                formatGwei(tx.getGasPrice()),
                getStatusEmoji(tx.getStatus()),
                tx.getTimestamp().format(DATE_FORMATTER)
            );
        } catch (Exception e) {
            log.error("Error searching transaction", e);
            return "ERROR Lỗi khi tìm kiếm transaction: " + e.getMessage();
        }
    }

    public String searchAddress(String query, String network) {
        try {
            SearchService.AddressSearchResult result = searchService.searchAddress(query, network, 0, MAX_RESULTS);
            
            if (!result.isFound()) {
                return "ERROR Không tìm thấy địa chỉ: " + query;
            }

            StringBuilder response = new StringBuilder();
            response.append(" **Address Found**\n\n");
            response.append("**Address:** `").append(query).append("`\n");
            response.append("**Network:** ").append(network).append("\n");
            response.append("**Total Transactions:** ").append(result.getTotalTransactions()).append("\n");
            response.append("**Total Sent:** ").append(formatEther(result.getTotalSent())).append(" ETH\n");
            response.append("**Total Received:** ").append(formatEther(result.getTotalReceived())).append(" ETH\n\n");

            if (!result.getTransactions().isEmpty()) {
                response.append(" **Recent Transactions:**\n");
                for (int i = 0; i < Math.min(MAX_RESULTS, result.getTransactions().size()); i++) {
                    var tx = result.getTransactions().get(i);
                    response.append(String.format("""
                        • `%s` %s %s ETH
                          Block: %s | %s
                        """,
                        tx.getTransactionHash().substring(0, 10) + "...",
                        tx.getFromAddress() != null && tx.getFromAddress().equalsIgnoreCase(query) ? "→" : "←",
                        formatEther(tx.getValue()),
                        tx.getBlockNumber(),
                        tx.getTimestamp().format(DATE_FORMATTER)
                    ));
                }
            }

            return response.toString();
        } catch (Exception e) {
            log.error("Error searching address", e);
            return "ERROR Lỗi khi tìm kiếm địa chỉ: " + e.getMessage();
        }
    }

    public String searchToken(String query, String network) {
        try {
            SearchService.TokenSearchResult result = searchService.searchToken(query, network, 0, MAX_RESULTS);
            
            if (!result.isFound()) {
                return "ERROR Không tìm thấy token: " + query;
            }

            if (result.getToken() != null) {
                var token = result.getToken();
                return String.format("""
 **Token Found**
                    
                    **Name:** %s
                    **Symbol:** %s
                    **Address:** `%s`
                    **Network:** %s
                    **Decimals:** %d
                    **Total Supply:** %s
                    **Market Cap:** %s
                    **Price:** $%s
                    **24h Change:** %s%s%%
                    **Volume 24h:** %s
                    """,
                    token.getName(),
                    token.getSymbol(),
                    token.getTokenAddress(),
                    token.getNetwork(),
                    token.getDecimals(),
                    formatTokenAmount(token.getTotalSupply(), token.getDecimals()),
                    formatDollar(token.getMarketCap()),
                    formatDollar(token.getPriceUsd()),
                    token.getPriceChange24h() != null && token.getPriceChange24h().compareTo(BigDecimal.ZERO) >= 0 ? " Tăng+" : " Giảm",
                    token.getPriceChange24h() != null ? String.format("%.2f", token.getPriceChange24h()) : "N/A",
                    formatDollar(token.getVolume24h())
                );
            }

            if (!result.getTokens().isEmpty()) {
                StringBuilder response = new StringBuilder();
                response.append(" **Tokens Found**\n\n");
                
                for (int i = 0; i < Math.min(MAX_RESULTS, result.getTokens().size()); i++) {
                    var token = result.getTokens().get(i);
                    response.append(String.format("""
                        **%s** (%s)
                        Address: `%s`
                        Market Cap: %s
                        Price: $%s
                        
                        """,
                        token.getName(),
                        token.getSymbol(),
                        token.getTokenAddress(),
                        formatDollar(token.getMarketCap()),
                        formatDollar(token.getPriceUsd())
                    ));
                }
                
                return response.toString();
            }

            return "ERROR Không tìm thấy token: " + query;
        } catch (Exception e) {
            log.error("Error searching token", e);
            return "ERROR Lỗi khi tìm kiếm token: " + e.getMessage();
        }
    }

    public String generalSearch(String query, String network) {
        try {
            SearchService.SearchResult result = searchService.search(query, network, 0, MAX_RESULTS);
            
            if (!result.isFound()) {
                return "ERROR Không tìm thấy kết quả cho: " + query;
            }

            StringBuilder response = new StringBuilder();
            response.append(" **Search Results**\n\n");

            if (result.getTotalBlocks() > 0) {
                response.append(" **Blocks:** ").append(result.getTotalBlocks()).append("\n");
                for (var block : result.getBlocks()) {
                    response.append(String.format("• Block %s | %s\n",
                        block.getBlockNumber(),
                        block.getTimestamp().format(DATE_FORMATTER)
                    ));
                }
                response.append("\n");
            }

            if (result.getTotalTransactions() > 0) {
                response.append(" **Transactions:** ").append(result.getTotalTransactions()).append("\n");
                for (var tx : result.getTransactions()) {
                    response.append(String.format("• `%s` | %s ETH | %s\n",
                        tx.getTransactionHash().substring(0, 10) + "...",
                        formatEther(tx.getValue()),
                        tx.getTimestamp().format(DATE_FORMATTER)
                    ));
                }
                response.append("\n");
            }

            if (result.getTotalTokens() > 0) {
                response.append(" **Tokens:** ").append(result.getTotalTokens()).append("\n");
                for (var token : result.getTokens()) {
                    response.append(String.format("• %s (%s) | %s\n",
                        token.getName(),
                        token.getSymbol(),
                        formatDollar(token.getMarketCap())
                    ));
                }
                response.append("\n");
            }

            return response.toString();
        } catch (Exception e) {
            log.error("Error in general search", e);
            return "ERROR Lỗi khi tìm kiếm: " + e.getMessage();
        }
    }

    public String getNetworkStats(String network) {
        try {
            SearchService.NetworkStats stats = searchService.getNetworkStats(network);
            
            return String.format("""
 **Network Statistics**
                
                **Network:** %s
                **Latest Block:** %s
                **Total Blocks:** %,d
                **Total Transactions:** %,d
                **Total Tokens:** %,d
                **Blocks (24h):** %,d
                **Transactions (24h):** %,d
                """,
                network,
                stats.getLatestBlockNumber(),
                stats.getTotalBlocks(),
                stats.getTotalTransactions(),
                stats.getTotalTokens(),
                stats.getBlocks24h(),
                stats.getTransactions24h()
            );
        } catch (Exception e) {
            log.error("Error getting network stats", e);
            return "ERROR Lỗi khi lấy thống kê network: " + e.getMessage();
        }
    }

    public String getTopTokens(String network, int limit) {
        try {
            List<Token> tokens = searchService.getTopTokens(network, limit);
            
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
                    formatDollar(token.getMarketCap()),
                    formatDollar(token.getPriceUsd()),
                    token.getPriceChange24h() != null && token.getPriceChange24h().compareTo(BigDecimal.ZERO) >= 0 ? "Tăng +" : "Giảm ",
                    token.getPriceChange24h() != null ? String.format("%.2f", token.getPriceChange24h()) : "N/A"
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
            log.info("Fetching {} new tokens from BSCScan", count);
            
            List<Token> newTokens = bscScanService.getNewTokens(count);
            
            if (newTokens.isEmpty()) {
                log.info("No new tokens from BSCScan API, fetching from database");
                newTokens = bscScanService.getLatestTokensFromDB(count);
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
                    token.getTokenAddress(),
                    token.getDecimals(),
                    formatTokenAmount(token.getTotalSupply(), token.getDecimals()),
                    formatDollar(token.getMarketCap()),
                    formatDollar(token.getPriceUsd()),
                    token.getNetwork(),
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

    private String formatDollar(BigInteger amount) {
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

    private String formatTokenAmount(BigInteger amount, Integer decimals) {
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
