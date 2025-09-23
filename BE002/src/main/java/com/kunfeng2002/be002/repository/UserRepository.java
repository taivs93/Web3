package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.User;
import com.kunfeng2002.be002.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(nativeQuery = true, value = "SELECT wallet_id FROM users WHERE telegram_user_id = :telegramId")
    Optional<Long> findWalletIdByTelegramId(@Param("telegramId") Long telegramId);

    Optional<User> findByWallet(Wallet wallet);

    Optional<User> findByWalletAddress(String address);

    @Query("SELECT u FROM User u JOIN u.wallet w WHERE w.address = :address")
    Optional<User> findByWalletAddressQuery(@Param("address") String address);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByTelegramUserId(Long telegramUserId);

    Optional<User> findByTelegramLinkingCode(String telegramLinkingCode);
}