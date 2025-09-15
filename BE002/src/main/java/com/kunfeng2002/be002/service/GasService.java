package com.kunfeng2002.be002.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GasService {

    private final Web3Service web3Service;
    private final StringRedisTemplate redisTemplate;

    public String getGasEstimate(String network, BigInteger gasLimit) {
        String cacheKey = "gas:" + network;
        String cachedPrice = redisTemplate.opsForValue().get(cacheKey);

        BigInteger gasPrice;
        if (cachedPrice != null) {
            gasPrice = new BigInteger(cachedPrice);
        } else {
            gasPrice = web3Service.getGasPrice(network);
            redisTemplate.opsForValue().set(cacheKey, gasPrice.toString(), 30, TimeUnit.SECONDS);
        }

        BigInteger fee = gasLimit != null
                ? gasPrice.multiply(gasLimit)
                : null;

        return buildResponse(network, gasPrice, gasLimit, fee);
    }

    private String buildResponse(String network, BigInteger gasPrice, BigInteger gasLimit, BigInteger fee) {
        StringBuilder sb = new StringBuilder();
        sb.append("Gas Estimate (").append(network.toUpperCase()).append(")\n");
        sb.append("Gas Price: ").append(gasPrice).append(" wei\n");
        if (gasLimit != null) sb.append("Gas Limit: ").append(gasLimit).append("\n");
        if (fee != null) sb.append("Total Fee: ").append(fee).append(" wei");
        return sb.toString();
    }
}
