package com.kunfeng2002.be002.dto.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class FeeRequestValidator {

    private static final Set<String> SUPPORTED_NETWORKS = Set.of(
        "ETHEREUM", "ETH", "BSC", "BINANCE", "ARBITRUM", "OPTIMISM", "AVALANCHE", "AVAX"
    );

    private static final BigInteger MIN_GAS_LIMIT = BigInteger.valueOf(21000);
    private static final BigInteger MAX_GAS_LIMIT = BigInteger.valueOf(10_000_000);
    
    private static final BigDecimal MIN_MULTIPLIER = new BigDecimal("0.1");
    private static final BigDecimal MAX_MULTIPLIER = new BigDecimal("10.0");

    public ValidationResult validateFeeRequest(FeeRequest request, String network) {
        List<String> errors = new ArrayList<>();

        // Validate network
        if (network == null || network.trim().isEmpty()) {
            errors.add("Network is required");
        } else if (!SUPPORTED_NETWORKS.contains(network.toUpperCase())) {
            errors.add("Unsupported network: " + network + ". Supported networks: " + SUPPORTED_NETWORKS);
        }

        // Validate gas limit
        if (request.getGasLimit() != null) {
            if (request.getGasLimit().compareTo(MIN_GAS_LIMIT) < 0) {
                errors.add("Gas limit too low. Minimum: " + MIN_GAS_LIMIT);
            }
            if (request.getGasLimit().compareTo(MAX_GAS_LIMIT) > 0) {
                errors.add("Gas limit too high. Maximum: " + MAX_GAS_LIMIT);
            }
        }

        // Validate multipliers
        validateMultiplier(request.getSlowMultiplier(), "slowMultiplier", errors);
        validateMultiplier(request.getRecommendedMultiplier(), "recommendedMultiplier", errors);
        validateMultiplier(request.getFastMultiplier(), "fastMultiplier", errors);

        // Validate percentile
        if (request.getPercentile() != null) {
            if (request.getPercentile() < 1.0 || request.getPercentile() > 99.0) {
                errors.add("Percentile must be between 1.0 and 99.0");
            }
        }

        // Validate block count
        if (request.getBlockCount() != null) {
            if (request.getBlockCount() < 1 || request.getBlockCount() > 100) {
                errors.add("Block count must be between 1 and 100");
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    private void validateMultiplier(BigDecimal multiplier, String fieldName, List<String> errors) {
        if (multiplier != null) {
            if (multiplier.compareTo(MIN_MULTIPLIER) < 0) {
                errors.add(fieldName + " too low. Minimum: " + MIN_MULTIPLIER);
            }
            if (multiplier.compareTo(MAX_MULTIPLIER) > 0) {
                errors.add(fieldName + " too high. Maximum: " + MAX_MULTIPLIER);
            }
        }
    }

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}
