package com.kunfeng2002.be002.service.fee;

import java.math.BigDecimal;
import java.math.BigInteger;

public class L2RollupGasFeeCalculator implements GasFeeCalculatorStrategy {
    @Override
    public BigDecimal calculate(BigInteger gasUsed, BigInteger gasPrice, BigInteger l1Fee) {
        return new BigDecimal(gasUsed.multiply(gasPrice).add(l1Fee));
    }
}