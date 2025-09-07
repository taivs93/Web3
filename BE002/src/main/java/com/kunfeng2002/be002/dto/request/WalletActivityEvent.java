package com.kunfeng2002.be002.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletActivityEvent {

    @JsonProperty("transaction_hash")
    private String transactionHash;

    @JsonProperty("from_address")
    private String fromAddress;

    @JsonProperty("to_address")
    private String toAddress;

    @JsonProperty("network")
    private String network;

    @JsonProperty("value")
    private String value;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("block_number")
    private String blockNumber;

    @JsonProperty("gas_price")
    private String gasPrice;

    @JsonProperty("gas_used")
    private String gasUsed;


    public WalletActivityEvent(String transactionHash, String fromAddress, String toAddress,
                               String network, String value, Long timestamp) {
        this.transactionHash = transactionHash;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.network = network;
        this.value = value;
        this.timestamp = timestamp;
    }
}