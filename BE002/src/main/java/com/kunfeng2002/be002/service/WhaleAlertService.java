package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.response.WhaleAlertResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhaleAlertService {

    private final RestTemplate restTemplate;
    private static final String WHALE_ALERT_URL = "https://whale-alert.io/data.json?alerts=3";

    public List<WhaleAlertResponse> getWhaleAlerts() {
        try {
            log.info("Fetching whale alerts from API");
            
            String response = restTemplate.getForObject(WHALE_ALERT_URL, String.class);
            
            if (response == null || response.trim().isEmpty()) {
                log.warn("Empty response from Whale Alert API");
                return new ArrayList<>();
            }

            return parseWhaleAlertData(response);
            
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

            String[] alertEntries = alertsData.split(",\"");
            
            for (String entry : alertEntries) {
                if (entry.trim().isEmpty()) continue;

                if (!entry.startsWith("\"")) {
                    entry = "\"" + entry;
                }
                if (!entry.endsWith("\"")) {
                    entry = entry + "\"";
                }

                String cleanEntry = entry.replaceAll("^\"|\"$", "");
                String[] parts = cleanEntry.split(",");
                
                if (parts.length >= 6) {
                    WhaleAlertResponse alert = new WhaleAlertResponse();
                    alert.setTimestamp(parts[0]);
                    alert.setEmoji(parts[1]);
                    alert.setAmount(parts[2].replaceAll("\"", ""));
                    alert.setValue(parts[3].replaceAll("\"", ""));
                    alert.setDescription(parts[4].replaceAll("\"", ""));
                    alert.setUrl(parts[5].replaceAll("\"", ""));
                    
                    alerts.add(alert);
                }
            }
            
        } catch (Exception e) {
            log.error("Error parsing whale alert data: {}", e.getMessage(), e);
        }
        
        return alerts;
    }
}
