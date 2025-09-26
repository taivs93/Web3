package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.NoLombokToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoLombokTokenRepository extends JpaRepository<NoLombokToken, Long> {
    
    Optional<NoLombokToken> findByAddress(String address);
    
    List<NoLombokToken> findByNetworkAndIsActiveTrueOrderByCreatedAtDesc(String network, Pageable pageable);
    
    List<NoLombokToken> findByNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(String name, String symbol);
    
    List<NoLombokToken> findByIsVerifiedTrueOrderByMarketCapUsdDesc(Pageable pageable);
    
    List<NoLombokToken> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    long countByNetwork(String network);
    
    long countByNetworkAndIsVerifiedTrue(String network);
    
    @Query("SELECT t FROM NoLombokToken t WHERE t.isActive = true ORDER BY t.createdAt DESC")
    List<NoLombokToken> findNewTokens(Pageable pageable);
    
    @Query("SELECT t FROM NoLombokToken t WHERE t.isVerified = true ORDER BY t.marketCapUsd DESC NULLS LAST")
    List<NoLombokToken> findTopTokensByMarketCap(Pageable pageable);
}
