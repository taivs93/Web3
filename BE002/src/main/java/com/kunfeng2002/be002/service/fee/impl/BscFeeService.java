package com.kunfeng2002.be002.service.fee.impl;

import com.kunfeng2002.be002.dto.request.FeeRequest;
import com.kunfeng2002.be002.dto.response.FeeLevel;
import com.kunfeng2002.be002.dto.response.FeeResponse;
import com.kunfeng2002.be002.entity.NetworkType;
import com.kunfeng2002.be002.service.fee.FeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGasPrice;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

@Service
@Slf4j
public class BscFeeService implements FeeService {
    
    private final Web3j web3;
    private static final BigInteger DEFAULT_GAS_PRICE = BigInteger.valueOf(5_000_000_000L); // 5 Gwei for BSC
    private static final BigInteger MIN_GAS_PRICE = BigInteger.valueOf(3_000_000_000L); // 3 Gwei
    private static final BigInteger MAX_GAS_PRICE = BigInteger.valueOf(20_000_000_000L); // 20 Gwei

    public BscFeeService(Map<String, Web3j> web3Clients) {
        this.web3 = getClient(web3Clients, "BSC");
    }

    private static Web3j getClient(Map<String, Web3j> web3Clients, String key) {
        Web3j client = web3Clients.get(key);
        if (client == null) throw new IllegalStateException("Web3j not found for key: " + key);
        return client;
    }

    @Override
    public NetworkType getNetwork() {
        return NetworkType.BSC;
    }

    @Override
    public FeeResponse getFeeEstimate(FeeRequest request) {
        try {
            BigInteger gasPrice = getNetworkGasPrice();
            BigInteger gasLimit = request.getGasLimit() != null ? request.getGasLimit() : BigInteger.valueOf(21000);
            
            // Validate gas limit
            if (gasLimit.compareTo(BigInteger.valueOf(21000)) < 0) {
                gasLimit = BigInteger.valueOf(21000);
            }
            if (gasLimit.compareTo(BigInteger.valueOf(10_000_000)) > 0) {
                gasLimit = BigInteger.valueOf(10_000_000);
            }

            return FeeResponse.builder()
                    .network("BSC")
                    .slow(buildFeeLevel(gasPrice, gasLimit, request.getSlowMultiplier()))
                    .recommended(buildFeeLevel(gasPrice, gasLimit, request.getRecommendedMultiplier()))
                    .fast(buildFeeLevel(gasPrice, gasLimit, request.getFastMultiplier()))
                    .build();

        } catch (Exception e) {
            log.error("[BSC] Failed to fetch fee estimate", e);
            return buildFallbackResponse(request);
        }
    }

    private BigInteger getNetworkGasPrice() {
        try {
            EthGasPrice gasPriceResponse = web3.ethGasPrice().send();
            BigInteger networkGasPrice = gasPriceResponse.getGasPrice();
            
            // Validate gas price bounds for BSC
            if (networkGasPrice.compareTo(MIN_GAS_PRICE) < 0) {
                log.warn("[BSC] Network gas price {} too low, using minimum {}", networkGasPrice, MIN_GAS_PRICE);
                return MIN_GAS_PRICE;
            }
            if (networkGasPrice.compareTo(MAX_GAS_PRICE) > 0) {
                log.warn("[BSC] Network gas price {} too high, using maximum {}", networkGasPrice, MAX_GAS_PRICE);
                return MAX_GAS_PRICE;
            }
            
            log.info("[BSC] Network gas price: {}", networkGasPrice);
            return networkGasPrice;
            
        } catch (Exception e) {
            log.warn("[BSC] Failed to get network gas price, using default: {}", DEFAULT_GAS_PRICE);
            return DEFAULT_GAS_PRICE;
        }
    }

    private FeeLevel buildFeeLevel(BigInteger gasPrice, BigInteger gasLimit, BigDecimal multiplier) {
        if (multiplier == null) {
            multiplier = BigDecimal.ONE;
        }
        
        BigInteger adjustedGasPrice = new BigDecimal(gasPrice)
                .multiply(multiplier)
                .toBigInteger();
        
        // BSC doesn't use EIP-1559, so priorityFeePerGas is 0
        return FeeLevel.builder()
                .maxFeePerGas(adjustedGasPrice)
                .priorityFeePerGas(BigInteger.ZERO)
                .gasLimit(gasLimit)
                .totalFee(adjustedGasPrice.multiply(gasLimit).toString())
                .build();
    }

    private FeeResponse buildFallbackResponse(FeeRequest request) {
        BigInteger gasLimit = request.getGasLimit() != null ? request.getGasLimit() : BigInteger.valueOf(21000);
        
        return FeeResponse.builder()
                .network("BSC")
                .slow(buildFeeLevel(DEFAULT_GAS_PRICE, gasLimit, request.getSlowMultiplier()))
                .recommended(buildFeeLevel(DEFAULT_GAS_PRICE, gasLimit, request.getRecommendedMultiplier()))
                .fast(buildFeeLevel(DEFAULT_GAS_PRICE, gasLimit, request.getFastMultiplier()))
                .build();
    }
}
