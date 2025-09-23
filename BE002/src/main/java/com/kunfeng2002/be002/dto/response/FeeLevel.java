package com.kunfeng2002.be002.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeLevel {
    private BigInteger maxFeePerGas;
    private BigInteger priorityFeePerGas;
    private BigInteger gasLimit;
    private String totalFee;
}
