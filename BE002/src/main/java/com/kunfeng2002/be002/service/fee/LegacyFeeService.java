package com.kunfeng2002.be002.service.fee;

import com.kunfeng2002.be002.dto.request.FeeRequest;
import com.kunfeng2002.be002.dto.response.FeeLevel;
import com.kunfeng2002.be002.dto.response.FeeResponse;
import com.kunfeng2002.be002.entity.NetworkType;
import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGasPrice;

import java.math.BigDecimal;
import java.math.BigInteger;

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
            EthGasPrice gasPriceResponse = web3.ethGasPrice().send();
            BigInteger gasPrice = gasPriceResponse.getGasPrice();

            BigInteger gasLimit = request.getGasLimit() != null ? request.getGasLimit() : BigInteger.valueOf(21000);

            return FeeResponse.builder()
                    .network(networkType.name())
                    .slow(buildFeeLevel(gasPrice, gasLimit, request.getSlowMultiplier()))
                    .recommended(buildFeeLevel(gasPrice, gasLimit, request.getRecommendedMultiplier()))
                    .fast(buildFeeLevel(gasPrice, gasLimit, request.getFastMultiplier()))
                    .build();

        } catch (Exception e) {
            log.error("Failed to fetch gas price from {} RPC", networkType, e);
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
