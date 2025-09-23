package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    Optional<Wallet> findByAddress(String address);
    
    boolean existsByAddress(String address);
}
