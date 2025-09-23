package com.kunfeng2002.be002.dto.response;

import com.kunfeng2002.be002.entity.Block;
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
public class BlockSearchResponse {

    private String query;
    private String network;
    private BlockInfo block;
    private boolean found;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockInfo {
        private Long id;
        private BigInteger blockNumber;
        private String blockHash;
        private String parentHash;
        private String network;
        private LocalDateTime timestamp;
        private BigInteger gasLimit;
        private BigInteger gasUsed;
        private BigInteger difficulty;
        private BigInteger totalDifficulty;
        private Long size;
        private Integer transactionCount;
        private BigInteger baseFeePerGas;
        private String extraData;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static BlockInfo fromEntity(Block block) {
            if (block == null) return null;
            
            return BlockInfo.builder()
                    .id(block.getId())
                    .blockNumber(block.getBlockNumber())
                    .blockHash(block.getBlockHash())
                    .parentHash(block.getParentHash())
                    .network(block.getNetwork())
                    .timestamp(block.getTimestamp())
                    .gasLimit(block.getGasLimit())
                    .gasUsed(block.getGasUsed())
                    .difficulty(block.getDifficulty())
                    .totalDifficulty(block.getTotalDifficulty())
                    .size(block.getSize())
                    .transactionCount(block.getTransactionCount())
                    .baseFeePerGas(block.getBaseFeePerGas())
                    .extraData(block.getExtraData())
                    .createdAt(block.getCreatedAt())
                    .updatedAt(block.getUpdatedAt())
                    .build();
        }
    }
}
