package com.kunfeng2002.be002.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BSCScanService {

    private final RestTemplate restTemplate;

    @Value("${bscscan.api-key}")
    private String bscscanApiKey;

    @Value("${bscscan.base-url}")
    private String bscscanBaseUrl;

    public String getContractAddress(String symbol, String name) {
        try {
            // Tìm kiếm token trên BSCScan
            String searchQuery = symbol + " " + name;
            String url = bscscanBaseUrl + "?module=token&action=tokenlist&address=0x0000000000000000000000000000000000000000&apikey=" + bscscanApiKey;
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "1".equals(response.get("status"))) {
                // Tìm token phù hợp trong danh sách
                return findMatchingToken(response, symbol, name);
            }
            
            // Fallback: tìm kiếm bằng symbol
            return searchTokenBySymbol(symbol);
            
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm contract address từ BSCScan: {}", e.getMessage(), e);
            return null;
        }
    }

    private String findMatchingToken(Map<String, Object> response, String symbol, String name) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) response.get("result");
            
            if (result != null) {
                for (Map<String, Object> token : result) {
                    String tokenSymbol = (String) token.get("symbol");
                    String tokenName = (String) token.get("name");
                    String contractAddress = (String) token.get("contractAddress");
                    
                    if (tokenSymbol != null && tokenSymbol.equalsIgnoreCase(symbol)) {
                        log.info("Found token by symbol: {} -> {}", symbol, contractAddress);
                        return contractAddress;
                    }
                    
                    if (tokenName != null && tokenName.toLowerCase().contains(name.toLowerCase())) {
                        log.info("Found token by name: {} -> {}", name, contractAddress);
                        return contractAddress;
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi tìm token phù hợp: {}", e.getMessage());
            return null;
        }
    }

    private String searchTokenBySymbol(String symbol) {
        try {
            // Use CoinGecko as primary source for BSC tokens
            String url = "https://api.coingecko.com/api/v3/coins/list?include_platform=true";
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> coins = restTemplate.getForObject(url, List.class);
            
            if (coins != null) {
                for (Map<String, Object> coin : coins) {
                    String coinSymbol = (String) coin.get("symbol");
                    if (coinSymbol != null && coinSymbol.equalsIgnoreCase(symbol)) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> platforms = (Map<String, Object>) coin.get("platforms");
                        if (platforms != null) {
                            String bscAddress = (String) platforms.get("binance-smart-chain");
                            if (bscAddress != null && !bscAddress.isEmpty()) {
                                log.info("Found BSC token address for {}: {}", symbol, bscAddress);
                                return bscAddress;
                            }
                        }
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm token bằng symbol: {}", e.getMessage());
            return null;
        }
    }

    private String findTokenBySymbol(Map<String, Object> response, String symbol) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) response.get("result");
            
            if (result != null) {
                for (Map<String, Object> token : result) {
                    String tokenSymbol = (String) token.get("symbol");
                    if (tokenSymbol != null && tokenSymbol.equalsIgnoreCase(symbol)) {
                        return (String) token.get("contractAddress");
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi tìm token theo symbol: {}", e.getMessage());
            return null;
        }
    }

    public String getTokenInfo(String contractAddress) {
        try {
            String url = bscscanBaseUrl + "?module=token&action=tokeninfo&contractaddress=" + contractAddress + "&apikey=" + bscscanApiKey;
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "1".equals(response.get("status"))) {
                return response.toString();
            }
            
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin token từ BSCScan: {}", e.getMessage(), e);
            return null;
        }
    }

    public String getTokenSymbol(String contractAddress) {
        try {
            // Sử dụng CoinGecko API để tìm token theo contract address
            String url = "https://api.coingecko.com/api/v3/coins/binance-smart-chain/contract/" + contractAddress;
            
            log.info("Gọi CoinGecko API để lấy symbol: {}", url);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            log.info("CoinGecko response: {}", response);
            
            if (response != null && response.get("symbol") != null) {
                String symbol = (String) response.get("symbol");
                log.info("Tìm thấy symbol: {}", symbol);
                return symbol.toUpperCase();
            } else {
                log.warn("CoinGecko API không tìm thấy token: {}", response);
            }
            
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi lấy symbol token từ CoinGecko: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private String decodeHexToString(String hex) {
        try {
            if (hex.startsWith("0x")) {
                hex = hex.substring(2);
            }
            
            // Remove padding zeros
            hex = hex.replaceAll("^0+", "");
            if (hex.length() % 2 != 0) {
                hex = "0" + hex;
            }
            
            if (hex.length() == 0) {
                return "";
            }
            
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < hex.length(); i += 2) {
                String str = hex.substring(i, i + 2);
                int charCode = Integer.parseInt(str, 16);
                if (charCode > 0) {
                    result.append((char) charCode);
                }
            }
            
            return result.toString().trim();
        } catch (Exception e) {
            log.error("Lỗi khi decode hex: {}", e.getMessage());
            return "";
        }
    }

    public String getTokenName(String contractAddress) {
        try {
            // Sử dụng CoinGecko API để tìm token theo contract address
            String url = "https://api.coingecko.com/api/v3/coins/binance-smart-chain/contract/" + contractAddress;
            
            log.info("Gọi CoinGecko API để lấy tên token: {}", url);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            log.info("CoinGecko response: {}", response);
            
            if (response != null && response.get("name") != null) {
                String name = (String) response.get("name");
                log.info("Tìm thấy tên token: {}", name);
                return name;
            } else {
                log.warn("CoinGecko API không tìm thấy token: {}", response);
            }
            
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi lấy tên token từ CoinGecko: {}", e.getMessage(), e);
            return null;
        }
    }

    public Integer getTokenDecimals(String contractAddress) {
        try {
            // Sử dụng CoinGecko API để tìm token theo contract address
            String url = "https://api.coingecko.com/api/v3/coins/binance-smart-chain/contract/" + contractAddress;
            
            log.info("Gọi CoinGecko API để lấy decimals: {}", url);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            log.info("CoinGecko response: {}", response);
            
            if (response != null && response.get("detail_platforms") != null) {
                Map<String, Object> platforms = (Map<String, Object>) response.get("detail_platforms");
                Map<String, Object> bsc = (Map<String, Object>) platforms.get("binance-smart-chain");
                if (bsc != null && bsc.get("decimal_place") != null) {
                    Integer decimals = (Integer) bsc.get("decimal_place");
                    log.info("Tìm thấy decimals: {}", decimals);
                    return decimals;
                }
            } else {
                log.warn("CoinGecko API không tìm thấy token: {}", response);
            }
            
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi lấy decimals token từ CoinGecko: {}", e.getMessage(), e);
            return null;
        }
    }

    public String searchTokenByName(String name) {
        try {
            // BSCScan không có API search trực tiếp, sử dụng tokenlist
            String url = bscscanBaseUrl + "?module=token&action=tokenlist&address=0x0000000000000000000000000000000000000000&apikey=" + bscscanApiKey;
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "1".equals(response.get("status"))) {
                // Tìm token có tên phù hợp
                return findTokenByName(response, name);
            }
            
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm token theo tên: {}", e.getMessage(), e);
            return null;
        }
    }

    private String findTokenByName(Map<String, Object> response, String name) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) response.get("result");
            
            if (result != null) {
                for (Map<String, Object> token : result) {
                    String tokenName = (String) token.get("name");
                    if (tokenName != null && tokenName.toLowerCase().contains(name.toLowerCase())) {
                        String contractAddress = (String) token.get("contractAddress");
                        log.info("Found token by name search: {} -> {}", name, contractAddress);
                        return contractAddress;
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi tìm token theo tên: {}", e.getMessage());
            return null;
        }
    }
}
