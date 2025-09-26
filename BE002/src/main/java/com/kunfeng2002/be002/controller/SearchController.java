package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.dto.request.SearchRequest;
import com.kunfeng2002.be002.dto.response.*;
import com.kunfeng2002.be002.entity.Token;
import com.kunfeng2002.be002.entity.Transaction;
import com.kunfeng2002.be002.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SearchController {

    private final SearchService searchService;
    private final RealTimeCoinCrawlerService realTimeCoinCrawlerService;
    private final CoinCrawlerBridgeService coinCrawlerBridgeService;

    /**
     * Tìm kiếm tổng quát
     */
    @PostMapping("/general")
    public ResponseEntity<SearchResponse> generalSearch(@Valid @RequestBody SearchRequest request) {
        try {
            log.info("General search request: query={}, network={}, page={}, size={}", 
                    request.getQuery(), request.getNetwork(), request.getPage(), request.getSize());

            SearchService.SearchResult result = searchService.search(
                    request.getQuery(), 
                    request.getNetwork(), 
                    request.getPage(), 
                    request.getSize()
            );

            SearchResponse response = convertToSearchResponse(result);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in general search", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Tìm kiếm theo block
     */
    @GetMapping("/block")
    public ResponseEntity<BlockSearchResponse> searchBlock(
            @RequestParam String query,
            @RequestParam String network) {
        try {
            log.info("Block search request: query={}, network={}", query, network);

            SearchService.BlockSearchResult result = searchService.searchBlock(query, network);

            BlockSearchResponse response = BlockSearchResponse.builder()
                    .query(result.getQuery())
                    .network(result.getNetwork())
                    .block(BlockSearchResponse.BlockInfo.fromEntity(result.getBlock()))
                    .found(result.isFound())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in block search", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Tìm kiếm theo transaction hash
     */
    @GetMapping("/transaction")
    public ResponseEntity<TransactionSearchResponse> searchTransaction(
            @RequestParam String query,
            @RequestParam String network) {
        try {
            log.info("Transaction search request: query={}, network={}", query, network);

            SearchService.TransactionSearchResult result = searchService.searchTransaction(query, network);

            TransactionSearchResponse response = TransactionSearchResponse.builder()
                    .query(result.getQuery())
                    .network(result.getNetwork())
                    .transaction(TransactionSearchResponse.TransactionInfo.fromEntity(result.getTransaction()))
                    .found(result.isFound())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in transaction search", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Tìm kiếm theo address
     */
    @GetMapping("/address")
    public ResponseEntity<AddressSearchResponse> searchAddress(
            @RequestParam String query,
            @RequestParam String network,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("Address search request: query={}, network={}, page={}, size={}", 
                    query, network, page, size);

            SearchService.AddressSearchResult result = searchService.searchAddress(query, network, page, size);

            AddressSearchResponse response = AddressSearchResponse.builder()
                    .query(result.getQuery())
                    .network(result.getNetwork())
                    .transactions(result.getTransactions().stream()
                            .map(AddressSearchResponse.TransactionInfo::fromEntity)
                            .collect(Collectors.toList()))
                    .activities(result.getActivities().stream()
                            .map(AddressSearchResponse.AddressActivityInfo::fromEntity)
                            .collect(Collectors.toList()))
                    .totalTransactions(result.getTotalTransactions())
                    .totalActivities(result.getTotalActivities())
                    .totalSent(result.getTotalSent())
                    .totalReceived(result.getTotalReceived())
                    .found(result.isFound())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in address search", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Tìm kiếm theo token/coin
     */
    @GetMapping("/token")
    public ResponseEntity<TokenSearchResponse> searchToken(
            @RequestParam String query,
            @RequestParam String network,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("Token search request: query={}, network={}, page={}, size={}", 
                    query, network, page, size);

            SearchService.TokenSearchResult result = searchService.searchToken(query, network, page, size);

            TokenSearchResponse response = TokenSearchResponse.builder()
                    .query(result.getQuery())
                    .network(result.getNetwork())
                    .token(TokenSearchResponse.TokenInfo.fromEntity(result.getToken()))
                    .tokens(result.getTokens().stream()
                            .map(TokenSearchResponse.TokenInfo::fromEntity)
                            .collect(Collectors.toList()))
                    .totalTokens(result.getTotalTokens())
                    .found(result.isFound())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in token search", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Tìm kiếm token với real-time crawling
     */
    @GetMapping("/token/realtime")
    public ResponseEntity<TokenSearchResponse> searchTokenRealtime(
            @RequestParam String query,
            @RequestParam String network,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("Real-time token search request: query={}, network={}, page={}, size={}", 
                    query, network, page, size);

            // Đảm bảo real-time crawling đang chạy
            if (!realTimeCoinCrawlerService.isCrawling()) {
                log.info("Starting real-time crawling for fresh search results");
                realTimeCoinCrawlerService.startRealTimeCrawling();
            }

            SearchService.TokenSearchResult result = searchService.searchToken(query, network, page, size);

            TokenSearchResponse response = TokenSearchResponse.builder()
                    .query(result.getQuery())
                    .network(result.getNetwork())
                    .token(TokenSearchResponse.TokenInfo.fromEntity(result.getToken()))
                    .tokens(result.getTokens().stream()
                            .map(TokenSearchResponse.TokenInfo::fromEntity)
                            .collect(Collectors.toList()))
                    .totalTokens(result.getTotalTokens())
                    .found(result.isFound())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in real-time token search", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy token mới với real-time crawling
     */
    @GetMapping("/newtokens/realtime")
    public ResponseEntity<List<TokenSearchResponse.TokenInfo>> getNewTokensRealtime(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Getting {} new tokens with real-time crawling", limit);

            // Đảm bảo real-time crawling đang chạy
            if (!realTimeCoinCrawlerService.isCrawling()) {
                log.info("Starting real-time crawling for new tokens");
                realTimeCoinCrawlerService.startRealTimeCrawling();
            }

            List<Token> newTokens = searchService.getTopTokens("BSC", limit);
            
            List<TokenSearchResponse.TokenInfo> tokenInfos = newTokens.stream()
                    .map(TokenSearchResponse.TokenInfo::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(tokenInfos);

        } catch (Exception e) {
            log.error("Error getting new tokens with real-time crawling", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Tìm kiếm token với coincrawler integration
     */
    @GetMapping("/token/coincrawler")
    public ResponseEntity<List<TokenSearchResponse.TokenInfo>> searchTokenWithCoincrawler(
            @RequestParam String query,
            @RequestParam(defaultValue = "BSC") String network,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Coincrawler token search: query={}, network={}, limit={}", 
                    query, network, limit);

            List<Token> tokens = coinCrawlerBridgeService.searchTokensFromCoincrawler(query, network, limit);
            
            List<TokenSearchResponse.TokenInfo> tokenInfos = tokens.stream()
                    .map(TokenSearchResponse.TokenInfo::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(tokenInfos);

        } catch (Exception e) {
            log.error("Error in coincrawler token search", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy token mới với coincrawler integration
     */
    @GetMapping("/newtokens/coincrawler")
    public ResponseEntity<List<TokenSearchResponse.TokenInfo>> getNewTokensWithCoincrawler(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Getting new tokens with coincrawler: limit={}", limit);

            List<Token> newTokens = coinCrawlerBridgeService.getNewTokensFromCoincrawler(limit);
            
            List<TokenSearchResponse.TokenInfo> tokenInfos = newTokens.stream()
                    .map(TokenSearchResponse.TokenInfo::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(tokenInfos);

        } catch (Exception e) {
            log.error("Error getting new tokens with coincrawler", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy top tokens với coincrawler integration
     */
    @GetMapping("/toptokens/coincrawler")
    public ResponseEntity<List<TokenSearchResponse.TokenInfo>> getTopTokensWithCoincrawler(
            @RequestParam(defaultValue = "BSC") String network,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Getting top tokens with coincrawler: network={}, limit={}", network, limit);

            List<Token> topTokens = coinCrawlerBridgeService.getTopTokensFromCoincrawler(network, limit);
            
            List<TokenSearchResponse.TokenInfo> tokenInfos = topTokens.stream()
                    .map(TokenSearchResponse.TokenInfo::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(tokenInfos);

        } catch (Exception e) {
            log.error("Error getting top tokens with coincrawler", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy thống kê coincrawler
     */
    @GetMapping("/coincrawler/stats")
    public ResponseEntity<CoinCrawlerBridgeService.CoincrawlerStats> getCoincrawlerStats() {
        try {
            log.info("Getting coincrawler stats");

            CoinCrawlerBridgeService.CoincrawlerStats stats = coinCrawlerBridgeService.getCoincrawlerStats();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error getting coincrawler stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy thống kê network
     */
    @GetMapping("/stats/{network}")
    public ResponseEntity<NetworkStatsResponse> getNetworkStats(@PathVariable String network) {
        try {
            log.info("Network stats request: network={}", network);

            SearchService.NetworkStats stats = searchService.getNetworkStats(network);

            NetworkStatsResponse response = NetworkStatsResponse.builder()
                    .network(stats.getNetwork())
                    .totalBlocks(stats.getTotalBlocks())
                    .totalTransactions(stats.getTotalTransactions())
                    .totalTokens(stats.getTotalTokens())
                    .latestBlockNumber(stats.getLatestBlockNumber())
                    .blocks24h(stats.getBlocks24h())
                    .transactions24h(stats.getTransactions24h())
                    .status("ACTIVE")
                    .lastUpdate(java.time.LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting network stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy top tokens
     */
    @GetMapping("/tokens/top")
    public ResponseEntity<List<TokenSearchResponse.TokenInfo>> getTopTokens(
            @RequestParam String network,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Top tokens request: network={}, limit={}", network, limit);

            List<Token> tokens = searchService.getTopTokens(network, limit);

            List<TokenSearchResponse.TokenInfo> response = tokens.stream()
                    .map(TokenSearchResponse.TokenInfo::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting top tokens", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy recent transactions
     */
    @GetMapping("/transactions/recent")
    public ResponseEntity<List<TransactionSearchResponse.TransactionInfo>> getRecentTransactions(
            @RequestParam String network,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Recent transactions request: network={}, limit={}", network, limit);

            List<Transaction> transactions = searchService.getRecentTransactions(network, limit);

            List<TransactionSearchResponse.TransactionInfo> response = transactions.stream()
                    .map(TransactionSearchResponse.TransactionInfo::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting recent transactions", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Convert SearchResult to SearchResponse
     */
    private SearchResponse convertToSearchResponse(SearchService.SearchResult result) {
        return SearchResponse.builder()
                .query(result.getQuery())
                .network(result.getNetwork())
                .blocks(result.getBlocks().stream()
                        .map(this::convertToBlockDto)
                        .collect(Collectors.toList()))
                .transactions(result.getTransactions().stream()
                        .map(this::convertToTransactionDto)
                        .collect(Collectors.toList()))
                .tokens(result.getTokens().stream()
                        .map(this::convertToTokenDto)
                        .collect(Collectors.toList()))
                .addressActivities(result.getAddressActivities().stream()
                        .map(this::convertToAddressActivityDto)
                        .collect(Collectors.toList()))
                .totalBlocks(result.getTotalBlocks())
                .totalTransactions(result.getTotalTransactions())
                .totalTokens(result.getTotalTokens())
                .totalActivities(result.getTotalActivities())
                .found(result.getTotalBlocks() > 0 || result.getTotalTransactions() > 0 || 
                       result.getTotalTokens() > 0 || result.getTotalActivities() > 0)
                .build();
    }

    private SearchResponse.BlockDto convertToBlockDto(com.kunfeng2002.be002.entity.Block block) {
        return SearchResponse.BlockDto.builder()
                .id(block.getId())
                .blockNumber(block.getBlockNumber())
                .blockHash(block.getBlockHash())
                .parentHash(block.getParentHash())
                .network(block.getNetwork())
                .timestamp(block.getTimestamp())
                .gasLimit(block.getGasLimit())
                .gasUsed(block.getGasUsed())
                .difficulty(block.getDifficulty())
                .totalDifficulty(block.getTotalDifficulty())
                .size(block.getSize())
                .transactionCount(block.getTransactionCount())
                .baseFeePerGas(block.getBaseFeePerGas())
                .extraData(block.getExtraData())
                .build();
    }

    private SearchResponse.TransactionDto convertToTransactionDto(com.kunfeng2002.be002.entity.Transaction transaction) {
        return SearchResponse.TransactionDto.builder()
                .id(transaction.getId())
                .transactionHash(transaction.getTransactionHash())
                .blockNumber(transaction.getBlockNumber())
                .fromAddress(transaction.getFromAddress())
                .toAddress(transaction.getToAddress())
                .value(transaction.getValue())
                .gas(transaction.getGas())
                .gasPrice(transaction.getGasPrice())
                .gasUsed(transaction.getGasUsed())
                .nonce(transaction.getNonce())
                .network(transaction.getNetwork())
                .timestamp(transaction.getTimestamp())
                .status(transaction.getStatus())
                .inputData(transaction.getInputData())
                .transactionIndex(transaction.getTransactionIndex())
                .isContractCreation(transaction.getIsContractCreation())
                .contractAddress(transaction.getContractAddress())
                .logsCount(transaction.getLogsCount())
                .build();
    }

    private SearchResponse.TokenDto convertToTokenDto(com.kunfeng2002.be002.entity.Token token) {
        return SearchResponse.TokenDto.builder()
                .id(token.getId())
                .tokenAddress(token.getTokenAddress())
                .name(token.getName())
                .symbol(token.getSymbol())
                .decimals(token.getDecimals())
                .totalSupply(token.getTotalSupply())
                .network(token.getNetwork())
                .isVerified(token.getIsVerified())
                .logoUrl(token.getLogoUrl())
                .description(token.getDescription())
                .website(token.getWebsite())
                .twitter(token.getTwitter())
                .telegram(token.getTelegram())
                .discord(token.getDiscord())
                .marketCap(token.getMarketCap())
                .priceUsd(token.getPriceUsd())
                .volume24h(token.getVolume24h())
                .priceChange24h(token.getPriceChange24h())
                .lastPriceUpdate(token.getLastPriceUpdate())
                .isActive(token.getIsActive())
                .build();
    }

    private SearchResponse.AddressActivityDto convertToAddressActivityDto(com.kunfeng2002.be002.entity.AddressActivity activity) {
        return SearchResponse.AddressActivityDto.builder()
                .id(activity.getId())
                .address(activity.getAddress())
                .network(activity.getNetwork())
                .activityType(activity.getActivityType())
                .transactionHash(activity.getTransactionHash())
                .blockNumber(activity.getBlockNumber())
                .value(activity.getValue())
                .tokenAddress(activity.getTokenAddress())
                .tokenSymbol(activity.getTokenSymbol())
                .fromAddress(activity.getFromAddress())
                .toAddress(activity.getToAddress())
                .timestamp(activity.getTimestamp())
                .gasUsed(activity.getGasUsed())
                .gasPrice(activity.getGasPrice())
                .isContract(activity.getIsContract())
                .contractName(activity.getContractName())
                .methodName(activity.getMethodName())
                .build();
    }
}
