package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.config.Web3Config;
import com.kunfeng2002.be002.entity.BotEntity;
import com.kunfeng2002.be002.entity.FollowedAddress;
import com.kunfeng2002.be002.entity.Wallet;
import com.kunfeng2002.be002.exception.DataNotFoundException;
import com.kunfeng2002.be002.repository.BotRepository;
import com.kunfeng2002.be002.repository.FollowedAddressRepository;
import com.kunfeng2002.be002.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowedAddressRepository followedAddressRepository;
    private final BotRepository botRepository;
    private final WalletRepository walletRepository;
    private final Web3Config web3Config;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String GLOBAL_KEY = "globallyFollowedAddresses";
    private static final String BOT_KEY_PREFIX = "followedAddressesByBot:";

    @Transactional
    public void follow(Long telegramId, String address) {
        BotEntity bot = botRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException("Bot not linked"));

        String normalized = address.toLowerCase();
        Wallet wallet = walletRepository.findByAddress(normalized)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .address(normalized)
                        .nonce("INIT")
                        .build()));

        if (followedAddressRepository.existsByBotAndWallet(bot, wallet)) {
            log.info("Bot {} already following {}", telegramId, normalized);
            return;
        }

        followedAddressRepository.save(FollowedAddress.builder()
                .bot(bot)
                .wallet(wallet)
                .build());

        log.info("Bot {} started following {}", telegramId, normalized);

        clearGlobalCache();
        clearBotCache(telegramId);
    }

    @Transactional
    public void unfollow(Long telegramId, String address) {
        BotEntity bot = botRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException("Bot not linked"));

        Wallet wallet = walletRepository.findByAddress(address.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        followedAddressRepository.deleteByBotAndWallet(bot, wallet);
        log.info("Bot {} stopped following {}", telegramId, wallet.getAddress());

        clearGlobalCache();
        clearBotCache(telegramId);
    }

    public Set<String> getFollowedAddresses(Long telegramId) {
        String cacheKey = BOT_KEY_PREFIX + telegramId;
        Set<String> cached = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        BotEntity bot = botRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new DataNotFoundException("Bot not linked"));

        Set<String> addresses = followedAddressRepository.findByBot(bot).stream()
                .map(fa -> fa.getWallet().getAddress())
                .collect(Collectors.toSet());

        redisTemplate.opsForValue().set(cacheKey, addresses, 15, TimeUnit.MINUTES);
        return addresses;
    }

    public List<String> getGloballyFollowedAddresses() {
        List<String> cached = (List<String>) redisTemplate.opsForValue().get(GLOBAL_KEY);
        if (cached != null) {
            return cached;
        }

        List<String> addresses = followedAddressRepository.findAllWalletAddresses();

        redisTemplate.opsForValue().set(GLOBAL_KEY, addresses, 15, TimeUnit.MINUTES);
        return addresses;
    }

    private void clearGlobalCache() {
        redisTemplate.delete(GLOBAL_KEY);
    }

    private void clearBotCache(Long telegramId) {
        redisTemplate.delete(BOT_KEY_PREFIX + telegramId);
    }
}
