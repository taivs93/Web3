package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.entity.*;
import com.kunfeng2002.be002.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainCrawlerService {

    private final Map<String, Web3j> web3Clients;
    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;
    private final TokenRepository tokenRepository;
    private final AddressActivityRepository addressActivityRepository;
    private final Web3Service web3Service;

    @Value("${app.crawler.batch-size:100}")
    private int batchSize;

    @Value("${app.crawler.max-blocks-per-run:1000}")
    private int maxBlocksPerRun;

    @Value("${app.crawler.enabled-networks:BSC}")
    private List<String> enabledNetworks;

    @Scheduled(fixedDelay = 30000)
    public void crawlBlockchainData() {
        for (String network : enabledNetworks) {
            try {
                crawlNetworkData(network);
            } catch (Exception e) {
                log.error("Error crawling data for network: {}", network, e);
            }
        }
    }

    @Async
    public CompletableFuture<Void> crawlNetworkData(String network) {
        try {
            Web3j web3 = web3Clients.get(network);
            if (web3 == null) {
                log.warn("No Web3 client found for network: {}", network);
                return CompletableFuture.completedFuture(null);
            }

            EthBlockNumber currentBlockResp = web3.ethBlockNumber().send();
            BigInteger currentBlock = currentBlockResp.getBlockNumber();
            BigInteger lastCrawledBlock = getLastCrawledBlock(network);
            BigInteger blocksToCrawl = currentBlock.subtract(lastCrawledBlock);
            if (blocksToCrawl.compareTo(BigInteger.ZERO) <= 0) {
                log.debug("No new blocks to crawl for network: {}", network);
                return CompletableFuture.completedFuture(null);
            }
            BigInteger maxBlocks = BigInteger.valueOf(maxBlocksPerRun);
            if (blocksToCrawl.compareTo(maxBlocks) > 0) {
                blocksToCrawl = maxBlocks;
            }

            log.info("Crawling {} blocks for network {} from block {} to {}", 
                    blocksToCrawl, network, lastCrawledBlock.add(BigInteger.ONE), currentBlock);

            for (BigInteger i = lastCrawledBlock.add(BigInteger.ONE); 
                 i.compareTo(lastCrawledBlock.add(blocksToCrawl)) <= 0; 
                 i = i.add(BigInteger.ONE)) {
                
                try {
                    crawlBlock(network, web3, i);
                } catch (Exception e) {
                    log.error("Error crawling block {} for network {}", i, network, e);
                }
            }

            log.info("Completed crawling {} blocks for network {}", blocksToCrawl, network);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Error in crawlNetworkData for network: {}", network, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Transactional
    public void crawlBlock(String network, Web3j web3, BigInteger blockNumber) {
        try {
            EthBlock blockResp = web3.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNumber), true).send();
            EthBlock.Block block = blockResp.getBlock();
            if (block == null) {
                log.warn("Block {} not found for network {}", blockNumber, network);
                return;
            }
            Block savedBlock = saveBlock(network, block);
            if (block.getTransactions() != null && !block.getTransactions().isEmpty()) {
                for (Object txResult : block.getTransactions()) {
                    if (txResult instanceof TransactionObject) {
                        TransactionObject tx = (TransactionObject) txResult;
                        try {
                            crawlTransaction(network, web3, tx, savedBlock);
                        } catch (Exception e) {
                            log.error("Error crawling transaction {} in block {}", tx.getHash(), blockNumber, e);
                        }
                    }
                }
            }
            log.debug("Successfully crawled block {} for network {}", blockNumber, network);
        } catch (Exception e) {
            log.error("Error crawling block {} for network {}", blockNumber, network, e);
            throw new RuntimeException("Failed to crawl block " + blockNumber, e);
        }
    }

    private Block saveBlock(String network, EthBlock.Block block) {
        Optional<Block> existingBlock = blockRepository.findByBlockHash(block.getHash());
        if (existingBlock.isPresent()) {
            return existingBlock.get();
        }
        BigInteger totalDifficulty = null;
        BigInteger difficulty = null;
        BigInteger gasLimit = null;
        BigInteger gasUsed = null;
        BigInteger baseFeePerGas = null;
        
        try {
            totalDifficulty = block.getTotalDifficulty();
        } catch (Exception e) {
            log.warn("Could not decode totalDifficulty for block {}: {}", block.getNumber(), e.getMessage());
        }
        
        try {
            difficulty = block.getDifficulty();
        } catch (Exception e) {
            log.warn("Could not decode difficulty for block {}: {}", block.getNumber(), e.getMessage());
        }
        
        try {
            gasLimit = block.getGasLimit();
        } catch (Exception e) {
            log.warn("Could not decode gasLimit for block {}: {}", block.getNumber(), e.getMessage());
        }
        
        try {
            gasUsed = block.getGasUsed();
        } catch (Exception e) {
            log.warn("Could not decode gasUsed for block {}: {}", block.getNumber(), e.getMessage());
        }
        
        try {
            baseFeePerGas = block.getBaseFeePerGas();
        } catch (Exception e) {
            log.debug("Could not decode baseFeePerGas for block {}: {}", block.getNumber(), e.getMessage());
        }

        Block newBlock = Block.builder()
                .blockNumber(block.getNumber())
                .blockHash(block.getHash())
                .parentHash(block.getParentHash())
                .network(network)
                .timestamp(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(block.getTimestamp().longValue()), 
                        ZoneOffset.UTC))
                .gasLimit(gasLimit)
                .gasUsed(gasUsed)
                .difficulty(difficulty)
                .totalDifficulty(totalDifficulty)
                .size(block.getSize().longValue())
                .transactionCount(block.getTransactions().size())
                .baseFeePerGas(baseFeePerGas)
                .extraData(block.getExtraData())
                .build();

        return blockRepository.save(newBlock);
    }

    private void crawlTransaction(String network, Web3j web3, TransactionObject tx, Block block) {

        Optional<Transaction> existingTx = transactionRepository.findByTransactionHash(tx.getHash());
        if (existingTx.isPresent()) {
            return;
        }

        TransactionReceipt receipt = null;
        try {
            receipt = web3.ethGetTransactionReceipt(tx.getHash()).send().getTransactionReceipt().orElse(null);
        } catch (Exception e) {
            log.warn("Could not get receipt for transaction {}", tx.getHash(), e);
        }

        BigInteger value = null;
        BigInteger gas = null;
        BigInteger gasPrice = null;
        BigInteger nonce = null;
        Integer transactionIndex = null;
        
        try {
            value = tx.getValue();
        } catch (Exception e) {
            log.warn("Could not decode value for transaction {}: {}", tx.getHash(), e.getMessage());
        }
        
        try {
            gas = tx.getGas();
        } catch (Exception e) {
            log.warn("Could not decode gas for transaction {}: {}", tx.getHash(), e.getMessage());
        }
        
        try {
            gasPrice = tx.getGasPrice();
        } catch (Exception e) {
            log.warn("Could not decode gasPrice for transaction {}: {}", tx.getHash(), e.getMessage());
        }
        
        try {
            nonce = tx.getNonce();
        } catch (Exception e) {
            log.warn("Could not decode nonce for transaction {}: {}", tx.getHash(), e.getMessage());
        }
        
        try {
            transactionIndex = tx.getTransactionIndex().intValue();
        } catch (Exception e) {
            log.warn("Could not decode transactionIndex for transaction {}: {}", tx.getHash(), e.getMessage());
        }

        Transaction transaction = Transaction.builder()
                .transactionHash(tx.getHash())
                .blockNumber(block.getBlockNumber())
                .block(block)
                .fromAddress(tx.getFrom())
                .toAddress(tx.getTo())
                .value(value)
                .gas(gas)
                .gasPrice(gasPrice)
                .nonce(nonce)
                .network(network)
                .timestamp(block.getTimestamp())
                .status(receipt != null ? (receipt.isStatusOK() ? "SUCCESS" : "FAILED") : "PENDING")
                .inputData(tx.getInput())
                .transactionIndex(transactionIndex)
                .isContractCreation(tx.getTo() == null)
                .contractAddress(receipt != null ? receipt.getContractAddress() : null)
                .logsCount(receipt != null ? receipt.getLogs().size() : 0)
                .build();

        if (receipt != null) {
            try {
                transaction.setGasUsed(receipt.getGasUsed());
            } catch (Exception e) {
                log.warn("Could not decode gasUsed from receipt for transaction {}: {}", tx.getHash(), e.getMessage());
            }
        }

        Transaction savedTx = transactionRepository.save(transaction);

        createAddressActivities(network, savedTx);
    }

    private void createAddressActivities(String network, Transaction transaction) {

        if (transaction.getFromAddress() != null) {
            AddressActivity fromActivity = AddressActivity.builder()
                    .address(transaction.getFromAddress())
                    .network(network)
                    .activityType("SEND")
                    .transactionHash(transaction.getTransactionHash())
                    .blockNumber(transaction.getBlockNumber())
                    .value(transaction.getValue())
                    .fromAddress(transaction.getFromAddress())
                    .toAddress(transaction.getToAddress())
                    .timestamp(transaction.getTimestamp())
                    .gasUsed(transaction.getGasUsed())
                    .gasPrice(transaction.getGasPrice())
                    .isContract(transaction.getIsContractCreation())
                    .build();

            addressActivityRepository.save(fromActivity);
        }

        if (transaction.getToAddress() != null) {
            AddressActivity toActivity = AddressActivity.builder()
                    .address(transaction.getToAddress())
                    .network(network)
                    .activityType("RECEIVE")
                    .transactionHash(transaction.getTransactionHash())
                    .blockNumber(transaction.getBlockNumber())
                    .value(transaction.getValue())
                    .fromAddress(transaction.getFromAddress())
                    .toAddress(transaction.getToAddress())
                    .timestamp(transaction.getTimestamp())
                    .gasUsed(transaction.getGasUsed())
                    .gasPrice(transaction.getGasPrice())
                    .isContract(transaction.getIsContractCreation())
                    .build();

            addressActivityRepository.save(toActivity);
        }
    }

    private BigInteger getLastCrawledBlock(String network) {
        return blockRepository.findLatestBlockNumber(network)
                .orElse(BigInteger.ZERO);
    }

    @Async
    public CompletableFuture<Void> crawlBlockRange(String network, BigInteger startBlock, BigInteger endBlock) {
        try {
            Web3j web3 = web3Clients.get(network);
            if (web3 == null) {
                throw new IllegalArgumentException("No Web3 client found for network: " + network);
            }

            log.info("Crawling blocks {} to {} for network {}", startBlock, endBlock, network);

            for (BigInteger i = startBlock; i.compareTo(endBlock) <= 0; i = i.add(BigInteger.ONE)) {
                try {
                    crawlBlock(network, web3, i);
                } catch (Exception e) {
                    log.error("Error crawling block {} for network {}", i, network, e);
                }
            }

            log.info("Completed crawling blocks {} to {} for network {}", startBlock, endBlock, network);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Error in crawlBlockRange for network: {}", network, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<Void> crawlTokenData(String network, String contractAddress) {
        try {
            log.info("Token crawling not yet implemented for contract: {}", contractAddress);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Error crawling token data for contract: {}", contractAddress, e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
