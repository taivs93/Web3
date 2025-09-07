package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.request.WalletActivityEvent;
import com.kunfeng2002.be002.repository.FollowedAddressRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.websocket.WebSocketService;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class BlockchainListener {

    private final FollowedAddressRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, Web3j> web3Clients;

    private final Map<String, Set<String>> followedAddressesCache = new ConcurrentHashMap<>();

    public BlockchainListener(
            FollowedAddressRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Qualifier("ETH") Web3j ethWeb3,
            @Qualifier("BSC") Web3j bscWeb3,
            @Qualifier("ARBITRUM") Web3j arbitrumWeb3,
            @Qualifier("OPTIMISM") Web3j optimismWeb3,
            @Qualifier("AVALANCHE") Web3j avalancheWeb3
    ) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;

        this.web3Clients = Map.of(
                "ETH", ethWeb3,
                "BSC", bscWeb3,
                "ARBITRUM", arbitrumWeb3,
                "OPTIMISM", optimismWeb3,
                "AVALANCHE", avalancheWeb3
        );
    }

    @PostConstruct
    public void start() {
        refreshCache();
        web3Clients.forEach(this::startListener);
        log.info("BlockchainListener started for networks: {}", web3Clients.keySet());
    }

    private void startListener(String network, Web3j web3) {
        web3.blockFlowable(false)
                .subscribe(
                        block -> processBlock(network, block),
                        error -> {
                            log.error("Error while listening to {} blockchain", network, error);

                            restartListener(network, web3);
                        }
                );
        log.info("Started blockchain listener for network: {}", network);
    }

    private void restartListener(String network, Web3j web3) {
        try {
            Thread.sleep(5000);
            startListener(network, web3);
            log.info("Restarted blockchain listener for network: {}", network);
        } catch (InterruptedException e) {
            log.error("Failed to restart listener for {}", network, e);
            Thread.currentThread().interrupt();
        }
    }

    private void processBlock(String network, EthBlock ethBlock) {
        if (ethBlock.getBlock() == null || ethBlock.getBlock().getTransactions() == null) {
            return;
        }

        ethBlock.getBlock().getTransactions().forEach(txResult -> {
            try {
                Object rawTx = txResult.get();
                Transaction tx = null;

                if (rawTx instanceof Transaction) {
                    tx = (Transaction) rawTx;
                } else if (rawTx instanceof String) {
                    String txHash = (String) rawTx;
                    try {
                        tx = web3Clients.get(network)
                                .ethGetTransactionByHash(txHash)
                                .send()
                                .getTransaction()
                                .orElse(null);
                    } catch (Exception apiException) {
                        log.error("Failed to get transaction details for hash {} on network {}", txHash, network, apiException);
                        return;
                    }

                    if (tx == null) {
                        log.warn("Transaction with hash {} not found on network {}", txHash, network);
                        return;
                    }
                } else {
                    log.warn("Unexpected transaction result type: {}", rawTx.getClass().getName());
                    return;
                }

                checkAndPublish(network, tx);
            } catch (Exception e) {
                log.error("Error processing transaction in block {}", ethBlock.getBlock().getNumber(), e);
            }
        });
    }

    private void checkAndPublish(String network, Transaction tx) {
        String from = tx.getFrom() != null ? tx.getFrom().toLowerCase() : null;
        String to = tx.getTo() != null ? tx.getTo().toLowerCase() : null;

        Set<String> followedAddresses = followedAddressesCache.get(network);
        if (followedAddresses == null || followedAddresses.isEmpty()) {
            return;
        }

        boolean fromFollowed = from != null && followedAddresses.contains(from);
        boolean toFollowed = to != null && followedAddresses.contains(to);

        if (fromFollowed || toFollowed) {
            WalletActivityEvent event = new WalletActivityEvent(
                    tx.getHash(),
                    tx.getFrom(),
                    tx.getTo(),
                    network,
                    tx.getValue() != null ? tx.getValue().toString() : "0",
                    System.currentTimeMillis()
            );

            try {
                kafkaTemplate.send(
                        "wallet-activity",
                        tx.getHash(),
                        objectMapper.writeValueAsString(event)
                );
                log.debug("Published wallet activity for tx {} on network {}", tx.getHash(), network);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize WalletActivityEvent for tx {}", tx.getHash(), e);
            }
        }
    }

    @Scheduled(fixedDelay = 300000)
    public void refreshCache() {
        try {
            followedAddressesCache.clear();

            repository.findAll().forEach(followedAddress -> {
                String network = followedAddress.getNetwork();
                String address = followedAddress.getAddress().toLowerCase();

                followedAddressesCache
                        .computeIfAbsent(network, k -> ConcurrentHashMap.newKeySet())
                        .add(address);
            });

            log.info("Cache refreshed. Following {} addresses across {} networks",
                    followedAddressesCache.values().stream().mapToInt(Set::size).sum(),
                    followedAddressesCache.size());

        } catch (Exception e) {
            log.error("Failed to refresh cache", e);
        }
    }

    public void addToCache(String network, String address) {
        followedAddressesCache
                .computeIfAbsent(network, k -> ConcurrentHashMap.newKeySet())
                .add(address.toLowerCase());
        log.debug("Added {} to cache for network {}", address, network);
    }

    public void removeFromCache(String network, String address) {
        Set<String> addresses = followedAddressesCache.get(network);
        if (addresses != null) {
            addresses.remove(address.toLowerCase());
            log.debug("Removed {} from cache for network {}", address, network);
        }
    }

    public Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new ConcurrentHashMap<>();
        followedAddressesCache.forEach((network, addresses) ->
                stats.put(network, addresses.size()));
        return stats;
    }
}
