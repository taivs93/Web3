package com.kunfeng2002.be002.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal totalValue;
    private BigDecimal totalPnl;
    private BigDecimal totalPnlPercentage;
    private Integer tokenCount;
    private LocalDateTime createdAt;
    private List<TokenHoldingResponse> tokens;
}
