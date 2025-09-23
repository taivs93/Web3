package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.AddressActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AddressActivityRepository extends JpaRepository<AddressActivity, Long> {

    List<AddressActivity> findByAddressAndNetworkOrderByTimestampDesc(String address, String network, Pageable pageable);

    List<AddressActivity> findByAddressAndNetworkAndActivityTypeOrderByTimestampDesc(String address, String network, String activityType, Pageable pageable);

    @Query("SELECT a FROM AddressActivity a WHERE a.address = :address AND a.network = :network AND a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
    List<AddressActivity> findByAddressAndNetworkAndTimestampRange(@Param("address") String address, 
                                                                 @Param("network") String network, 
                                                                 @Param("startTime") LocalDateTime startTime, 
                                                                 @Param("endTime") LocalDateTime endTime, 
                                                                 Pageable pageable);

    @Query("SELECT a FROM AddressActivity a WHERE a.address = :address AND a.network = :network AND a.value >= :minValue ORDER BY a.value DESC")
    List<AddressActivity> findHighValueActivities(@Param("address") String address, 
                                                @Param("network") String network, 
                                                @Param("minValue") BigInteger minValue, 
                                                Pageable pageable);

    @Query("SELECT a FROM AddressActivity a WHERE a.address = :address AND a.network = :network AND a.tokenAddress = :tokenAddress ORDER BY a.timestamp DESC")
    List<AddressActivity> findByAddressAndToken(@Param("address") String address, 
                                              @Param("network") String network, 
                                              @Param("tokenAddress") String tokenAddress, 
                                              Pageable pageable);

    @Query("SELECT a FROM AddressActivity a WHERE a.address = :address AND a.network = :network AND a.isContract = true ORDER BY a.timestamp DESC")
    List<AddressActivity> findContractInteractions(@Param("address") String address, 
                                                 @Param("network") String network, 
                                                 Pageable pageable);

    @Query("SELECT COUNT(a) FROM AddressActivity a WHERE a.address = :address AND a.network = :network")
    Long countByAddressAndNetwork(@Param("address") String address, @Param("network") String network);

    @Query("SELECT COUNT(a) FROM AddressActivity a WHERE a.address = :address AND a.network = :network AND a.activityType = :activityType")
    Long countByAddressAndActivityType(@Param("address") String address, 
                                     @Param("network") String network, 
                                     @Param("activityType") String activityType);

    @Query("SELECT SUM(a.value) FROM AddressActivity a WHERE a.address = :address AND a.network = :network AND a.activityType = 'SEND'")
    BigInteger sumSentValue(@Param("address") String address, @Param("network") String network);

    @Query("SELECT SUM(a.value) FROM AddressActivity a WHERE a.address = :address AND a.network = :network AND a.activityType = 'RECEIVE'")
    BigInteger sumReceivedValue(@Param("address") String address, @Param("network") String network);

    @Query("SELECT a FROM AddressActivity a WHERE a.network = :network AND (a.address LIKE %:query% OR a.transactionHash LIKE %:query% OR a.contractName LIKE %:query%)")
    Page<AddressActivity> searchActivities(@Param("network") String network, @Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT a.address FROM AddressActivity a WHERE a.network = :network AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<String> findActiveAddresses(@Param("network") String network, 
                                   @Param("since") LocalDateTime since, 
                                   Pageable pageable);
}
