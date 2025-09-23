package com.kunfeng2002.be002.service.fee;

import com.kunfeng2002.be002.dto.request.FeeRequest;
import com.kunfeng2002.be002.dto.response.FeeResponse;
import com.kunfeng2002.be002.entity.NetworkType;

public interface FeeService {
    FeeResponse getFeeEstimate(FeeRequest request);
    NetworkType getNetwork();
}
