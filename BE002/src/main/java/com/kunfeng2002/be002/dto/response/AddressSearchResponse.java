package com.kunfeng2002.be002.dto.response;

import com.kunfeng2002.be002.entity.AddressActivity;
import com.kunfeng2002.be002.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressSearchResponse {

    private String query;
    private String network;
    private List<TransactionInfo> transactions;
    private List<AddressActivityInfo> activities;
    private long totalTransactions;
    private long totalActivities;
    private BigInteger totalSent;
    private BigInteger totalReceived;
    private boolean found;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionInfo {
        private Long id;
        private String transactionHash;
        private BigInteger blockNumber;
        private String fromAddress;
        private String toAddress;
        private BigInteger value;
        private BigInteger gas;
        private BigInteger gasPrice;
        private BigInteger gasUsed;
        private BigInteger nonce;
        private String network;
        private LocalDateTime timestamp;
        private String status;
        private String inputData;
        private Integer transactionIndex;
        private Boolean isContractCreation;
        private String contractAddress;
        private Integer logsCount;

        public static TransactionInfo fromEntity(Transaction transaction) {
            if (transaction == null) return null;
            
            return TransactionInfo.builder()
                    .id(transaction.getId())
                    .transactionHash(transaction.getTransactionHash())
                    .blockNumber(transaction.getBlockNumber())
                    .fromAddress(transaction.getFromAddress())
                    .toAddress(transaction.getToAddress())
                    .value(transaction.getValue())
                    .gas(transaction.getGas())
                    .gasPrice(transaction.getGasPrice())
                    .gasUsed(transaction.getGasUsed())
                    .nonce(transaction.getNonce())
                    .network(transaction.getNetwork())
                    .timestamp(transaction.getTimestamp())
                    .status(transaction.getStatus())
                    .inputData(transaction.getInputData())
                    .transactionIndex(transaction.getTransactionIndex())
                    .isContractCreation(transaction.getIsContractCreation())
                    .contractAddress(transaction.getContractAddress())
                    .logsCount(transaction.getLogsCount())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressActivityInfo {
        private Long id;
        private String address;
        private String network;
        private String activityType;
        private String transactionHash;
        private BigInteger blockNumber;
        private BigInteger value;
        private String tokenAddress;
        private String tokenSymbol;
        private String fromAddress;
        private String toAddress;
        private LocalDateTime timestamp;
        private BigInteger gasUsed;
        private BigInteger gasPrice;
        private Boolean isContract;
        private String contractName;
        private String methodName;

        public static AddressActivityInfo fromEntity(AddressActivity activity) {
            if (activity == null) return null;
            
            return AddressActivityInfo.builder()
                    .id(activity.getId())
                    .address(activity.getAddress())
                    .network(activity.getNetwork())
                    .activityType(activity.getActivityType())
                    .transactionHash(activity.getTransactionHash())
                    .blockNumber(activity.getBlockNumber())
                    .value(activity.getValue())
                    .tokenAddress(activity.getTokenAddress())
                    .tokenSymbol(activity.getTokenSymbol())
                    .fromAddress(activity.getFromAddress())
                    .toAddress(activity.getToAddress())
                    .timestamp(activity.getTimestamp())
                    .gasUsed(activity.getGasUsed())
                    .gasPrice(activity.getGasPrice())
                    .isContract(activity.getIsContract())
                    .contractName(activity.getContractName())
                    .methodName(activity.getMethodName())
                    .build();
        }
    }
}
