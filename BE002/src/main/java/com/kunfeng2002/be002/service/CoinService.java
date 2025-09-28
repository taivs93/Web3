package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.response.CoinResponse;
import com.kunfeng2002.be002.entity.Coin;
import com.kunfeng2002.be002.repository.CoinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoinService {

    private final CoinRepository coinRepository;
    private final OnlineSearchService onlineSearchService;

    public List<CoinResponse> getAllCoins() {
        return coinRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Optional<CoinResponse> getCoinByAddress(String address) {
        log.info("Tìm kiếm địa chỉ trực tuyến cho: {}", address);
        try {
            CoinResponse onlineResult = onlineSearchService.searchByAddressOnline(address).get();
            if (onlineResult != null) {
                log.info("Tìm thấy địa chỉ trực tuyến: {} - {}", onlineResult.getName(), onlineResult.getSymbol());
                return Optional.of(onlineResult);
            }
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm địa chỉ trực tuyến: {}", e.getMessage(), e);
        }
        
        return Optional.empty();
    }

    public List<CoinResponse> getCoinsBySymbol(String symbol) {
        return coinRepository.findActiveCoinsBySymbol(symbol).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<CoinResponse> searchCoins(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        
        query = query.trim();
        log.info("Tìm kiếm coins trực tuyến với query: {}", query);
        
        try {
            List<CoinResponse> onlineResults = onlineSearchService.searchOnline(query).get();
            if (!onlineResults.isEmpty()) {
                log.info("Tìm thấy {} kết quả trực tuyến cho: {}", onlineResults.size(), query);
                return onlineResults;
            }
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm trực tuyến: {}", e.getMessage(), e);
        }
        
        return List.of();
    }
    



    private CoinResponse mapToResponse(Coin coin) {
        return CoinResponse.builder()
                .id(coin.getId())
                .address(coin.getAddress())
                .name(coin.getName())
                .symbol(coin.getSymbol())
                .deployAt(coin.getDeployAt())
                .decimals(coin.getDecimals())
                .totalSupply(coin.getTotalSupply())
                .currentPrice(coin.getCurrentPrice())
                .marketCap(coin.getMarketCap())
                .volume24h(coin.getVolume24h())
                .priceChange24h(coin.getPriceChange24h())
                .priceChangePercentage24h(coin.getPriceChangePercentage24h())
                .circulatingSupply(coin.getCirculatingSupply())
                .maxSupply(coin.getMaxSupply())
                .logoUrl(coin.getLogoUrl())
                .description(coin.getDescription())
                .website(coin.getWebsite())
                .twitter(coin.getTwitter())
                .telegram(coin.getTelegram())
                .discord(coin.getDiscord())
                .reddit(coin.getReddit())
                .github(coin.getGithub())
                .isActive(coin.getIsActive())
                .lastUpdated(coin.getLastUpdated())
                .createdAt(coin.getCreatedAt())
                .updatedAt(coin.getUpdatedAt())
                .build();
    }
}
