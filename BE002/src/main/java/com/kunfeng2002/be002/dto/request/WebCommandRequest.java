

package com.kunfeng2002.be002.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebCommandRequest {
    private String command;
    private String walletAddress;
    private String argument;
}
