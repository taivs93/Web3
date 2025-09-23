package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.Follow;
import com.kunfeng2002.be002.entity.Chat;
import com.kunfeng2002.be002.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    @Query("""
    SELECT f FROM Follow f
    JOIN FETCH f.chat c
    JOIN FETCH f.wallet w
    WHERE w.address IN :addresses
""")
    List<Follow> findByWalletAddresses(@Param("addresses") List<String> addresses);

    boolean existsByChatAndWallet(Chat chat, Wallet wallet);

    @Modifying
    void deleteByChatAndWallet(Chat chat, Wallet wallet);

    @Query("SELECT DISTINCT f.wallet.address FROM Follow f")
    List<String> findAllWalletAddresses();

    @Query("SELECT f.wallet.address FROM Follow f WHERE f.chat.chatId = :chatId")
    List<String> findWalletAddressesByChatId(@Param("chatId") Long chatId);
}
