package com.kunfeng2002.be002.service.fee;

import java.math.BigDecimal;
import java.math.BigInteger;

public class EvmGasFeeCalculator implements GasFeeCalculatorStrategy {

    @Override
    public BigDecimal calculate(BigInteger gasUsed, BigInteger gasPrice, BigInteger l1Fee) {
        return new BigDecimal(gasUsed.multiply(gasPrice));
    }
}
