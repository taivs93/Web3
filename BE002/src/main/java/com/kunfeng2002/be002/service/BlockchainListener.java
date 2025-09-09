package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.request.WalletActivityEvent;
import io.reactivex.disposables.Disposable;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class BlockchainListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, Web3j> web3Clients;
    private final FollowService followService;
    private final Map<String, Integer> retryCounts = new ConcurrentHashMap<>();
    private final Map<String, Disposable> subscriptions = new ConcurrentHashMap<>();

    public BlockchainListener(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Qualifier("ETH") Web3j ethWeb3,
            @Qualifier("BSC") Web3j bscWeb3,
            @Qualifier("ARBITRUM") Web3j arbitrumWeb3,
            @Qualifier("OPTIMISM") Web3j optimismWeb3,
            @Qualifier("AVALANCHE") Web3j avalancheWeb3,
            FollowService followService
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.followService = followService;

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
        web3Clients.forEach(this::startListener);
        log.info("BlockchainListener started for networks: {}", web3Clients.keySet());
    }

    private void startListener(String network, Web3j web3) {
        Disposable oldSub = subscriptions.get(network);
        if (oldSub != null && !oldSub.isDisposed()) {
            oldSub.dispose();
        }

        Disposable subscription = web3.blockFlowable(false)
                .subscribe(
                        block -> processBlock(network, block),
                        error -> {
                            log.error("Error while listening to {} blockchain", network, error);
                            restartListener(network, web3);
                        }
                );
        subscriptions.put(network, subscription);
        log.info("Started blockchain listener for network: {}", network);
    }

    private void restartListener(String network, Web3j web3) {
        int maxRetries = 5;
        int currentRetry = retryCounts.computeIfAbsent(network, k -> 0);

        if (currentRetry >= maxRetries) {
            log.error("Failed to restart listener for {} after {} retries. Giving up.", network, maxRetries);
            return;
        }

        long delay = (long) (Math.pow(2, currentRetry) * 1000);
        retryCounts.put(network, currentRetry + 1);

        try {
            log.warn("Attempting to restart listener for {} in {}ms. Retry count: {}", network, delay, currentRetry + 1);
            Thread.sleep(delay);
            startListener(network, web3);
            log.info("Restarted blockchain listener for network: {}", network);
            retryCounts.put(network, 0);
        } catch (InterruptedException e) {
            log.error("Failed to restart listener for {}", network, e);
            Thread.currentThread().interrupt();
        }
    }

    private void processBlock(String network, EthBlock ethBlock) {
        if (ethBlock.getBlock() == null || ethBlock.getBlock().getTransactions() == null) {
            return;
        }
        ethBlock.getBlock().getTransactions().forEach(transactionResult -> {
            try {
                Object rawTransaction = transactionResult.get();
                Transaction transaction = null;
                if (rawTransaction instanceof Transaction) {
                    transaction = (Transaction) rawTransaction;
                } else if (rawTransaction instanceof String) {
                    String transactionHash = (String) rawTransaction;
                    try {
                        transaction = web3Clients.get(network)
                                .ethGetTransactionByHash(transactionHash)
                                .send()
                                .getTransaction()
                                .orElse(null);
                    } catch (Exception apiException) {
                        log.error("Failed to get transaction details for hash {} on network {}", transactionHash, network, apiException);
                        return;
                    }

                    if (transaction == null) {
                        log.warn("Transaction with hash {} not found on network {}", transactionHash, network);
                        return;
                    }
                } else {
                    log.warn("Unexpected transaction result type: {}", rawTransaction.getClass().getName());
                    return;
                }
                checkAndPublish(network, transaction);
            } catch (Exception e) {
                log.error("Error processing transaction in block {}", ethBlock.getBlock().getNumber(), e);
            }
        });
    }

    private void checkAndPublish(String network, Transaction transaction) {

        String from = transaction.getFrom() != null ? transaction.getFrom().toLowerCase() : null;
        String to = transaction.getTo() != null ? transaction.getTo().toLowerCase() : null;
        List<String> followedAddresses = followService.getGloballyFollowedAddresses();
        if (followedAddresses == null || followedAddresses.isEmpty()) {
            return;
        }

        boolean fromFollowed = from != null && followedAddresses.contains(from);
        boolean toFollowed = to != null && followedAddresses.contains(to);

        if (fromFollowed || toFollowed) {
            WalletActivityEvent event = new WalletActivityEvent(
                    transaction.getHash(),
                    transaction.getFrom(),
                    transaction.getTo(),
                    network,
                    transaction.getValue() != null ? transaction.getValue().toString() : "0",
                    System.currentTimeMillis()
            );
            log.info("Receive block");

            try {
                kafkaTemplate.send(
                        "wallet-activity",
                        transaction.getHash(),
                        objectMapper.writeValueAsString(event)
                );
                log.debug("Published wallet activity for transaction {} on network {}", transaction.getHash(), network);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize WalletActivityEvent for transaction {}", transaction.getHash(), e);
            }
        }
    }
}