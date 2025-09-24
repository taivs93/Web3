package com.kunfeng2002.be002.service.fee;

import com.kunfeng2002.be002.dto.request.FeeRequest;
import com.kunfeng2002.be002.dto.response.FeeLevel;
import com.kunfeng2002.be002.dto.response.FeeResponse;
import com.kunfeng2002.be002.entity.NetworkType;
import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthFeeHistory;
import org.web3j.protocol.core.methods.response.EthGasPrice;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Slf4j
public abstract class LegacyFeeService implements FeeService {

    private final Web3j web3;
    private final NetworkType networkType;

    protected LegacyFeeService(Web3j web3, NetworkType networkType) {
        this.web3 = web3;
        this.networkType = networkType;
    }

    @Override
    public NetworkType getNetwork() {
        return networkType;
    }

    @Override
    public FeeResponse getFeeEstimate(FeeRequest request) {
        try {
            BigInteger gasPrice;

            try {
                EthGasPrice gasPriceResponse = web3.ethGasPrice().send();
                gasPrice = gasPriceResponse.getGasPrice();
                log.info("[{}] Raw eth_gasPrice response: {}", networkType, gasPrice);
            } catch (Exception e) {
                log.warn("[{}] eth_gasPrice invalid, fallback to eth_feeHistory", networkType);

                EthBlock.Block latestBlock = web3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false)
                        .send().getBlock();
                BigInteger baseFee = latestBlock.getBaseFeePerGas();

                int blockCount = request.getBlockCount() != null
                        ? request.getBlockCount().intValue()
                        : 5;

                double percentile = request.getPercentile() != null
                        ? request.getPercentile()
                        : 50.0;

                EthFeeHistory feeHistory = web3.ethFeeHistory(
                        blockCount,
                        DefaultBlockParameterName.LATEST,
                        List.of(percentile)
                ).send();
                List<List<BigInteger>> rewards = feeHistory.getResult().getReward();
                BigInteger priorityFee = rewards.stream()
                        .map(r -> r.get(0))
                        .reduce(BigInteger.ZERO, BigInteger::add)
                        .divide(BigInteger.valueOf(rewards.size()));

                gasPrice = baseFee.add(priorityFee);
                log.info("[{}] Fallback gas price (baseFee+priorityFee): {}", networkType, gasPrice);
            }

            BigInteger gasLimit = request.getGasLimit() != null ? request.getGasLimit() : BigInteger.valueOf(21000);

            return FeeResponse.builder()
                    .network(networkType.name())
                    .slow(buildFeeLevel(gasPrice, gasLimit, request.getSlowMultiplier()))
                    .recommended(buildFeeLevel(gasPrice, gasLimit, request.getRecommendedMultiplier()))
                    .fast(buildFeeLevel(gasPrice, gasLimit, request.getFastMultiplier()))
                    .build();

        } catch (Exception e) {
            log.error("[{}] Failed to fetch gas price", networkType, e);
            return FeeResponse.builder().network(networkType.name()).build();
        }
    }

    private FeeLevel buildFeeLevel(BigInteger gasPrice, BigInteger gasLimit, BigDecimal multiplier) {
        BigInteger finalGasPrice = new BigDecimal(gasPrice)
                .multiply(multiplier)
                .toBigInteger();

        return FeeLevel.builder()
                .maxFeePerGas(finalGasPrice)
                .priorityFeePerGas(BigInteger.ZERO)
                .gasLimit(gasLimit)
                .totalFee(finalGasPrice.multiply(gasLimit).toString())
                .build();
    }
}
