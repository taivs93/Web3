package com.kunfeng2002.be002.repository;

import com.kunfeng2002.be002.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat,Long> {

    Optional<Chat> findByChatId(Long id);

    Optional<Chat> findByUserId(Long id);
    
    boolean existsByChatId(Long chatId);
}
