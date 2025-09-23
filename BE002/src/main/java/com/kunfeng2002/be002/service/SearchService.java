package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.entity.*;
import com.kunfeng2002.be002.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    
    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;
    private final TokenRepository tokenRepository;
    private final AddressActivityRepository addressActivityRepository;

    public SearchService(BlockRepository blockRepository, 
                        TransactionRepository transactionRepository, 
                        TokenRepository tokenRepository, 
                        AddressActivityRepository addressActivityRepository) {
        this.blockRepository = blockRepository;
        this.transactionRepository = transactionRepository;
        this.tokenRepository = tokenRepository;
        this.addressActivityRepository = addressActivityRepository;
    }

    public SearchResult search(String query, String network, int page, int size) {
        if (!StringUtils.hasText(query)) {
            return SearchResult.empty();
        }

        query = query.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        SearchResult result = new SearchResult();
        result.setQuery(query);
        result.setNetwork(network);

        try {
            Page<Block> blocks = blockRepository.searchBlocks(network, query, pageable);
            result.setBlocks(blocks.getContent());
            result.setTotalBlocks(blocks.getTotalElements());
        } catch (Exception e) {
            log.error("Error searching blocks", e);
        }

        try {
            Page<Transaction> transactions = transactionRepository.searchTransactions(network, query, pageable);
            result.setTransactions(transactions.getContent());
            result.setTotalTransactions(transactions.getTotalElements());
        } catch (Exception e) {
            log.error("Error searching transactions", e);
        }

        try {
            Page<Token> tokens = tokenRepository.searchTokens(network, query, pageable);
            result.setTokens(tokens.getContent());
            result.setTotalTokens(tokens.getTotalElements());
        } catch (Exception e) {
            log.error("Error searching tokens", e);
        }

        try {
            Page<AddressActivity> activities = addressActivityRepository.searchActivities(network, query, pageable);
            result.setAddressActivities(activities.getContent());
            result.setTotalActivities(activities.getTotalElements());
        } catch (Exception e) {
            log.error("Error searching address activities", e);
        }

        return result;
    }

    public BlockSearchResult searchBlock(String query, String network) {
        if (!StringUtils.hasText(query)) {
            return BlockSearchResult.empty();
        }

        query = query.trim();
        BlockSearchResult result = new BlockSearchResult();
        result.setQuery(query);
        result.setNetwork(network);

        try {

            Optional<Block> blockByHash = blockRepository.findByBlockHash(query);
            if (blockByHash.isPresent()) {
                result.setBlock(blockByHash.get());
                result.setFound(true);
                return result;
            }

            try {
                BigInteger blockNumber = new BigInteger(query);
                Optional<Block> blockByNumber = blockRepository.findByBlockNumberAndNetwork(blockNumber, network);
                if (blockByNumber.isPresent()) {
                    result.setBlock(blockByNumber.get());
                    result.setFound(true);
                }
            } catch (NumberFormatException e) {
                log.debug("Query is not a valid block number: {}", query);
            }

        } catch (Exception e) {
            log.error("Error searching block", e);
        }

        return result;
    }

    public TransactionSearchResult searchTransaction(String query, String network) {
        if (!StringUtils.hasText(query)) {
            return TransactionSearchResult.empty();
        }

        query = query.trim();
        TransactionSearchResult result = new TransactionSearchResult();
        result.setQuery(query);
        result.setNetwork(network);

        try {
            Optional<Transaction> transaction = transactionRepository.findByTransactionHash(query);
            if (transaction.isPresent()) {
                result.setTransaction(transaction.get());
                result.setFound(true);
            }
        } catch (Exception e) {
            log.error("Error searching transaction", e);
        }

        return result;
    }

    public AddressSearchResult searchAddress(String query, String network, int page, int size) {
        if (!StringUtils.hasText(query)) {
            return AddressSearchResult.empty();
        }

        query = query.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        AddressSearchResult result = new AddressSearchResult();
        result.setQuery(query);
        result.setNetwork(network);

        try {

            List<Transaction> fromTransactions = transactionRepository.findByFromAddressAndNetwork(query, network, pageable);
            List<Transaction> toTransactions = transactionRepository.findByToAddressAndNetwork(query, network, pageable);

            Set<Transaction> allTransactions = new HashSet<>();
            allTransactions.addAll(fromTransactions);
            allTransactions.addAll(toTransactions);

            result.setTransactions(new ArrayList<>(allTransactions));
            result.setTotalTransactions(transactionRepository.countByFromAddress(network, query) + 
                                     transactionRepository.countByToAddress(network, query));

            List<AddressActivity> activities = addressActivityRepository.findByAddressAndNetworkOrderByTimestampDesc(query, network, pageable);
            result.setActivities(activities);
            result.setTotalActivities(addressActivityRepository.countByAddressAndNetwork(query, network));

            BigInteger totalSent = transactionRepository.sumValueByFromAddress(network, query);
            BigInteger totalReceived = transactionRepository.sumValueByToAddress(network, query);
            result.setTotalSent(totalSent != null ? totalSent : BigInteger.ZERO);
            result.setTotalReceived(totalReceived != null ? totalReceived : BigInteger.ZERO);

            result.setFound(!allTransactions.isEmpty() || !activities.isEmpty());

        } catch (Exception e) {
            log.error("Error searching address", e);
        }

        return result;
    }

    public TokenSearchResult searchToken(String query, String network, int page, int size) {
        if (!StringUtils.hasText(query)) {
            return TokenSearchResult.empty();
        }

        query = query.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by("marketCap").descending());

        TokenSearchResult result = new TokenSearchResult();
        result.setQuery(query);
        result.setNetwork(network);

        try {

            if (query.startsWith("0x") && query.length() == 42) {
                Optional<Token> tokenByAddress = tokenRepository.findByTokenAddressAndNetwork(query, network);
                if (tokenByAddress.isPresent()) {
                    result.setToken(tokenByAddress.get());
                    result.setFound(true);
                    return result;
                }
            }

            List<Token> tokensBySymbol = tokenRepository.findBySymbolIgnoreCaseAndNetwork(query, network);
            if (!tokensBySymbol.isEmpty()) {
                result.setTokens(tokensBySymbol);
                result.setFound(true);
                return result;
            }

            Page<Token> tokens = tokenRepository.searchTokens(network, query, pageable);
            result.setTokens(tokens.getContent());
            result.setTotalTokens(tokens.getTotalElements());
            result.setFound(!tokens.getContent().isEmpty());

        } catch (Exception e) {
            log.error("Error searching token", e);
        }

        return result;
    }

    public NetworkStats getNetworkStats(String network) {
        NetworkStats stats = new NetworkStats();
        stats.setNetwork(network);

        try {
            stats.setTotalBlocks(blockRepository.countByNetwork(network));
            stats.setTotalTransactions(transactionRepository.countByNetwork(network));
            stats.setTotalTokens(tokenRepository.countActiveTokensByNetwork(network));

            Optional<BigInteger> latestBlock = blockRepository.findLatestBlockNumber(network);
            stats.setLatestBlockNumber(latestBlock.orElse(BigInteger.ZERO));

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneDayAgo = now.minusDays(1);

            List<Block> blocks24h = blockRepository.findByNetworkAndTimestampRange(network, oneDayAgo, now);
            List<Transaction> transactions24h = transactionRepository.findByNetworkAndTimestampRange(network, oneDayAgo, now);

            stats.setBlocks24h(blocks24h.size());
            stats.setTransactions24h(transactions24h.size());

        } catch (Exception e) {
            log.error("Error getting network stats", e);
        }

        return stats;
    }

    public List<Token> getTopTokens(String network, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit, Sort.by("marketCap").descending());
            return tokenRepository.findByNetworkAndIsActiveTrueOrderByMarketCapDesc(network, pageable);
        } catch (Exception e) {
            log.error("Error getting top tokens", e);
            return Collections.emptyList();
        }
    }

    public List<Transaction> getRecentTransactions(String network, int limit) {
        try {
            return transactionRepository.findByNetworkAndTimestampRange(network, 
                    LocalDateTime.now().minusDays(1), LocalDateTime.now()).stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting recent transactions", e);
            return Collections.emptyList();
        }
    }

    public static class SearchResult {
        private String query;
        private String network;
        private List<Block> blocks = new ArrayList<>();
        private List<Transaction> transactions = new ArrayList<>();
        private List<Token> tokens = new ArrayList<>();
        private List<AddressActivity> addressActivities = new ArrayList<>();
        private long totalBlocks = 0;
        private long totalTransactions = 0;
        private long totalTokens = 0;
        private long totalActivities = 0;

        public static SearchResult empty() {
            return new SearchResult();
        }

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public String getNetwork() { return network; }
        public void setNetwork(String network) { this.network = network; }
        public List<Block> getBlocks() { return blocks; }
        public void setBlocks(List<Block> blocks) { this.blocks = blocks; }
        public List<Transaction> getTransactions() { return transactions; }
        public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
        public List<Token> getTokens() { return tokens; }
        public void setTokens(List<Token> tokens) { this.tokens = tokens; }
        public List<AddressActivity> getAddressActivities() { return addressActivities; }
        public void setAddressActivities(List<AddressActivity> addressActivities) { this.addressActivities = addressActivities; }
        public long getTotalBlocks() { return totalBlocks; }
        public void setTotalBlocks(long totalBlocks) { this.totalBlocks = totalBlocks; }
        public long getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }
        public long getTotalTokens() { return totalTokens; }
        public void setTotalTokens(long totalTokens) { this.totalTokens = totalTokens; }
        public long getTotalActivities() { return totalActivities; }
        public void setTotalActivities(long totalActivities) { this.totalActivities = totalActivities; }

        public boolean isFound() {
            return false;
        }
    }

    public static class BlockSearchResult {
        private String query;
        private String network;
        private Block block;
        private boolean found = false;

        public static BlockSearchResult empty() {
            return new BlockSearchResult();
        }

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public String getNetwork() { return network; }
        public void setNetwork(String network) { this.network = network; }
        public Block getBlock() { return block; }
        public void setBlock(Block block) { this.block = block; }
        public boolean isFound() { return found; }
        public void setFound(boolean found) { this.found = found; }
    }

    public static class TransactionSearchResult {
        private String query;
        private String network;
        private Transaction transaction;
        private boolean found = false;

        public static TransactionSearchResult empty() {
            return new TransactionSearchResult();
        }

        // Getters and setters
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public String getNetwork() { return network; }
        public void setNetwork(String network) { this.network = network; }
        public Transaction getTransaction() { return transaction; }
        public void setTransaction(Transaction transaction) { this.transaction = transaction; }
        public boolean isFound() { return found; }
        public void setFound(boolean found) { this.found = found; }
    }

    public static class AddressSearchResult {
        private String query;
        private String network;
        private List<Transaction> transactions = new ArrayList<>();
        private List<AddressActivity> activities = new ArrayList<>();
        private long totalTransactions = 0;
        private long totalActivities = 0;
        private BigInteger totalSent = BigInteger.ZERO;
        private BigInteger totalReceived = BigInteger.ZERO;
        private boolean found = false;

        public static AddressSearchResult empty() {
            return new AddressSearchResult();
        }

        // Getters and setters
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public String getNetwork() { return network; }
        public void setNetwork(String network) { this.network = network; }
        public List<Transaction> getTransactions() { return transactions; }
        public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
        public List<AddressActivity> getActivities() { return activities; }
        public void setActivities(List<AddressActivity> activities) { this.activities = activities; }
        public long getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }
        public long getTotalActivities() { return totalActivities; }
        public void setTotalActivities(long totalActivities) { this.totalActivities = totalActivities; }
        public BigInteger getTotalSent() { return totalSent; }
        public void setTotalSent(BigInteger totalSent) { this.totalSent = totalSent; }
        public BigInteger getTotalReceived() { return totalReceived; }
        public void setTotalReceived(BigInteger totalReceived) { this.totalReceived = totalReceived; }
        public boolean isFound() { return found; }
        public void setFound(boolean found) { this.found = found; }
    }

    public static class TokenSearchResult {
        private String query;
        private String network;
        private Token token;
        private List<Token> tokens = new ArrayList<>();
        private long totalTokens = 0;
        private boolean found = false;

        public static TokenSearchResult empty() {
            return new TokenSearchResult();
        }

        // Getters and setters
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public String getNetwork() { return network; }
        public void setNetwork(String network) { this.network = network; }
        public Token getToken() { return token; }
        public void setToken(Token token) { this.token = token; }
        public List<Token> getTokens() { return tokens; }
        public void setTokens(List<Token> tokens) { this.tokens = tokens; }
        public long getTotalTokens() { return totalTokens; }
        public void setTotalTokens(long totalTokens) { this.totalTokens = totalTokens; }
        public boolean isFound() { return found; }
        public void setFound(boolean found) { this.found = found; }
    }

    public static class NetworkStats {
        private String network;
        private long totalBlocks = 0;
        private long totalTransactions = 0;
        private long totalTokens = 0;
        private BigInteger latestBlockNumber = BigInteger.ZERO;
        private int blocks24h = 0;
        private int transactions24h = 0;

        // Getters and setters
        public String getNetwork() { return network; }
        public void setNetwork(String network) { this.network = network; }
        public long getTotalBlocks() { return totalBlocks; }
        public void setTotalBlocks(long totalBlocks) { this.totalBlocks = totalBlocks; }
        public long getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }
        public long getTotalTokens() { return totalTokens; }
        public void setTotalTokens(long totalTokens) { this.totalTokens = totalTokens; }
        public BigInteger getLatestBlockNumber() { return latestBlockNumber; }
        public void setLatestBlockNumber(BigInteger latestBlockNumber) { this.latestBlockNumber = latestBlockNumber; }
        public int getBlocks24h() { return blocks24h; }
        public void setBlocks24h(int blocks24h) { this.blocks24h = blocks24h; }
        public int getTransactions24h() { return transactions24h; }
        public void setTransactions24h(int transactions24h) { this.transactions24h = transactions24h; }
    }
}
