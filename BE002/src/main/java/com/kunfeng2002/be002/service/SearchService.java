package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.entity.NoLombokToken;
import com.kunfeng2002.be002.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final TokenRepository tokenRepository;

    public List<NoLombokToken> searchToken(String query, String network, int page, int size) {
        try {
        if (!StringUtils.hasText(query)) {
                return List.of();
        }

        query = query.trim();
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            if (query.startsWith("0x") && query.length() == 42) {
                return tokenRepository.findByAddressAndNetwork(query, network)
                        .map(List::of)
                        .orElse(List.of());
            }

            return tokenRepository.findByNameContainingIgnoreCaseAndNetwork(query, network, pageable);
        } catch (Exception e) {
            log.error("Error searching tokens", e);
            return List.of();
        }
    }

    public List<NoLombokToken> getTopTokens(String network, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
            return tokenRepository.findByNetworkAndIsActiveTrueOrderByCreatedAtDesc(network, pageable);
        } catch (Exception e) {
            log.error("Error getting top tokens", e);
            return List.of();
        }
    }
}