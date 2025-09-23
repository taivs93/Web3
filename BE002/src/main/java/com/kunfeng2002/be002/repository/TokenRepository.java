package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.Token;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenAddressAndNetwork(String tokenAddress, String network);

    List<Token> findByNetworkAndIsActiveTrueOrderByMarketCapDesc(String network, Pageable pageable);

    List<Token> findBySymbolIgnoreCaseAndNetwork(String symbol, String network);

    List<Token> findByNameContainingIgnoreCaseAndNetwork(String name, String network, Pageable pageable);

    List<Token> findBySymbolContainingIgnoreCaseAndNetwork(String symbol, String network, Pageable pageable);

    @Query("SELECT t FROM Token t WHERE t.network = :network AND t.isVerified = true ORDER BY t.marketCap DESC NULLS LAST")
    List<Token> findVerifiedTokensByNetwork(@Param("network") String network, Pageable pageable);

    @Query("SELECT t FROM Token t WHERE t.network = :network AND (t.name LIKE %:query% OR t.symbol LIKE %:query% OR t.tokenAddress LIKE %:query%)")
    Page<Token> searchTokens(@Param("network") String network, @Param("query") String query, Pageable pageable);

    @Query("SELECT t FROM Token t WHERE t.network = :network AND t.marketCap >= :minMarketCap ORDER BY t.marketCap DESC")
    List<Token> findByMarketCapGreaterThan(@Param("network") String network, 
                                         @Param("minMarketCap") Long minMarketCap, 
                                         Pageable pageable);

    @Query("SELECT t FROM Token t WHERE t.network = :network AND t.priceChange24h >= :minChange ORDER BY t.priceChange24h DESC")
    List<Token> findTopGainers(@Param("network") String network, 
                              @Param("minChange") Double minChange, 
                              Pageable pageable);

    @Query("SELECT t FROM Token t WHERE t.network = :network AND t.priceChange24h <= :maxChange ORDER BY t.priceChange24h ASC")
    List<Token> findTopLosers(@Param("network") String network, 
                             @Param("maxChange") Double maxChange, 
                             Pageable pageable);

    @Query("SELECT COUNT(t) FROM Token t WHERE t.network = :network AND t.isActive = true")
    Long countActiveTokensByNetwork(@Param("network") String network);

    @Query("SELECT t FROM Token t WHERE t.network = :network AND t.lastPriceUpdate < :cutoffTime")
    List<Token> findTokensNeedingPriceUpdate(@Param("network") String network, 
                                           @Param("cutoffTime") java.time.LocalDateTime cutoffTime);
}
