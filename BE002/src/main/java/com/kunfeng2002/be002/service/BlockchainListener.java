package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.dto.request.WalletActivityEvent;
import com.kunfeng2002.be002.entity.NetworkType;
import io.reactivex.disposables.Disposable;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class BlockchainListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, Web3j> web3Clients;
    private final FollowService followService;
    private final Map<String, Integer> retryCounts = new ConcurrentHashMap<>();
    private final Map<String, Disposable> subscriptions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final Map<String, AtomicReference<BigInteger>> lastProcessedBlocks = new ConcurrentHashMap<>();

    @Value("${app.blockchain.max-retry-attempts:3}")
    private int maxRetryAttempts;

    @Value("${app.blockchain.reconnect-delay:5000}")
    private long reconnectDelay;

    public BlockchainListener(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
//            @Qualifier("ETH") Web3j ethWeb3,
            @Qualifier("BSC") Web3j bscWeb3,
//            @Qualifier("ARBITRUM") Web3j arbitrumWeb3,
//            @Qualifier("OPTIMISM") Web3j optimismWeb3,
//            @Qualifier("AVALANCHE") Web3j avalancheWeb3,
            FollowService followService
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.followService = followService;

        this.web3Clients = Map.of(
//                "ETH", ethWeb3,
                "BSC", bscWeb3
//                "ARBITRUM", arbitrumWeb3,
//                "OPTIMISM", optimismWeb3,
//                "AVALANCHE", avalancheWeb3
        );

        web3Clients.keySet().forEach(network -> lastProcessedBlocks.put(network, new AtomicReference<>(BigInteger.ZERO)));
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

        Disposable subscription = web3.blockFlowable(true)
                .subscribe(
                        block -> {
                            processBlock(network, block);
                            if (block.getBlock() != null) {
                                lastProcessedBlocks.get(network)
                                        .set(block.getBlock().getNumber());
                            }
                        },
                        error -> {
                            log.error("Error while listening to {} blockchain", network, error);
                            restartListener(network, web3);
                        }
                );
        subscriptions.put(network, subscription);
        log.info("Started blockchain listener for network: {}", network);
    }

    private void restartListener(String network, Web3j web3) {
        int currentRetry = retryCounts.computeIfAbsent(network, k -> 0);

        if (currentRetry >= maxRetryAttempts) {
            log.error("Failed to restart listener for {} after {} retries. Giving up.", network, maxRetryAttempts);
            return;
        }

        long delay = reconnectDelay * (long) (Math.pow(2, currentRetry));
        retryCounts.put(network, currentRetry + 1);

        log.warn("Attempting to restart listener for {} in {}ms. Retry count: {}", network, delay, currentRetry + 1);

        scheduler.schedule(() -> {
            try {
                catchUpBlocks(network, web3);
                startListener(network, web3);
                log.info("Restarted blockchain listener for network: {}", network);
                retryCounts.put(network, 0);
            } catch (Exception e) {
                log.error("Failed to restart listener for {}", network, e);
                restartListener(network, web3);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void catchUpBlocks(String network, Web3j web3) {
        try {
            BigInteger lastBlock = lastProcessedBlocks.get(network).get();
            EthBlockNumber currentBlockResp = web3.ethBlockNumber().send();
            BigInteger currentBlock = currentBlockResp.getBlockNumber();

            if (currentBlock.compareTo(lastBlock) > 0) {
                log.info("Catching up {} from block {} to {}", network, lastBlock.add(BigInteger.ONE), currentBlock);
                for (BigInteger i = lastBlock.add(BigInteger.ONE); i.compareTo(currentBlock) <= 0; i = i.add(BigInteger.ONE)) {
                    EthBlock block = web3.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), true).send();
                    processBlock(network, block);
                    lastProcessedBlocks.get(network).set(i);
                }
            } else {
                log.info("No missed blocks for network {}", network);
            }
        } catch (Exception e) {
            log.error("Failed to catch up blocks for network {}", network, e);
        }
    }

    private void processBlock(String network, EthBlock ethBlock) {
        if (ethBlock.getBlock() == null || ethBlock.getBlock().getTransactions() == null) {
            return;
        }
        ethBlock.getBlock().getTransactions().forEach(transactionResult -> {
            try {
                Object rawTransaction = transactionResult.get();
                if (rawTransaction instanceof TransactionObject) {
                    TransactionObject transaction = (TransactionObject) rawTransaction;
                    checkAndPublish(network, transaction);
                } else log.warn("Wrong transaction type");
            } catch (Exception e) {
                log.error("Error processing transaction in block {}", ethBlock.getBlock().getNumber(), e);
            }
        });
    }

    private void checkAndPublish(String network, TransactionObject transaction) {
        String from = transaction.getFrom() != null ? transaction.getFrom().toLowerCase() : null;
        String to = transaction.getTo() != null ? transaction.getTo().toLowerCase() : null;

        List<String> listAddresses = followService.getGloballyFollowedAddresses();
        Set<String> followedAddresses = new HashSet<>(listAddresses);
        if (followedAddresses.isEmpty()) {
            return;
        }

        boolean fromFollowed = from != null && followedAddresses.contains(from);
        boolean toFollowed = to != null && followedAddresses.contains(to);

        if (fromFollowed || toFollowed) {
            WalletActivityEvent event = WalletActivityEvent.builder()
                    .network(NetworkType.valueOf(network.toUpperCase()))
                    .transactionHash(transaction.getHash())
                    .fromAddress(transaction.getFrom())
                    .toAddress(transaction.getTo())
                    .value(transaction.getValue() != null ? transaction.getValue() : BigInteger.ZERO)
                    .blockNumber(transaction.getBlockNumberRaw())
                    .gasPrice(transaction.getGasPrice())
                    .timestamp(System.currentTimeMillis())
                    .build();

            try {
                kafkaTemplate.send(
                        "wallet-activity",
                        transaction.getHash(),
                        objectMapper.writeValueAsString(event)
                );
                log.info("Published wallet activity for transaction {} on network {}", transaction.getHash(), network);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize WalletActivityEvent for transaction {}", transaction.getHash(), e);
            }
        }
    }


}
