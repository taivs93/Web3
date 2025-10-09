package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.RecurringInvestment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecurringInvestmentRepository extends JpaRepository<RecurringInvestment, Long> {
    
    List<RecurringInvestment> findByUserIdAndIsActiveTrue(Long userId);
    
    @Query("SELECT ri FROM RecurringInvestment ri WHERE ri.isActive = true AND ri.nextNotificationDate <= :now")
    List<RecurringInvestment> findDueInvestments(@Param("now") LocalDateTime now);
}
