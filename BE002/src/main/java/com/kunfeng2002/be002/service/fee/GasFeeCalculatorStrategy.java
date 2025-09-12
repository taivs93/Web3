package com.kunfeng2002.be002.service.fee;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface GasFeeCalculatorStrategy {
    BigDecimal calculate(BigInteger gasUsed, BigInteger gasPrice, BigInteger l1Fee);
}