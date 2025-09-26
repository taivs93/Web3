package com.kunfeng2002.web3.coincrawler.service;

import com.kunfeng2002.web3.coincrawler.entity.Coin;
import com.kunfeng2002.web3.coincrawler.entity.Pair;
import com.kunfeng2002.web3.coincrawler.entity.Token;
import com.kunfeng2002.web3.coincrawler.model.CrawlStatistics;
import com.kunfeng2002.web3.coincrawler.model.SearchCriteria;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Web3CoinCrawlerService {
    
    List<Coin> getAllCoins(int page, int size);
    
    Optional<Coin> getCoinById(Long id);
    
    Optional<Coin> getCoinByAddress(String address);
    
    List<Coin> searchCoins(SearchCriteria criteria);
    
    List<Coin> getCoinsByLetter(String letter, int page, int size);
    
    List<Coin> getTopCoinsByVolume(int limit);
    
    List<Coin> getNewCoins(int limit);
    
    List<Coin> getTrendingCoins(int limit);
    
    
    List<Pair> getAllPairs(int page, int size);
    
    Optional<Pair> getPairById(Long id);
    
    Optional<Pair> getPairByAddress(String address);
    
    List<Pair> getPairsByTokenId(Long tokenId, int page, int size);
    
    List<Pair> getTopPairsByVolume(int limit);
    
    List<Pair> getNewPairs(int limit);
    
    List<Pair> getTrendingPairs(int limit);
    
    
    List<Token> getAllTokens(int page, int size);
    
    Optional<Token> getTokenById(Long id);
    
    Optional<Token> getTokenByAddress(String address);
    
    List<Token> searchTokens(SearchCriteria criteria);
    
    List<Token> getTokensByLetter(String letter, int page, int size);
    
    List<Token> getTopTokensByVolume(int limit);
    
    List<Token> getNewTokens(int limit);
    
    List<Token> getTrendingTokens(int limit);
    
    
    CrawlStatistics startCrawling();
    
    void stopCrawling();
    
    boolean isCrawling();
    
    CrawlStatistics getCrawlStatistics();
    
    Map<String, Long> getStatisticsByLetter();
    
    Map<String, Long> getStatisticsByCategory();
    
    
    Optional<Coin> refreshCoinData(Long coinId);
    
    Optional<Pair> refreshPairData(Long pairId);
    
    Optional<Token> refreshTokenData(Long tokenId);
    
    int cleanupOldData();
    
    String exportToCsv(String type, SearchCriteria criteria);
}
