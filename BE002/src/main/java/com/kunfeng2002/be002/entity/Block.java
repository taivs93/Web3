package com.kunfeng2002.be002.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "blocks", indexes = {
    @Index(name = "idx_block_number", columnList = "blockNumber"),
    @Index(name = "idx_block_hash", columnList = "blockHash"),
    @Index(name = "idx_network", columnList = "network"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "block_number", nullable = false)
    private BigInteger blockNumber;

    @Column(name = "block_hash", nullable = false, unique = true, length = 66)
    private String blockHash;

    @Column(name = "parent_hash", length = 66)
    private String parentHash;

    @Column(name = "network", nullable = false, length = 20)
    private String network;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "gas_limit")
    private BigInteger gasLimit;

    @Column(name = "gas_used")
    private BigInteger gasUsed;

    @Column(name = "difficulty")
    private BigInteger difficulty;

    @Column(name = "total_difficulty")
    private BigInteger totalDifficulty;

    @Column(name = "size")
    private Long size;

    @Column(name = "transaction_count")
    private Integer transactionCount;

    @Column(name = "base_fee_per_gas")
    private BigInteger baseFeePerGas;

    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;
}
