package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    List<Portfolio> findByUserIdAndIsActiveTrue(Long userId);
    
    Optional<Portfolio> findByIdAndUserIdAndIsActiveTrue(Long id, Long userId);
    
    @Query("SELECT p FROM Portfolio p WHERE p.userId = :userId AND p.isActive = true ORDER BY p.createdAt DESC")
    List<Portfolio> findActivePortfoliosByUserId(@Param("userId") Long userId);
    
    boolean existsByUserIdAndNameAndIsActiveTrue(Long userId, String name);
}
