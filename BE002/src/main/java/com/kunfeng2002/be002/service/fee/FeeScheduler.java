package com.kunfeng2002.be002.service.fee;

import com.kunfeng2002.be002.dto.request.FeeRequest;
import com.kunfeng2002.be002.dto.response.FeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FeeScheduler {

    private final List<FeeService> feeServices;
    private final FeeCacheService cacheService;

    @Scheduled(fixedRate = 5000)
    public void refreshAllFees() {
        FeeRequest defaultRequest = FeeRequest.builder()
                .gasLimit(BigInteger.valueOf(21000))
                .build();

        for (FeeService service : feeServices) {
            FeeResponse feeResponse = service.getFeeEstimate(defaultRequest);
            if (feeResponse.getSlow() != null) {
                cacheService.update(service.getNetwork(), feeResponse);
            }
        }
    }
}

