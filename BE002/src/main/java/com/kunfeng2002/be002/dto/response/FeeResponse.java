package com.kunfeng2002.be002.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeResponse {
    private String network;
    private FeeLevel slow;
    private FeeLevel recommended;
    private FeeLevel fast;
}
