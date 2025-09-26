package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.NoLombokToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<NoLombokToken, Long> {

    Optional<NoLombokToken> findByAddressAndNetwork(String address, String network);
    
    // Method này đã được đổi tên để khớp với property 'address' trong entity
    // Optional<NoLombokToken> findByTokenAddressAndNetwork(String address, String network);

    List<NoLombokToken> findByNetworkAndIsActiveTrueOrderByMarketCapUsdDesc(String network, Pageable pageable);
    
    // Method này đã được đổi tên để khớp với property 'marketCapUsd' trong entity
    // List<NoLombokToken> findByNetworkAndIsActiveTrueOrderByMarketCapDesc(String network, Pageable pageable);

    List<NoLombokToken> findBySymbolIgnoreCaseAndNetwork(String symbol, String network);

    List<NoLombokToken> findByNameContainingIgnoreCaseAndNetwork(String name, String network, Pageable pageable);

    List<NoLombokToken> findBySymbolContainingIgnoreCaseAndNetwork(String symbol, String network, Pageable pageable);

    @Query("SELECT t FROM NoLombokToken t WHERE t.network = :network AND t.isVerified = true ORDER BY t.marketCapUsd DESC NULLS LAST")
    List<NoLombokToken> findVerifiedTokensByNetwork(@Param("network") String network, Pageable pageable);

    @Query("SELECT t FROM NoLombokToken t WHERE t.network = :network AND (t.name LIKE %:query% OR t.symbol LIKE %:query% OR t.address LIKE %:query%)")
    Page<NoLombokToken> searchTokens(@Param("network") String network, @Param("query") String query, Pageable pageable);

    @Query("SELECT t FROM NoLombokToken t WHERE t.network = :network AND t.marketCapUsd >= :minMarketCap ORDER BY t.marketCapUsd DESC")
    List<NoLombokToken> findByMarketCapGreaterThan(@Param("network") String network, 
                                         @Param("minMarketCap") java.math.BigDecimal minMarketCap, 
                                         Pageable pageable);

    @Query("SELECT t FROM NoLombokToken t WHERE t.network = :network AND t.priceChange24h >= :minChange ORDER BY t.priceChange24h DESC")
    List<NoLombokToken> findTopGainers(@Param("network") String network, 
                              @Param("minChange") java.math.BigDecimal minChange, 
                              Pageable pageable);

    @Query("SELECT t FROM NoLombokToken t WHERE t.network = :network AND t.priceChange24h <= :maxChange ORDER BY t.priceChange24h ASC")
    List<NoLombokToken> findTopLosers(@Param("network") String network, 
                             @Param("maxChange") java.math.BigDecimal maxChange, 
                             Pageable pageable);

    @Query("SELECT COUNT(t) FROM NoLombokToken t WHERE t.network = :network AND t.isActive = true")
    Long countActiveTokensByNetwork(@Param("network") String network);

    @Query("SELECT t FROM NoLombokToken t WHERE t.network = :network AND t.lastPriceUpdate < :cutoffTime")
    List<NoLombokToken> findTokensNeedingPriceUpdate(@Param("network") String network, 
                                           @Param("cutoffTime") java.time.LocalDateTime cutoffTime);

    List<NoLombokToken> findByNetworkAndIsActiveTrueOrderByCreatedAtDesc(String network, Pageable pageable);
    
    Long countByNetwork(String network);
    
    Long countByNetworkAndIsVerifiedTrue(String network);
}
