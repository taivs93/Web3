package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    
    Optional<Token> findBySymbolAndIsActiveTrue(String symbol);
    
    List<Token> findByIsActiveTrue();
    
    List<Token> findByNetworkAndIsActiveTrue(String network);
    
    boolean existsBySymbolAndIsActiveTrue(String symbol);
}
