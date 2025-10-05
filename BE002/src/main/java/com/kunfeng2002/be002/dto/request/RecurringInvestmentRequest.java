package com.kunfeng2002.be002.dto.request;

import com.kunfeng2002.be002.entity.InvestmentFrequency;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class RecurringInvestmentRequest {
    private String coinSymbol;
    private String coinAddress;
    private BigDecimal amount;
    private InvestmentFrequency frequency;
    private LocalTime notificationTime;
}

