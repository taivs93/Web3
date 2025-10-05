package com.kunfeng2002.be002.dto.response;

import com.kunfeng2002.be002.entity.InvestmentFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringInvestmentResponse {
    private Long id;
    private String coinSymbol;
    private String coinAddress;
    private BigDecimal amount;
    private InvestmentFrequency frequency;
    private LocalTime notificationTime;
    private LocalDateTime nextNotificationDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

