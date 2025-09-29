package com.kunfeng2002.be002.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token_id", nullable = false)
    private Token token;
    
    @Column(name = "amount", precision = 36, scale = 18)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;
    
    @Column(name = "average_buy_price", precision = 36, scale = 18)
    private BigDecimal averageBuyPrice;
    
    @Column(name = "current_price", precision = 36, scale = 18)
    private BigDecimal currentPrice;
    
    @Column(name = "total_value", precision = 36, scale = 18)
    private BigDecimal totalValue;
    
    @Column(name = "pnl_percentage", precision = 10, scale = 4)
    private BigDecimal pnlPercentage;
    
    @Column(name = "pnl_amount", precision = 36, scale = 18)
    private BigDecimal pnlAmount;
    
    @Column(name = "added_at")
    private LocalDateTime addedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
