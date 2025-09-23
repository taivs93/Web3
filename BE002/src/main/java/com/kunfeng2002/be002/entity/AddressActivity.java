package com.kunfeng2002.be002.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "address_activities", indexes = {
    @Index(name = "idx_address", columnList = "address"),
    @Index(name = "idx_network", columnList = "network"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_activity_type", columnList = "activityType")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address", nullable = false, length = 42)
    private String address;

    @Column(name = "network", nullable = false, length = 20)
    private String network;

    @Column(name = "activity_type", nullable = false, length = 20)
    private String activityType; // SEND, RECEIVE, CONTRACT_CREATION, CONTRACT_INTERACTION

    @Column(name = "transaction_hash", length = 66)
    private String transactionHash;

    @Column(name = "block_number", nullable = false)
    private BigInteger blockNumber;

    @Column(name = "value", precision = 36, scale = 18)
    private BigInteger value;

    @Column(name = "token_address", length = 42)
    private String tokenAddress;

    @Column(name = "token_symbol", length = 20)
    private String tokenSymbol;

    @Column(name = "from_address", length = 42)
    private String fromAddress;

    @Column(name = "to_address", length = 42)
    private String toAddress;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "gas_used")
    private BigInteger gasUsed;

    @Column(name = "gas_price", precision = 36, scale = 18)
    private BigInteger gasPrice;

    @Column(name = "is_contract", nullable = false)
    private Boolean isContract = false;

    @Column(name = "contract_name", length = 100)
    private String contractName;

    @Column(name = "method_name", length = 100)
    private String methodName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
