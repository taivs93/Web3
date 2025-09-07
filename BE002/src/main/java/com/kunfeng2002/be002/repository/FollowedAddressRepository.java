package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.FollowedAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowedAddressRepository extends JpaRepository<FollowedAddress, Long> {

    boolean existsByAddressAndNetwork(String address, String network);

    List<FollowedAddress> findByNetwork(String network);

    List<FollowedAddress> findByAddress(String address);

    Optional<FollowedAddress> findByAddressAndNetwork(String address, String network);

    void deleteByAddressAndNetwork(String address, String network);

    @Query("SELECT DISTINCT f.network FROM FollowedAddress f")
    List<String> findDistinctNetworks();

    @Query("SELECT COUNT(f) FROM FollowedAddress f WHERE f.network = :network")
    Long countByNetwork(@Param("network") String network);

    @Query("SELECT f.network, COUNT(f) FROM FollowedAddress f GROUP BY f.network")
    List<Object[]> countByAllNetworks();

    @Query("SELECT f FROM FollowedAddress f WHERE f.address LIKE %:addressPart%")
    List<FollowedAddress> findByAddressContaining(@Param("addressPart") String addressPart);
}