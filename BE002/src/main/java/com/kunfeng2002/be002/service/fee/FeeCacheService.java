package com.kunfeng2002.be002.service.fee;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class FeeCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String GAS_PRICE_KEY = "gasprice:";
    private static final String BASE_FEE_KEY = "basefee:";
    private static final String PRIORITY_FEE_KEY = "priorityfee:";

    private static final Duration TTL = Duration.ofSeconds(15);

    public void updateGasPrice(String network, BigInteger gasPrice) {
        redisTemplate.opsForValue().set(GAS_PRICE_KEY + network, gasPrice.toString(), TTL);
    }

    public BigInteger getGasPrice(String network) {
        String v = redisTemplate.opsForValue().get(GAS_PRICE_KEY + network);
        return v != null ? new BigInteger(v) : null;
    }

    public void updateBaseFee(String network, BigInteger baseFee, BigInteger priorityFee) {
        redisTemplate.opsForValue().set(BASE_FEE_KEY + network, baseFee.toString(), TTL);
        redisTemplate.opsForValue().set(PRIORITY_FEE_KEY + network, priorityFee.toString(), TTL);
    }

    public BigInteger getBaseFee(String network) {
        String v = redisTemplate.opsForValue().get(BASE_FEE_KEY + network);
        return v != null ? new BigInteger(v) : null;
    }

    public BigInteger getPriorityFee(String network) {
        String v = redisTemplate.opsForValue().get(PRIORITY_FEE_KEY + network);
        return v != null ? new BigInteger(v) : null;
    }
}