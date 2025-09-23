package com.kunfeng2002.be002.service.fee.impl;

import com.kunfeng2002.be002.entity.NetworkType;
import com.kunfeng2002.be002.service.fee.LegacyFeeService;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;

import java.util.Map;

@Service
public class BscFeeService extends LegacyFeeService {
    public BscFeeService(Map<String, Web3j> web3Clients) {
        super(getClient(web3Clients, "BSC"), NetworkType.BSC);
    }

    private static Web3j getClient(Map<String, Web3j> web3Clients, String key) {
        Web3j client = web3Clients.get(key);
        if (client == null) throw new IllegalStateException("Web3j not found for key: " + key);
        return client;
    }
}
