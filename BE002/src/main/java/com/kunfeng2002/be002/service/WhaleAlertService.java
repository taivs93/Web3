package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.response.WhaleAlertResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhaleAlertService {

    private final RestTemplate restTemplate;
    private static final String WHALE_ALERT_URL = "https://whale-alert.io/data.json?alerts=";

    public List<WhaleAlertResponse> getWhaleAlerts() {
        return getWhaleAlerts(3);
    }

    public List<WhaleAlertResponse> getWhaleAlerts(int count) {
        try {
            log.info("Fetching {} whale alerts from API", count);
            
            String url = WHALE_ALERT_URL + count;
            String response = restTemplate.getForObject(url, String.class);
            
            if (response == null || response.trim().isEmpty()) {
                log.warn("Empty response from Whale Alert API");
                return new ArrayList<>();
            }

            List<WhaleAlertResponse> alerts = parseWhaleAlertData(response);

            alerts.sort((a, b) -> {
                try {
                    long timestampA = Long.parseLong(a.getTimestamp());
                    long timestampB = Long.parseLong(b.getTimestamp());
                    return Long.compare(timestampB, timestampA); // Descending order
                } catch (NumberFormatException e) {
                    return 0;
                }
            });

            if (alerts.size() > count) {
                return alerts.subList(0, count);
            }
            
            return alerts;
            
        } catch (Exception e) {
            log.error("Error fetching whale alerts: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private List<WhaleAlertResponse> parseWhaleAlertData(String jsonResponse) {
        List<WhaleAlertResponse> alerts = new ArrayList<>();
        
        try {
            String alertsData = jsonResponse.substring(jsonResponse.indexOf("\"alerts\":[") + 10);
            alertsData = alertsData.substring(0, alertsData.lastIndexOf("]"));

            String[] alertEntries = alertsData.split("\",\"");
            
            for (String entry : alertEntries) {
                if (entry.trim().isEmpty()) continue;

                String cleanEntry = entry.replaceAll("^\"|\"$", "");

                String[] parts = parseCSVLine(cleanEntry);
                
                if (parts.length >= 6) {
                    WhaleAlertResponse alert = new WhaleAlertResponse();
                    alert.setTimestamp(parts[0]);
                    alert.setEmoji(parts[1]);
                    alert.setAmount(cleanString(parts[2]));
                    alert.setValue(cleanString(parts[3]));
                    alert.setDescription(cleanString(parts[4]));

                    extractAdditionalInfo(alert, parts[5]);
                    
                    alerts.add(alert);
                }
            }
            
        } catch (Exception e) {
            log.error("Error parsing whale alert data: {}", e.getMessage(), e);
        }
        
        return alerts;
    }
    
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }
    
    private String cleanString(String input) {
        if (input == null) return null;
        return input.replaceAll("\\\\", "").trim();
    }
    
    private void extractAdditionalInfo(WhaleAlertResponse alert, String url) {
        try {
            String timestamp = alert.getTimestamp();
            if (timestamp != null && !timestamp.isEmpty()) {
                try {
                    long timestampLong = Long.parseLong(timestamp);
                    Instant instant = Instant.ofEpochSecond(timestampLong);
                    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    alert.setTime(dateTime.format(formatter));
                } catch (NumberFormatException e) {
                    log.warn("Invalid timestamp format: {}", timestamp);
                    alert.setTime("N/A");
                }
            }

            String amount = alert.getAmount();
            if (amount != null && amount.contains("#")) {
                String symbol = amount.substring(amount.lastIndexOf("#") + 1);
                alert.setSymbol(symbol);
            }

            String description = alert.getDescription();
            if (description != null) {
                if (description.contains("transferred from")) {
                    String[] parts = description.split(" transferred from ");
                    if (parts.length >= 2) {
                        String[] fromToParts = parts[1].split(" to ");
                        if (fromToParts.length >= 2) {
                            alert.setFrom(fromToParts[0].trim());
                            alert.setTo(fromToParts[1].trim());
                        }
                    }
                } else if (description.contains("minted at")) {
                    String[] parts = description.split(" minted at ");
                    if (parts.length >= 2) {
                        alert.setFrom("Mint");
                        alert.setTo(parts[1].trim());
                    }
                }
            }

            if (url != null && url.contains("whale-alert.io/transaction/")) {
                String[] urlParts = url.split("/transaction/");
                if (urlParts.length >= 2) {
                    String[] blockchainHash = urlParts[1].split("/");
                    if (blockchainHash.length >= 2) {
                        alert.setBlockchain(blockchainHash[0]);
                        alert.setHash(blockchainHash[1]);
                    } else {
                        alert.setBlockchain("unknown");
                        alert.setHash("N/A");
                    }
                } else {
                    alert.setBlockchain("unknown");
                    alert.setHash("N/A");
                }
            } else {
                alert.setBlockchain("unknown");
                alert.setHash("N/A");
            }
            
        } catch (Exception e) {
            log.error("Error extracting additional info: {}", e.getMessage(), e);
        }
    }
}
