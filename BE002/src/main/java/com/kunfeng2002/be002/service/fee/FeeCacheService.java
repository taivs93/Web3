package com.kunfeng2002.be002.service.fee;

import com.kunfeng2002.be002.dto.response.FeeResponse;
import com.kunfeng2002.be002.entity.NetworkType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class FeeCacheService {
    private final RedisTemplate<String, FeeResponse> redisTemplate;

    @Value("${app.fee-ttl:15}")
    private long cacheTtl;

    public void update(NetworkType network, FeeResponse response) {
        redisTemplate.opsForValue()
                .set("fee:" + network.name().toLowerCase(), response, Duration.ofSeconds(cacheTtl));
    }

    public FeeResponse get(NetworkType network) {
        return redisTemplate.opsForValue().get("fee:" + network.name().toLowerCase());
    }
}

