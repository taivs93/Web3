package com.kunfeng2002.be002.service.fee.impl;

import com.kunfeng2002.be002.entity.NetworkType;
import com.kunfeng2002.be002.service.fee.LegacyFeeService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;

@Service
public class AvalancheFeeService extends LegacyFeeService {
    public AvalancheFeeService(@Qualifier("AVALANCHE") Web3j web3) {
        super(web3, NetworkType.AVALANCHE);
    }
}