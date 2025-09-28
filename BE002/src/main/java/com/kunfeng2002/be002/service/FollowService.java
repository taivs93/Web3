package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.entity.Chat;
import com.kunfeng2002.be002.entity.Follow;
import com.kunfeng2002.be002.entity.Wallet;
import com.kunfeng2002.be002.exception.DataNotFoundException;
import com.kunfeng2002.be002.repository.ChatRepository;
import com.kunfeng2002.be002.repository.FollowRepository;
import com.kunfeng2002.be002.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowRepository followRepository;
    private final ChatRepository chatRepository;
    private final WalletRepository walletRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String GLOBAL_KEY = "follow:global";
    private static final String CHAT_KEY_PREFIX = "follow:chat:";

    @Transactional
    public void follow(Long chatId, String address) {
        Chat chat = chatRepository.findByChatId(chatId)
                .orElseThrow(() -> new DataNotFoundException("Chat not found"));

        String normalized = address.toLowerCase();
        Wallet wallet = walletRepository.findByAddress(normalized)
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder()
                                .address(normalized)
                                .nonce("INIT")
                                .build()
                ));

        if (followRepository.existsByChatAndWallet(chat, wallet)) {
            return;
        }

        followRepository.save(Follow.builder()
                .chat(chat)
                .wallet(wallet)
                .build());

        clearGlobalCache();
        clearChatCache(chatId);
    }

    @Transactional
    public void unfollow(Long chatId, String address) {
        Chat chat = chatRepository.findByChatId(chatId)
                .orElseThrow(() -> new DataNotFoundException("Chat not found"));

        Wallet wallet = walletRepository.findByAddress(address.toLowerCase())
                .orElseThrow(() -> new DataNotFoundException("Wallet not found"));

        followRepository.deleteByChatAndWallet(chat, wallet);
        clearGlobalCache();
        clearChatCache(chatId);
    }

    public List<String> getFollowedAddressesByChatId(Long chatId) {
        try {
            String cacheKey = CHAT_KEY_PREFIX + chatId;
            @SuppressWarnings("unchecked")
            List<String> cached = (List<String>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }

            List<String> addresses = followRepository.findWalletAddressesByChatId(chatId);
            try {
                redisTemplate.opsForValue().set(cacheKey, addresses, 15, TimeUnit.MINUTES);
            } catch (Exception e) {
            }
            return addresses;
        } catch (Exception e) {
            
            return followRepository.findWalletAddressesByChatId(chatId);
        }
    }

    public List<String> getGloballyFollowedAddresses() {
        try {
            @SuppressWarnings("unchecked")
            List<String> cached = (List<String>) redisTemplate.opsForValue().get(GLOBAL_KEY);
            if (cached != null) {
                return cached;
            }

            List<String> addresses = followRepository.findAllWalletAddresses();
            try {
                redisTemplate.opsForValue().set(GLOBAL_KEY, addresses, 15, TimeUnit.MINUTES);
            } catch (Exception e) {
            }
            return addresses;
        } catch (Exception e) {
            
            return followRepository.findAllWalletAddresses();
        }
    }

    private void clearGlobalCache() {
        try {
            redisTemplate.delete(GLOBAL_KEY);
        } catch (Exception e) {
        }
    }

    private void clearChatCache(Long chatId) {
        try {
            redisTemplate.delete(CHAT_KEY_PREFIX + chatId);
        } catch (Exception e) {
        }
    }
}
