package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.request.FeeRequest;
import com.kunfeng2002.be002.dto.response.FeeLevel;
import com.kunfeng2002.be002.dto.response.FeeResponse;
import com.kunfeng2002.be002.service.fee.FeeCacheService;
import com.kunfeng2002.be002.service.fee.FeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GasService {

    private final List<FeeService> feeServices;
    private final FeeCacheService cacheService;

    public FeeResponse getFeeEstimate(String network, FeeRequest request) {
        FeeService service = feeServices.stream()
                .filter(s -> s.getNetwork().name().equalsIgnoreCase(network))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported network: " + network));

        BigInteger cachedGasPrice = cacheService.getGasPrice(network);
        if (cachedGasPrice != null) {
            return FeeResponse.builder()
                    .network(network)
                    .slow(serviceLevel(cachedGasPrice, request, request.getSlowMultiplier()))
                    .recommended(serviceLevel(cachedGasPrice, request, request.getRecommendedMultiplier()))
                    .fast(serviceLevel(cachedGasPrice, request, request.getFastMultiplier()))
                    .build();
        }

        FeeResponse freshResponse = service.getFeeEstimate(request);
        if (freshResponse.getRecommended() != null) {
            cacheService.updateGasPrice(network, freshResponse.getRecommended().getMaxFeePerGas());
        }
        return freshResponse;
    }

    private FeeLevel serviceLevel(
            BigInteger gasPrice, FeeRequest request, java.math.BigDecimal multiplier) {
        BigInteger gasLimit = request.getGasLimit() != null ? request.getGasLimit() : BigInteger.valueOf(21000);
        BigInteger finalGasPrice = multiplier.multiply(new java.math.BigDecimal(gasPrice)).toBigInteger();
        return com.kunfeng2002.be002.dto.response.FeeLevel.builder()
                .maxFeePerGas(finalGasPrice)
                .priorityFeePerGas(BigInteger.ZERO)
                .gasLimit(gasLimit)
                .totalFee(finalGasPrice.multiply(gasLimit).toString())
                .build();
    }

    public String getGasEstimate(String bsc, Object o) {
        return bsc;
    }
}
