package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.request.FeeRequest;
import com.kunfeng2002.be002.dto.request.FeeRequestValidator;
import com.kunfeng2002.be002.dto.response.FeeLevel;
import com.kunfeng2002.be002.dto.response.FeeResponse;
import com.kunfeng2002.be002.service.fee.FeeCacheService;
import com.kunfeng2002.be002.service.fee.FeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GasService {

    private final List<FeeService> feeServices;
    private final FeeCacheService cacheService;
    private final FeeRequestValidator validator;
    private final GasTrackerService gasTrackerService;

    public FeeResponse getFeeEstimate(String network, FeeRequest request) {
        // Validate request
        FeeRequestValidator.ValidationResult validationResult = validator.validateFeeRequest(request, network);
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException("Invalid request: " + validationResult.getErrorMessage());
        }

        try {
            // Normalize network name
            String normalizedNetwork = normalizeNetworkName(network);
            
            FeeService service = feeServices.stream()
                    .filter(s -> s.getNetwork().name().equalsIgnoreCase(normalizedNetwork))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported network: " + network));

            // Try enhanced gas tracking first
            BigInteger enhancedGasPrice = getEnhancedGasPrice(normalizedNetwork);
            if (enhancedGasPrice != null) {
                return buildResponseFromGasPrice(normalizedNetwork, enhancedGasPrice, request);
            }

            // Fallback to cached price
            BigInteger cachedGasPrice = cacheService.getGasPrice(normalizedNetwork);
            if (cachedGasPrice != null) {
                return buildResponseFromGasPrice(normalizedNetwork, cachedGasPrice, request);
            }

            // Final fallback to service implementation
            FeeResponse freshResponse = service.getFeeEstimate(request);
            
            // Cache the fresh response
            if (freshResponse.getRecommended() != null) {
                cacheService.updateGasPrice(normalizedNetwork, freshResponse.getRecommended().getMaxFeePerGas());
            }
            
            return freshResponse;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get fee estimate: " + e.getMessage(), e);
        }
    }

    private String normalizeNetworkName(String network) {
        switch (network.toUpperCase()) {
            case "ETH":
            case "ETHEREUM":
                return "ETHEREUM";
            case "BSC":
            case "BINANCE":
                return "BSC";
            case "AVAX":
                return "AVALANCHE";
            default:
                return network.toUpperCase();
        }
    }

    private BigInteger getEnhancedGasPrice(String network) {
        try {
            switch (network.toUpperCase()) {
                case "ETHEREUM":
                    return gasTrackerService.getEthereumGasPrice();
                case "BSC":
                    return gasTrackerService.getBscGasPrice();
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private FeeResponse buildResponseFromGasPrice(String network, BigInteger gasPrice, FeeRequest request) {
        return FeeResponse.builder()
                .network(network)
                .slow(serviceLevel(gasPrice, request, request.getSlowMultiplier()))
                .recommended(serviceLevel(gasPrice, request, request.getRecommendedMultiplier()))
                .fast(serviceLevel(gasPrice, request, request.getFastMultiplier()))
                .build();
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
