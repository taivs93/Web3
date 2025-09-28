package com.kunfeng2002.be002.service.fee.impl;

import com.kunfeng2002.be002.dto.request.FeeRequest;
import com.kunfeng2002.be002.dto.response.FeeLevel;
import com.kunfeng2002.be002.dto.response.FeeResponse;
import com.kunfeng2002.be002.entity.NetworkType;
import com.kunfeng2002.be002.service.fee.FeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthFeeHistory;
import org.web3j.protocol.core.methods.response.EthGasPrice;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EthereumFeeService implements FeeService {

    private final Web3j web3;

    private static final BigInteger DEFAULT_GAS_PRICE = BigInteger.valueOf(30_000_000_000L); 
    private static final BigInteger DEFAULT_PRIORITY_FEE = BigInteger.valueOf(2_000_000_000L); 

    public EthereumFeeService(Map<String, Web3j> web3Clients) {
        this.web3 = getClient(web3Clients, "ETH");
    }

    private static Web3j getClient(Map<String, Web3j> web3Clients, String key) {
        Web3j client = web3Clients.get(key);
        if (client == null) throw new IllegalStateException("Web3j not found for key: " + key);
        return client;
    }

    @Override
    public NetworkType getNetwork() {
        return NetworkType.ETHEREUM;
    }

    @Override
    public FeeResponse getFeeEstimate(FeeRequest request) {
        try {
            BigInteger baseFee = null;
            BigInteger priorityFee = null;

            try {
                EthGasPrice gasPriceResponse = web3.ethGasPrice().send();
                baseFee = gasPriceResponse.getGasPrice();
                log.info("[ETH] eth_gasPrice response: {}", baseFee);
            } catch (Exception e) {
                log.warn("[ETH] eth_gasPrice invalid, fallback to eth_feeHistory");
            }

            if (baseFee == null) {
                try {
                    EthBlock.Block latestBlock = web3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false)
                            .send().getBlock();
                    if (latestBlock != null && latestBlock.getBaseFeePerGas() != null) {
                        baseFee = latestBlock.getBaseFeePerGas();
                        log.info("[ETH] Fallback baseFee from latest block: {}", baseFee);
                    }
                } catch (Exception e) {
                    log.warn("[ETH] Cannot get latestBlock: {}", e.getMessage());
                }
            }

            if (baseFee == null) {
                baseFee = DEFAULT_GAS_PRICE;
                log.warn("[ETH] Use default baseFee: {}", baseFee);
            }

            try {
                int blockCount = request.getBlockCount() != null ? request.getBlockCount().intValue() : 5;
                double percentile = request.getPercentile() != null ? request.getPercentile() : 50.0;

                EthFeeHistory feeHistory = web3.ethFeeHistory(
                        blockCount,
                        DefaultBlockParameterName.LATEST,
                        List.of(percentile)
                ).send();

                if (feeHistory.getResult() != null && feeHistory.getResult().getReward() != null && !feeHistory.getResult().getReward().isEmpty()) {
                    List<List<BigInteger>> rewards = feeHistory.getResult().getReward();
                    priorityFee = rewards.stream()
                            .map(r -> r.get(0))
                            .reduce(BigInteger.ZERO, BigInteger::add)
                            .divide(BigInteger.valueOf(rewards.size()));
                    log.info("[ETH] Calculated priorityFee from feeHistory: {}", priorityFee);
                }
            } catch (Exception e) {
                log.warn("[ETH] Cannot get data from history: {}", e.getMessage());
            }

            if (priorityFee == null) {
                priorityFee = DEFAULT_PRIORITY_FEE;
                log.warn("[ETH] Sử dụng default priorityFee: {}", priorityFee);
            }

            BigInteger gasLimit = request.getGasLimit() != null ? request.getGasLimit() : BigInteger.valueOf(21000);

            return FeeResponse.builder()
                    .network("ETH")
                    .slow(buildFeeLevel(baseFee, priorityFee, gasLimit, request.getSlowMultiplier()))
                    .recommended(buildFeeLevel(baseFee, priorityFee, gasLimit, request.getRecommendedMultiplier()))
                    .fast(buildFeeLevel(baseFee, priorityFee, gasLimit, request.getFastMultiplier()))
                    .build();

        } catch (Exception e) {
            log.error("[ETH] Failed to fetch fee", e);
            return FeeResponse.builder().network("ETH").build();
        }
    }

    private FeeLevel buildFeeLevel(BigInteger baseFee, BigInteger priorityFee,
                                   BigInteger gasLimit, BigDecimal multiplier) {
        BigInteger maxFeePerGas = new BigDecimal(baseFee.add(priorityFee))
                .multiply(multiplier)
                .toBigInteger();

        return FeeLevel.builder()
                .maxFeePerGas(maxFeePerGas)
                .priorityFeePerGas(priorityFee)
                .gasLimit(gasLimit)
                .totalFee(maxFeePerGas.multiply(gasLimit).toString())
                .build();
    }
}
