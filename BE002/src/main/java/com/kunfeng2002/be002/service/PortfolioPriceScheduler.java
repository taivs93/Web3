package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.entity.PortfolioToken;
import com.kunfeng2002.be002.repository.PortfolioTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioPriceScheduler {

    private final PortfolioTokenRepository portfolioTokenRepository;
    private final TokenPriceService tokenPriceService;
    private final WebSocketNotifyService webSocketNotifyService;

    @Scheduled(fixedRate = 30000)
    public void updatePortfolioPrices() {
        try {
            List<PortfolioToken> allHoldings = portfolioTokenRepository.findAll();
            
            for (PortfolioToken holding : allHoldings) {
                try {
                    BigDecimal currentPrice = tokenPriceService.getTokenPrice(holding.getToken().getSymbol());
                    
                    if (currentPrice.compareTo(BigDecimal.ZERO) > 0) {
                        holding.setCurrentPrice(currentPrice);
                        holding.setTotalValue(holding.getAmount().multiply(currentPrice));
                        
                        if (holding.getAverageBuyPrice() != null && holding.getAverageBuyPrice().compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal pnlAmount = holding.getTotalValue().subtract(
                                holding.getAmount().multiply(holding.getAverageBuyPrice())
                            );
                            holding.setPnlAmount(pnlAmount);
                            
                            BigDecimal pnlPercentage = holding.getAverageBuyPrice().compareTo(BigDecimal.ZERO) > 0 
                                ? pnlAmount.divide(holding.getAmount().multiply(holding.getAverageBuyPrice()), 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                : BigDecimal.ZERO;
                            holding.setPnlPercentage(pnlPercentage);
                        }
                        
                        portfolioTokenRepository.save(holding);
                        
                        webSocketNotifyService.notifyUser(
                            holding.getPortfolio().getUserId(),
                            String.format("Portfolio update: %s price changed to $%s", 
                                holding.getToken().getSymbol(), currentPrice)
                        );
                    }
                } catch (Exception e) {
                    log.warn("Error updating price for token {}: {}", holding.getToken().getSymbol(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Error in portfolio price update scheduler: {}", e.getMessage());
        }
    }
}
