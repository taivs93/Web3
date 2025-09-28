package com.kunfeng2002.be002.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlertEvent {
    private String symbol;
    private double price;
    private String message;
}
