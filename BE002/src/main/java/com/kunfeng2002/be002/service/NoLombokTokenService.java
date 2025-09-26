package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.entity.NoLombokToken;
import com.kunfeng2002.be002.repository.NoLombokTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoLombokTokenService {
    
    @Autowired
    private NoLombokTokenRepository tokenRepository;
    
    public List<NoLombokToken> getAllTokens() {
        return tokenRepository.findAll();
    }
    
    public Optional<NoLombokToken> getTokenById(Long id) {
        return tokenRepository.findById(id);
    }
    
    public Optional<NoLombokToken> getTokenByAddress(String address) {
        return tokenRepository.findByAddress(address);
    }
    
    public List<NoLombokToken> searchTokens(String query) {
        return tokenRepository.findByNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(query, query);
    }
    
    public List<NoLombokToken> getNewTokens(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return tokenRepository.findNewTokens(pageable);
    }
    
    public List<NoLombokToken> getTopTokens(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return tokenRepository.findTopTokensByMarketCap(pageable);
    }
    
    public List<NoLombokToken> getVerifiedTokens(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return tokenRepository.findByIsVerifiedTrueOrderByMarketCapUsdDesc(pageable);
    }
    
    public NoLombokToken saveToken(NoLombokToken token) {
        return tokenRepository.save(token);
    }
    
    public List<NoLombokToken> saveAllTokens(List<NoLombokToken> tokens) {
        return tokenRepository.saveAll(tokens);
    }
    
    public long getTotalTokenCount() {
        return tokenRepository.count();
    }
    
    public long getVerifiedTokenCount() {
        return tokenRepository.countByNetworkAndIsVerifiedTrue("BSC");
    }
    
    public long getNewTokenCount() {
        return tokenRepository.countByNetwork("BSC");
    }
}
