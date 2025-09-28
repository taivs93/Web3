package com.kunfeng2002.be002.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTokenRequest {
    private Long portfolioId;
    private String tokenSymbol;
    private BigDecimal amount;
    private BigDecimal buyPrice;
}
