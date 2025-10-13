package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.response.WhaleAlertResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhaleAlertFormatterService {

    public String formatWhaleAlerts(List<WhaleAlertResponse> whaleAlerts) {
        if (whaleAlerts == null || whaleAlerts.isEmpty()) {
            return "Không có dữ liệu whale alert tại thời điểm này. Vui lòng thử lại sau.";
        }

        StringBuilder response = new StringBuilder();
        response.append("Whale Alert - Giao dịch lớn\n\n");
        
        for (int i = 0; i < whaleAlerts.size(); i++) {
            WhaleAlertResponse alert = whaleAlerts.get(i);
            response.append(formatSingleAlert(i + 1, alert));
            response.append("\n");
        }
        
        return response.toString();
    }
    
    private String formatSingleAlert(int index, WhaleAlertResponse alert) {
        StringBuilder alertText = new StringBuilder();
        
        alertText.append(String.format("%d: %s\n", 
            index, 
            alert.getSymbol() != null ? alert.getSymbol() : "N/A"));
        alertText.append(String.format("   Amount: %s\n", 
            alert.getAmount() != null ? alert.getAmount() : "N/A"));
        alertText.append(String.format("   Conversion: %s\n", 
            alert.getValue() != null ? alert.getValue() : "N/A"));
        alertText.append(String.format("   From: %s\n", 
            alert.getFrom() != null ? alert.getFrom() : "N/A"));
        alertText.append(String.format("   To: %s\n", 
            alert.getTo() != null ? alert.getTo() : "N/A"));
        alertText.append(String.format("   Blockchain: %s\n", 
            alert.getBlockchain() != null ? capitalizeFirst(alert.getBlockchain()) : "N/A"));
        alertText.append(String.format("   Hash: %s\n", 
            alert.getHash() != null ? alert.getHash() : "N/A"));
        alertText.append(String.format("   Time: %s\n",
            alert.getTime() != null ? alert.getTime() : "N/A"));
            
        return alertText.toString();
    }
    
    private String capitalizeFirst(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
