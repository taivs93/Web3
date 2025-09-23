package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.Block;
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
public interface BlockRepository extends JpaRepository<Block, Long> {

    Optional<Block> findByBlockHash(String blockHash);

    Optional<Block> findByBlockNumberAndNetwork(BigInteger blockNumber, String network);

    List<Block> findByNetworkOrderByBlockNumberDesc(String network, Pageable pageable);

    @Query("SELECT b FROM Block b WHERE b.network = :network AND b.blockNumber BETWEEN :startBlock AND :endBlock ORDER BY b.blockNumber DESC")
    List<Block> findByNetworkAndBlockNumberRange(@Param("network") String network, 
                                                @Param("startBlock") BigInteger startBlock, 
                                                @Param("endBlock") BigInteger endBlock);

    @Query("SELECT b FROM Block b WHERE b.network = :network AND b.timestamp BETWEEN :startTime AND :endTime ORDER BY b.timestamp DESC")
    List<Block> findByNetworkAndTimestampRange(@Param("network") String network, 
                                             @Param("startTime") LocalDateTime startTime, 
                                             @Param("endTime") LocalDateTime endTime);

    @Query("SELECT b FROM Block b WHERE b.network = :network AND b.blockNumber >= :fromBlock ORDER BY b.blockNumber ASC")
    List<Block> findLatestBlocksFrom(@Param("network") String network, @Param("fromBlock") BigInteger fromBlock, Pageable pageable);

    @Query("SELECT MAX(b.blockNumber) FROM Block b WHERE b.network = :network")
    Optional<BigInteger> findLatestBlockNumber(@Param("network") String network);

    @Query("SELECT COUNT(b) FROM Block b WHERE b.network = :network")
    Long countByNetwork(@Param("network") String network);

    @Query("SELECT b FROM Block b WHERE b.network = :network AND (b.blockNumber = :query OR b.blockHash LIKE %:query%)")
    Page<Block> searchBlocks(@Param("network") String network, @Param("query") String query, Pageable pageable);
}
