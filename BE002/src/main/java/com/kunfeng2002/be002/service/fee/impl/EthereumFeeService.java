package com.kunfeng2002.be002.service.fee.impl;

//import com.kunfeng2002.be002.dto.request.FeeRequest;
//import com.kunfeng2002.be002.dto.response.FeeLevel;
//import com.kunfeng2002.be002.dto.response.FeeResponse;
//import com.kunfeng2002.be002.entity.NetworkType;
//import com.kunfeng2002.be002.service.fee.FeeService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Service;
//import org.web3j.protocol.Web3j;
//import org.web3j.protocol.core.DefaultBlockParameterName;
//import org.web3j.protocol.core.methods.response.EthBlock;
//import org.web3j.protocol.core.methods.response.EthFeeHistory;
//
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.util.List;
//
//@Service
//@Slf4j
//public class EthereumFeeService implements FeeService {
//
//    private final Web3j web3;
//
//    public EthereumFeeService(@Qualifier("ETH") Web3j web3) {
//        this.web3 = web3;
//    }
//
//    @Override
//    public NetworkType getNetwork() {
//        return NetworkType.ETHEREUM;
//    }
//
//    @Override
//    public FeeResponse getFeeEstimate(FeeRequest request) {
//        try {
//            EthBlock.Block latestBlock = web3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false)
//                    .send().getBlock();
//            BigInteger baseFee = latestBlock.getBaseFeePerGas();
//
//            EthFeeHistory feeHistory = web3.ethFeeHistory(
//                            request.getBlockCount(),
//                            DefaultBlockParameterName.LATEST,
//                            List.of(request.getPercentile()))
//                    .send();
//
//            List<List<BigInteger>> rewards = feeHistory.getResult().getReward();
//            BigInteger priorityFee = rewards.stream()
//                    .map(r -> r.get(0))
//                    .reduce(BigInteger.ZERO, BigInteger::add)
//                    .divide(BigInteger.valueOf(rewards.size()));
//
//            BigInteger gasLimit = request.getGasLimit() != null ? request.getGasLimit() : BigInteger.valueOf(21000);
//
//            return FeeResponse.builder()
//                    .network("ETH")
//                    .slow(buildFeeLevel(baseFee, priorityFee, gasLimit, request.getSlowMultiplier()))
//                    .recommended(buildFeeLevel(baseFee, priorityFee, gasLimit, request.getRecommendedMultiplier()))
//                    .fast(buildFeeLevel(baseFee, priorityFee, gasLimit, request.getFastMultiplier()))
//                    .build();
//
//        } catch (Exception e) {
//            log.error("Failed to fetch fee from Ethereum RPC", e);
//            return FeeResponse.builder().network("ETH").build();
//        }
//    }
//
//
//    private FeeLevel buildFeeLevel(BigInteger baseFee, BigInteger priorityFee,
//                                   BigInteger gasLimit, BigDecimal multiplier) {
//        BigInteger maxFeePerGas = new BigDecimal(baseFee.add(priorityFee))
//                .multiply(multiplier)
//                .toBigInteger();
//
//        return FeeLevel.builder()
//                .maxFeePerGas(maxFeePerGas)
//                .priorityFeePerGas(priorityFee)
//                .gasLimit(gasLimit)
//                .totalFee(maxFeePerGas.multiply(gasLimit).toString())
//                .build();
//    }
//}
