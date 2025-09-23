package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionHash(String transactionHash);

    List<Transaction> findByFromAddressAndNetwork(String fromAddress, String network, Pageable pageable);

    List<Transaction> findByToAddressAndNetwork(String toAddress, String network, Pageable pageable);

    List<Transaction> findByFromAddressOrToAddressAndNetwork(String fromAddress, String toAddress, String network, Pageable pageable);

    List<Transaction> findByBlockNumberAndNetwork(BigInteger blockNumber, String network);

    @Query("SELECT t FROM Transaction t WHERE t.network = :network AND t.timestamp BETWEEN :startTime AND :endTime ORDER BY t.timestamp DESC")
    List<Transaction> findByNetworkAndTimestampRange(@Param("network") String network, 
                                                   @Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);

    @Query("SELECT t FROM Transaction t WHERE t.network = :network AND t.value >= :minValue ORDER BY t.value DESC")
    List<Transaction> findHighValueTransactions(@Param("network") String network, 
                                              @Param("minValue") BigInteger minValue, 
                                              Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.network = :network AND t.isContractCreation = true ORDER BY t.timestamp DESC")
    List<Transaction> findContractCreations(@Param("network") String network, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.network = :network AND t.contractAddress = :contractAddress ORDER BY t.timestamp DESC")
    List<Transaction> findByContractAddress(@Param("network") String network, 
                                          @Param("contractAddress") String contractAddress, 
                                          Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.network = :network AND (t.transactionHash LIKE %:query% OR t.fromAddress LIKE %:query% OR t.toAddress LIKE %:query%)")
    Page<Transaction> searchTransactions(@Param("network") String network, @Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.network = :network")
    Long countByNetwork(@Param("network") String network);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.network = :network AND t.fromAddress = :address")
    Long countByFromAddress(@Param("network") String network, @Param("address") String address);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.network = :network AND t.toAddress = :address")
    Long countByToAddress(@Param("network") String network, @Param("address") String address);

    @Query("SELECT SUM(t.value) FROM Transaction t WHERE t.network = :network AND t.fromAddress = :address")
    BigInteger sumValueByFromAddress(@Param("network") String network, @Param("address") String address);

    @Query("SELECT SUM(t.value) FROM Transaction t WHERE t.network = :network AND t.toAddress = :address")
    BigInteger sumValueByToAddress(@Param("network") String network, @Param("address") String address);
}
