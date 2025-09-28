package com.kunfeng2002.be002.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.kunfeng2002.be002.dto.request.WalletActivityEvent;
import com.kunfeng2002.be002.entity.NetworkType;
import io.reactivex.disposables.Disposable;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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
    private final Set<String> processedTransactions = ConcurrentHashMap.newKeySet();


    @Value("${app.blockchain.max-retry-attempts:3}")
    private int maxRetryAttempts;

    @Value("${app.blockchain.reconnect-delay:5000}")
    private long reconnectDelay;

    public BlockchainListener(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            Map<String, Web3j> web3Clients,
            FollowService followService, TelegramBotService telegramBotService
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.followService = followService;
        this.web3Clients = web3Clients;

        web3Clients.keySet()
                .forEach(network -> lastProcessedBlocks.put(network, new AtomicReference<>(BigInteger.ZERO)));
    }

    @PostConstruct
    public void start() {
        web3Clients.forEach(this::startListener);
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
                            restartListener(network, web3);
                        }
                );
        subscriptions.put(network, subscription);
    }

    private void restartListener(String network, Web3j web3) {
        int currentRetry = retryCounts.computeIfAbsent(network, k -> 0);

        if (currentRetry >= maxRetryAttempts) {
            return;
        }

        long delay = reconnectDelay * (long) (Math.pow(2, currentRetry));
        retryCounts.put(network, currentRetry + 1);


        scheduler.schedule(() -> {
            try {
                catchUpBlocks(network, web3);
                startListener(network, web3);
                retryCounts.put(network, 0);
            } catch (Exception e) {
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
                for (BigInteger i = lastBlock.add(BigInteger.ONE); i.compareTo(currentBlock) <= 0; i = i.add(BigInteger.ONE)) {
                    EthBlock block = web3.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), true).send();
                    processBlock(network, block);
                    lastProcessedBlocks.get(network).set(i);
                }
            } else {
            }
        } catch (Exception e) {
        }
    }

    private void processBlock(String network, EthBlock ethBlock) {
        if (ethBlock.getBlock() == null || ethBlock.getBlock().getTransactions() == null) {
            return;
        }
        
        
        ethBlock.getBlock().getTransactions().forEach(transactionResult -> {
            Object rawTransaction = transactionResult.get();
            if (rawTransaction instanceof TransactionObject) {
                TransactionObject transaction = (TransactionObject) rawTransaction;
                checkAndPublish(network, transaction);
            } else {
            }
        });
    }

    private void checkAndPublish(String network, TransactionObject transaction) {
        String transactionHash = transaction.getHash();
        
        
        if (processedTransactions.contains(transactionHash)) {
            return;
        }
        
        String from = transaction.getFrom() != null ? transaction.getFrom().toLowerCase() : null;
        String to = transaction.getTo() != null ? transaction.getTo().toLowerCase() : null;

        List<String> listAddresses;
        try {
            listAddresses = followService.getGloballyFollowedAddresses();
        } catch (Exception e) {
            return;
        }
        
        Set<String> followedAddresses = new HashSet<>(listAddresses);
        if (followedAddresses.isEmpty()) {
            return;
        }

        boolean fromFollowed = from != null && followedAddresses.contains(from);
        boolean toFollowed = to != null && followedAddresses.contains(to);

        if (fromFollowed || toFollowed) {
            
            processedTransactions.add(transactionHash);
            
            NetworkType networkType;
            switch (network.toUpperCase()) {
                case "ETH":
                    networkType = NetworkType.ETHEREUM;
                    break;
                case "BSC":
                    networkType = NetworkType.BSC;
                    break;
                case "AVALANCHE":
                    networkType = NetworkType.AVALANCHE;
                    break;
                case "OPTIMISM":
                    networkType = NetworkType.OPTIMISM;
                    break;
                case "ARBITRUM":
                    networkType = NetworkType.ARBITRUM;
                    break;
                default:
                    return;
            }

            WalletActivityEvent event = WalletActivityEvent.builder()
                    .network(networkType)
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
            } catch (JsonProcessingException e) {
            }
        }
    }


}
