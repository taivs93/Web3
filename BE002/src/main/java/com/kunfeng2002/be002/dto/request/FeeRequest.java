package com.kunfeng2002.be002.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@Builder
public class FeeRequest {
    private BigInteger gasLimit;

    @Builder.Default
    private BigDecimal slowMultiplier = BigDecimal.valueOf(1.0);
    @Builder.Default
    private BigDecimal recommendedMultiplier = BigDecimal.valueOf(1.2);
    @Builder.Default
    private BigDecimal fastMultiplier = BigDecimal.valueOf(1.5);

    @Builder.Default
    private Integer blockCount = 5;
    @Builder.Default
    private Double percentile = 25.0;
}

