package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.BotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BotRepository extends JpaRepository<BotEntity, Long> {
    Optional<BotEntity> findByTelegramId(Long telegramId);
    Optional<BotEntity> findByUserId(Long userId);
}
