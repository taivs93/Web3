package com.kunfeng2002.be002.dto.request;

import java.math.BigDecimal;
import java.math.BigInteger;

public class FeeRequest {
    private BigInteger gasLimit;
    private BigDecimal slowMultiplier = BigDecimal.valueOf(1.0);
    private BigDecimal recommendedMultiplier = BigDecimal.valueOf(1.2);
    private BigDecimal fastMultiplier = BigDecimal.valueOf(1.5);
    private int blockCount = 5;
    private double percentile = 25.0;

    // Default constructor
    public FeeRequest() {
    }

    // Builder pattern constructor
    public FeeRequest(BigInteger gasLimit, BigDecimal slowMultiplier, BigDecimal recommendedMultiplier,
            BigDecimal fastMultiplier, int blockCount, double percentile) {
        this.gasLimit = gasLimit;
        this.slowMultiplier = slowMultiplier != null ? slowMultiplier : BigDecimal.valueOf(1.0);
        this.recommendedMultiplier = recommendedMultiplier != null ? recommendedMultiplier : BigDecimal.valueOf(1.2);
        this.fastMultiplier = fastMultiplier != null ? fastMultiplier : BigDecimal.valueOf(1.5);
        this.blockCount = blockCount;
        this.percentile = percentile;
    }

    // Getters
    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public BigDecimal getSlowMultiplier() {
        return slowMultiplier;
    }

    public BigDecimal getRecommendedMultiplier() {
        return recommendedMultiplier;
    }

    public BigDecimal getFastMultiplier() {
        return fastMultiplier;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public double getPercentile() {
        return percentile;
    }

    // Setters
    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public void setSlowMultiplier(BigDecimal slowMultiplier) {
        this.slowMultiplier = slowMultiplier;
    }

    public void setRecommendedMultiplier(BigDecimal recommendedMultiplier) {
        this.recommendedMultiplier = recommendedMultiplier;
    }

    public void setFastMultiplier(BigDecimal fastMultiplier) {
        this.fastMultiplier = fastMultiplier;
    }

    public void setBlockCount(int blockCount) {
        this.blockCount = blockCount;
    }

    public void setPercentile(double percentile) {
        this.percentile = percentile;
    }

    // Builder class
    public static class Builder {
        private BigInteger gasLimit;
        private BigDecimal slowMultiplier = BigDecimal.valueOf(1.0);
        private BigDecimal recommendedMultiplier = BigDecimal.valueOf(1.2);
        private BigDecimal fastMultiplier = BigDecimal.valueOf(1.5);
        private int blockCount = 5;
        private double percentile = 25.0;

        public Builder gasLimit(BigInteger gasLimit) {
            this.gasLimit = gasLimit;
            return this;
        }

        public Builder slowMultiplier(BigDecimal slowMultiplier) {
            this.slowMultiplier = slowMultiplier;
            return this;
        }

        public Builder recommendedMultiplier(BigDecimal recommendedMultiplier) {
            this.recommendedMultiplier = recommendedMultiplier;
            return this;
        }

        public Builder fastMultiplier(BigDecimal fastMultiplier) {
            this.fastMultiplier = fastMultiplier;
            return this;
        }

        public Builder blockCount(int blockCount) {
            this.blockCount = blockCount;
            return this;
        }

        public Builder percentile(double percentile) {
            this.percentile = percentile;
            return this;
        }

        public FeeRequest build() {
            return new FeeRequest(gasLimit, slowMultiplier, recommendedMultiplier,
                    fastMultiplier, blockCount, percentile);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
