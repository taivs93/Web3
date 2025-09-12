package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.entity.NetworkType;
import com.kunfeng2002.be002.service.fee.EvmGasFeeCalculator;
import com.kunfeng2002.be002.service.fee.GasFeeCalculatorStrategy;
import com.kunfeng2002.be002.service.fee.L2RollupGasFeeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GasFeeCalculatorFactory {

    private final EvmGasFeeCalculator evmCalculator;
    private final L2RollupGasFeeCalculator l2Calculator;

    public GasFeeCalculatorStrategy getCalculator(NetworkType type) {
        return switch (type.getCategory()) {
            case EVM -> evmCalculator;
            case L2_ROLLUP -> l2Calculator;
        };
    }
}
