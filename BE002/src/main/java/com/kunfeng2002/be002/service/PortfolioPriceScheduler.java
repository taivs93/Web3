package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.entity.PortfolioToken;
import com.kunfeng2002.be002.repository.PortfolioTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioPriceScheduler {

    private final PortfolioTokenRepository portfolioTokenRepository;
    private final TokenPriceService tokenPriceService;
    private final WebSocketNotifyService webSocketNotifyService;

    // Update every 5 minutes instead of 30 seconds to avoid rate limiting
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void updatePortfolioPrices() {
        try {
            log.info("Starting portfolio price update scheduler");
            
            List<PortfolioToken> allHoldings = portfolioTokenRepository.findAll();
            if (allHoldings.isEmpty()) {
                log.debug("No portfolio tokens found to update");
                return;
            }

            // Group by symbol to batch API calls
            Map<String, List<PortfolioToken>> holdingsBySymbol = allHoldings.stream()
                    .collect(Collectors.groupingBy(holding -> holding.getToken().getSymbol()));

            // Get unique symbols for batch price fetch
            String[] symbols = holdingsBySymbol.keySet().toArray(new String[0]);
            Map<String, BigDecimal> prices = tokenPriceService.getTokenPrices(symbols);

            int updatedCount = 0;
            Set<Long> notifiedUsers = new HashSet<>();

            for (Map.Entry<String, List<PortfolioToken>> entry : holdingsBySymbol.entrySet()) {
                String symbol = entry.getKey();
                List<PortfolioToken> holdings = entry.getValue();
                BigDecimal currentPrice = prices.get(symbol);

                if (currentPrice != null && currentPrice.compareTo(BigDecimal.ZERO) > 0) {
                    for (PortfolioToken holding : holdings) {
                        try {
                            BigDecimal oldPrice = holding.getCurrentPrice();
                            boolean priceChanged = oldPrice == null || 
                                oldPrice.subtract(currentPrice).abs()
                                    .divide(oldPrice, 4, RoundingMode.HALF_UP)
                                    .compareTo(new BigDecimal("0.01")) > 0; // 1% change threshold

                            updateHoldingPrices(holding, currentPrice);
                            portfolioTokenRepository.save(holding);
                            updatedCount++;

                            // Only notify if price changed significantly and user not already notified
                            if (priceChanged && !notifiedUsers.contains(holding.getPortfolio().getUserId())) {
                                notifyPriceUpdate(holding, currentPrice, oldPrice);
                                notifiedUsers.add(holding.getPortfolio().getUserId());
                            }

                        } catch (Exception e) {
                            log.warn("Error updating price for token {} in portfolio {}: {}", 
                                symbol, holding.getPortfolio().getId(), e.getMessage());
                        }
                    }
                } else {
                    log.debug("No valid price found for symbol: {}", symbol);
                }
            }

            log.info("Portfolio price update completed. Updated {} holdings for {} users", 
                updatedCount, notifiedUsers.size());

        } catch (Exception e) {
            log.error("Error in portfolio price update scheduler", e);
        }
    }

    private void updateHoldingPrices(PortfolioToken holding, BigDecimal currentPrice) {
        holding.setCurrentPrice(currentPrice);
        holding.setTotalValue(holding.getAmount().multiply(currentPrice));

        if (holding.getAverageBuyPrice() != null && holding.getAverageBuyPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal costBasis = holding.getAmount().multiply(holding.getAverageBuyPrice());
            BigDecimal pnlAmount = holding.getTotalValue().subtract(costBasis);
            holding.setPnlAmount(pnlAmount);

            BigDecimal pnlPercentage = pnlAmount.divide(costBasis, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            holding.setPnlPercentage(pnlPercentage);
        }
    }

    private void notifyPriceUpdate(PortfolioToken holding, BigDecimal newPrice, BigDecimal oldPrice) {
        try {
            String changeDirection = oldPrice != null && newPrice.compareTo(oldPrice) > 0 ? "📈" : "📉";
            String message = String.format("%s %s: $%s", 
                changeDirection,
                holding.getToken().getSymbol(), 
                newPrice.setScale(6, RoundingMode.HALF_UP));

            webSocketNotifyService.notifyUser(holding.getPortfolio().getUserId(), message);
        } catch (Exception e) {
            log.warn("Failed to send price update notification: {}", e.getMessage());
        }
    }

    // Manual trigger for immediate update (useful for testing)
    public void triggerManualUpdate() {
        log.info("Manual portfolio price update triggered");
        updatePortfolioPrices();
    }
}
