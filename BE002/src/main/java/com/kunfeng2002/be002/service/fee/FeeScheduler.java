// FeeScheduler.java
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
        FeeRequest baseRequest = FeeRequest.builder()
                .blockCount(5)
                .percentile(25)
                .build();

        for (FeeService service : feeServices) {
            FeeResponse response = service.getFeeEstimate(baseRequest);

            if (response.getRecommended() != null) {
                String network = service.getNetwork().name().toLowerCase();
                if (response.getRecommended().getPriorityFeePerGas() != null &&
                        response.getRecommended().getPriorityFeePerGas().compareTo(BigInteger.ZERO) > 0) {
                    cacheService.updateBaseFee(network,
                            response.getRecommended().getMaxFeePerGas()
                                    .subtract(response.getRecommended().getPriorityFeePerGas()),
                            response.getRecommended().getPriorityFeePerGas());
                } else {
                    cacheService.updateGasPrice(network, response.getRecommended().getMaxFeePerGas());
                }
            }
        }
    }
}
