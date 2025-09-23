package com.kunfeng2002.be002.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_tx_hash", columnList = "transactionHash"),
    @Index(name = "idx_from_address", columnList = "fromAddress"),
    @Index(name = "idx_to_address", columnList = "toAddress"),
    @Index(name = "idx_block_number", columnList = "blockNumber"),
    @Index(name = "idx_network", columnList = "network"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_hash", nullable = false, unique = true, length = 66)
    private String transactionHash;

    @Column(name = "block_number", nullable = false)
    private BigInteger blockNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    private Block block;

    @Column(name = "from_address", length = 42)
    private String fromAddress;

    @Column(name = "to_address", length = 42)
    private String toAddress;

    @Column(name = "value", precision = 36, scale = 18)
    private BigInteger value;

    @Column(name = "gas", nullable = false)
    private BigInteger gas;

    @Column(name = "gas_price", precision = 36, scale = 18)
    private BigInteger gasPrice;

    @Column(name = "gas_used")
    private BigInteger gasUsed;

    @Column(name = "nonce")
    private BigInteger nonce;

    @Column(name = "network", nullable = false, length = 20)
    private String network;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "status", length = 20)
    private String status; // SUCCESS, FAILED, PENDING

    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;

    @Column(name = "transaction_index")
    private Integer transactionIndex;

    @Column(name = "is_contract_creation")
    private Boolean isContractCreation = false;

    @Column(name = "contract_address", length = 42)
    private String contractAddress;

    @Column(name = "logs_count")
    private Integer logsCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
