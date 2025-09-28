package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.response.CoinResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class OnlineSearchService {

    private static final Logger log = LoggerFactory.getLogger(OnlineSearchService.class);
    private final RestTemplate restTemplate;

    @Value("${coingecko.api-key}")
    private String coingeckoApiKey;

    @Value("${coingecko.base-url}")
    private String coingeckoBaseUrl;

    @Value("${bscscan.api-key}")
    private String bscscanApiKey;

    @Value("${bscscan.base-url}")
    private String bscscanBaseUrl;

    public List<Map<String, Object>> searchByName(String name) {
        try {
            log.info("Tìm kiếm coin bằng tên: {}", name);
            
            String url = coingeckoBaseUrl + "/search?query=" + name + "&x_cg_demo_api_key=" + coingeckoApiKey;
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.get("coins") != null) {
                List<Map<String, Object>> coins = (List<Map<String, Object>>) response.get("coins");
                log.info("Tìm thấy {} coins từ CoinGecko", coins.size());
                
                return processCoinGeckoResults(coins);
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm coin bằng tên: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> searchBySymbol(String symbol) {
        try {
            log.info("Tìm kiếm coin bằng symbol: {}", symbol);
            
            String url = coingeckoBaseUrl + "/search?query=" + symbol + "&x_cg_demo_api_key=" + coingeckoApiKey;
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.get("coins") != null) {
                List<Map<String, Object>> coins = (List<Map<String, Object>>) response.get("coins");
                log.info("Tìm thấy {} coins từ CoinGecko", coins.size());
                
                List<Map<String, Object>> filteredCoins = new ArrayList<>();
                for (Map<String, Object> coin : coins) {
                    String coinSymbol = (String) coin.get("symbol");
                    if (coinSymbol != null && coinSymbol.equalsIgnoreCase(symbol)) {
                        filteredCoins.add(coin);
                    }
                }
                
                return processCoinGeckoResults(filteredCoins);
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm coin bằng symbol: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> searchByAddress(String address) {
        try {
            log.info("Tìm kiếm coin bằng address: {}", address);
            
            Map<String, Object> bscResult = searchByAddressBSCScan(address);
            if (!bscResult.isEmpty()) {
                log.info("Tìm thấy coin từ BSCScan: {}", bscResult.get("name"));
                return bscResult;
            }
            
            String url = coingeckoBaseUrl + "/coins/binance-smart-chain/contract/" + address + "?x_cg_demo_api_key=" + coingeckoApiKey;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.get("id") != null) {
                log.info("Tìm thấy coin từ CoinGecko: {}", response.get("name"));
                return processCoinGeckoDetail(response);
            }
            
            return new HashMap<>();
            
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm coin bằng address: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    private Map<String, Object> searchByAddressBSCScan(String address) {
        try {
            log.info("Tìm kiếm coin bằng address trên BSCScan: {}", address);
            
            String url = bscscanBaseUrl + "?module=token&action=tokeninfo&contractaddress=" + address + "&apikey=" + bscscanApiKey;
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "1".equals(response.get("status"))) {
                log.info("Tìm thấy token từ BSCScan: {}", response.get("tokenName"));
                return processBSCScanResult(response);
            } else {
                log.info("Không tìm thấy token trên BSCScan cho address: {}", address);
                if (response != null) {
                    log.info("BSCScan response: {}", response);
                }
            }
            
            return new HashMap<>();
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm coin bằng address trên BSCScan: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    private List<Map<String, Object>> processCoinGeckoResults(List<Map<String, Object>> coins) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Map<String, Object> coin : coins) {
            Map<String, Object> result = new HashMap<>();
            result.put("id", coin.get("id"));
            result.put("name", coin.get("name"));
            result.put("symbol", coin.get("symbol"));
            result.put("market_cap_rank", coin.get("market_cap_rank"));
            result.put("thumb", coin.get("thumb"));
            result.put("large", coin.get("large"));
            
            String coinId = (String) coin.get("id");
            if (coinId != null) {
                Map<String, Object> detail = getCoinDetail(coinId);
                if (!detail.isEmpty()) {
                    result.putAll(detail);
                }
            }
            
            results.add(result);
        }
        
        return results;
    }

    private Map<String, Object> processCoinGeckoDetail(Map<String, Object> coin) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("id", coin.get("id"));
        result.put("name", coin.get("name"));
        result.put("symbol", coin.get("symbol"));
        result.put("description", coin.get("description"));
        result.put("website", coin.get("homepage"));
        result.put("logo_url", coin.get("image"));
        
        if (coin.get("market_data") != null) {
            Map<String, Object> marketData = (Map<String, Object>) coin.get("market_data");
            result.put("current_price", marketData.get("current_price"));
            result.put("market_cap", marketData.get("market_cap"));
            result.put("total_volume", marketData.get("total_volume"));
            result.put("price_change_24h", marketData.get("price_change_24h"));
            result.put("price_change_percentage_24h", marketData.get("price_change_percentage_24h"));
            result.put("circulating_supply", marketData.get("circulating_supply"));
            result.put("total_supply", marketData.get("total_supply"));
            result.put("max_supply", marketData.get("max_supply"));
        }
        
        if (coin.get("links") != null) {
            Map<String, Object> links = (Map<String, Object>) coin.get("links");
            result.put("twitter", links.get("twitter_screen_name"));
            result.put("telegram", links.get("telegram_channel_identifier"));
            result.put("reddit", links.get("subreddit_url"));
            result.put("github", links.get("repos_url"));
        }
        
        if (coin.get("platforms") != null) {
            Map<String, Object> platforms = (Map<String, Object>) coin.get("platforms");
            String bscAddress = (String) platforms.get("binance-smart-chain");
            if (bscAddress != null) {
                result.put("address", bscAddress);
            }
        }
        
        return result;
    }

    private Map<String, Object> processBSCScanResult(Map<String, Object> response) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("name", response.get("tokenName"));
        result.put("symbol", response.get("symbol"));
        result.put("address", response.get("contractAddress"));
        result.put("decimals", response.get("divisor"));
        result.put("total_supply", response.get("totalSupply"));
        
        String contractAddress = (String) response.get("contractAddress");
        if (contractAddress != null && !contractAddress.isEmpty()) {
            result.put("address", contractAddress);
        }
        
        log.info("Processed BSCScan result: name={}, symbol={}, address={}", 
                result.get("name"), result.get("symbol"), result.get("address"));
        
        return result;
    }

    private Map<String, Object> getCoinDetail(String coinId) {
        try {
            String url = coingeckoBaseUrl + "/coins/" + coinId + "?x_cg_demo_api_key=" + coingeckoApiKey;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null) {
                return processCoinGeckoDetail(response);
            }
            
            return new HashMap<>();
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin chi tiết coin: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    public List<Map<String, Object>> searchAll(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        if (isValidAddress(query)) {
            log.info("Query là địa chỉ hợp lệ, tìm kiếm trực tiếp theo address: {}", query);
            Map<String, Object> addressResult = searchByAddress(query);
            if (!addressResult.isEmpty()) {
                results.add(addressResult);
            }
            return results;
        }
        
        log.info("Query không phải address, tìm kiếm theo name: {}", query);
        List<Map<String, Object>> nameResults = searchByName(query);
        results.addAll(nameResults);
        
        if (query.length() <= 10 && query.matches("^[A-Z0-9]+$")) {
            log.info("Query có vẻ là symbol, tìm kiếm theo symbol: {}", query);
            List<Map<String, Object>> symbolResults = searchBySymbol(query);
            results.addAll(symbolResults);
        }
        
        return removeDuplicates(results);
    }
    
    private boolean isValidAddress(String query) {
        return query != null && 
               query.startsWith("0x") && 
               query.length() == 42 && 
               query.matches("^0x[a-fA-F0-9]{40}$");
    }

    private List<Map<String, Object>> removeDuplicates(List<Map<String, Object>> results) {
        Set<String> seen = new HashSet<>();
        List<Map<String, Object>> uniqueResults = new ArrayList<>();
        
        for (Map<String, Object> result : results) {
            String key = (String) result.get("id");
            if (key == null) {
                key = (String) result.get("address");
            }
            
            if (key != null && !seen.contains(key)) {
                seen.add(key);
                uniqueResults.add(result);
            }
        }
        
        return uniqueResults;
    }

    public CompletableFuture<CoinResponse> searchByAddressOnline(String address) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Tìm kiếm coin bằng address trực tuyến: {}", address);
                
                Map<String, Object> result = searchByAddress(address);
                if (result.isEmpty()) {
                    return null;
                }
                
                return mapToCoinResponse(result);
            } catch (Exception e) {
                log.error("Lỗi khi tìm kiếm coin bằng address trực tuyến: {}", e.getMessage(), e);
                return null;
            }
        });
    }

    public CompletableFuture<List<CoinResponse>> searchOnline(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Tìm kiếm coin trực tuyến: {}", query);
                
                List<Map<String, Object>> results = searchAll(query);
                if (results.isEmpty()) {
                    return new ArrayList<>();
                }
                
                return results.stream()
                        .map(this::mapToCoinResponse)
                        .filter(Objects::nonNull)
                        .filter(coin -> coin.getAddress() != null && !coin.getAddress().trim().isEmpty())
                        .toList();
            } catch (Exception e) {
                log.error("Lỗi khi tìm kiếm coin trực tuyến: {}", e.getMessage(), e);
                return new ArrayList<>();
            }
        });
    }

    private CoinResponse mapToCoinResponse(Map<String, Object> data) {
        try {
            return CoinResponse.builder()
                    .name(safeGetString(data, "name"))
                    .symbol(safeGetString(data, "symbol"))
                    .address(safeGetString(data, "address"))
                    .decimals(parseInteger(data.get("decimals")))
                    .totalSupply(parseBigDecimal(data.get("total_supply")))
                    .currentPrice(parseBigDecimal(data.get("current_price")))
                    .marketCap(parseBigDecimal(data.get("market_cap")))
                    .volume24h(parseBigDecimal(data.get("total_volume")))
                    .priceChange24h(parseBigDecimal(data.get("price_change_24h")))
                    .priceChangePercentage24h(parseBigDecimal(data.get("price_change_percentage_24h")))
                    .circulatingSupply(parseBigDecimal(data.get("circulating_supply")))
                    .maxSupply(parseBigDecimal(data.get("max_supply")))
                    .logoUrl(safeGetString(data, "logo_url"))
                    .description(safeGetString(data, "description"))
                    .website(safeGetString(data, "website"))
                    .twitter(safeGetString(data, "twitter"))
                    .telegram(safeGetString(data, "telegram"))
                    .reddit(safeGetString(data, "reddit"))
                    .github(safeGetString(data, "github"))
                    .isActive(true)
                    .lastUpdated(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Lỗi khi chuyển đổi Map thành CoinResponse: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private String safeGetString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            if (!map.isEmpty()) {
                Object firstValue = map.values().iterator().next();
                return firstValue != null ? firstValue.toString() : null;
            }
        }
        return value.toString();
    }

    private Integer parseInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
