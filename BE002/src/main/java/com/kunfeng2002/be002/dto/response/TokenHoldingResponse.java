package com.kunfeng2002.be002.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenHoldingResponse {
    private Long id;
    private String symbol;
    private String name;
    private BigDecimal amount;
    private BigDecimal averageBuyPrice;
    private BigDecimal currentPrice;
    private BigDecimal totalValue;
    private BigDecimal pnlPercentage;
    private BigDecimal pnlAmount;
    private LocalDateTime updatedAt;
}
