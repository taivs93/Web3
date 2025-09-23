package com.kunfeng2002.be002.dto.response;

import com.kunfeng2002.be002.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSearchResponse {

    private String query;
    private String network;
    private TransactionInfo transaction;
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
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

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
                    .createdAt(transaction.getCreatedAt())
                    .updatedAt(transaction.getUpdatedAt())
                    .build();
        }
    }
}
