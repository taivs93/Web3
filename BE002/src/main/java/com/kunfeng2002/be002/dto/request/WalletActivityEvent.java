package com.kunfeng2002.be002.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kunfeng2002.be002.entity.NetworkType;
import lombok.*;

import java.math.BigInteger;

@Getter
@AllArgsConstructor
public class WalletActivityEvent {

    @JsonProperty("chain_id")
    private Long chainId;

    @JsonProperty("network")
    private NetworkType network;

    @JsonProperty("transaction_hash")
    private String transactionHash;

    @JsonProperty("from_address")
    private String fromAddress;

    @JsonProperty("to_address")
    private String toAddress;

    @JsonProperty("value")
    private BigInteger value;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("block_number")
    private String blockNumber;

    @JsonProperty("gas_price")
    private BigInteger gasPrice;

    @JsonProperty("gas_used")
    private BigInteger gasUsed;

    @JsonProperty("gas_limit")
    private BigInteger gasLimit;

    @JsonProperty("l1_fee")
    private BigInteger l1Fee;

    @JsonProperty("status")
    private Boolean status;
}
