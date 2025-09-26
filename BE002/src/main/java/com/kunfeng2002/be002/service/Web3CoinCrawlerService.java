package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.entity.Coin;
import com.kunfeng2002.be002.entity.Pair;
import com.kunfeng2002.be002.entity.Token;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Web3CoinCrawlerService {
    
    List<Coin> getAllCoins(int page, int size);
    
    Optional<Coin> getCoinById(Long id);
    
    List<Coin> searchCoins(String query, int page, int size);
    
    List<Coin> getNewCoins(int limit);
    
    List<Coin> getTrendingCoins(int limit);
    
    List<Coin> getTopCoins(int limit);
    
    List<Pair> getAllPairs(int page, int size);
    
    Optional<Pair> getPairById(Long id);
    
    List<Pair> searchPairs(String query, int page, int size);
    
    List<Pair> getNewPairs(int limit);
    
    List<Pair> getTrendingPairs(int limit);
    
    List<Pair> getTopPairs(int limit);
    
    List<Token> getAllTokens(int page, int size);
    
    Optional<Token> getTokenById(Long id);
    
    List<Token> searchTokens(String query, int page, int size);
    
    List<Token> getNewTokens(int limit);
    
    List<Token> getTrendingTokens(int limit);
    
    List<Token> getTopTokens(int limit);
    
    List<Coin> crawlNewCoins(int count);
    
    List<Pair> crawlNewPairs(int count);
    
    List<Token> crawlNewTokens(int count);
    
    Map<String, Object> getCrawlStats();
    
    void startCrawling();
    
    void stopCrawling();
    
    boolean isCrawling();
}