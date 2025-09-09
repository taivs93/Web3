package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.FollowedAddress;
import com.kunfeng2002.be002.entity.BotEntity;
import com.kunfeng2002.be002.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowedAddressRepository extends JpaRepository<FollowedAddress, Long> {

    @Query("""
    SELECT fa FROM FollowedAddress fa
    JOIN FETCH fa.bot b
    JOIN FETCH fa.wallet w
    WHERE w.address IN :addresses
""")
    List<FollowedAddress> findByWalletAddressIn(@Param("addresses") String... addresses);

    boolean existsByBotAndWallet(BotEntity bot, Wallet wallet);
    void deleteByBotAndWallet(BotEntity bot, Wallet wallet);
    List<FollowedAddress> findByBot(BotEntity bot);
    @Query("SELECT DISTINCT fa.wallet.address FROM FollowedAddress fa")
    List<String> findAllWalletAddresses();
}
