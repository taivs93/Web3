package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoinRepository extends JpaRepository<Coin, Long> {

    Optional<Coin> findByAddress(String address);

    List<Coin> findBySymbol(String symbol);

    List<Coin> findByNameContainingIgnoreCase(String name);

    List<Coin> findByIsActiveTrue();

    @Query("SELECT c FROM Coin c WHERE c.symbol = :symbol AND c.isActive = true")
    List<Coin> findActiveCoinsBySymbol(@Param("symbol") String symbol);

    @Query("SELECT c FROM Coin c WHERE c.name LIKE %:name% AND c.isActive = true")
    List<Coin> findActiveCoinsByName(@Param("name") String name);

    @Query("SELECT c FROM Coin c WHERE c.isActive = true ORDER BY c.marketCap DESC NULLS LAST")
    List<Coin> findTopCoinsByMarketCap();

    @Query("SELECT c FROM Coin c WHERE c.isActive = true ORDER BY c.volume24h DESC NULLS LAST")
    List<Coin> findTopCoinsByVolume();

    @Query("SELECT c FROM Coin c WHERE c.isActive = true ORDER BY c.priceChangePercentage24h DESC NULLS LAST")
    List<Coin> findTopGainers();

    @Query("SELECT c FROM Coin c WHERE c.isActive = true ORDER BY c.priceChangePercentage24h ASC NULLS LAST")
    List<Coin> findTopLosers();

    boolean existsByAddress(String address);

    List<Coin> findByAddressStartingWith(String prefix);

    List<Coin> findBySymbolIgnoreCase(String symbol);

    List<Coin> findByNameIgnoreCase(String name);
}
