package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.PortfolioToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioTokenRepository extends JpaRepository<PortfolioToken, Long> {
    
    List<PortfolioToken> findByPortfolioId(Long portfolioId);
    
    Optional<PortfolioToken> findByPortfolioIdAndTokenId(Long portfolioId, Long tokenId);
    
    @Query("SELECT pt FROM PortfolioToken pt WHERE pt.portfolio.id = :portfolioId AND pt.portfolio.userId = :userId")
    List<PortfolioToken> findByPortfolioIdAndUserId(@Param("portfolioId") Long portfolioId, @Param("userId") Long userId);
    
    boolean existsByPortfolioIdAndTokenId(Long portfolioId, Long tokenId);
}
