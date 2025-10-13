package com.kunfeng2002.be002.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhaleAlertResponse {
    private String timestamp;
    private String emoji;
    private String amount;
    private String value;
    private String description;
    private String url;
}
