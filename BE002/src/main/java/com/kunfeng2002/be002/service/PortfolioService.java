package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.request.AddTokenRequest;
import com.kunfeng2002.be002.dto.request.PortfolioRequest;
import com.kunfeng2002.be002.dto.response.PortfolioResponse;
import com.kunfeng2002.be002.dto.response.TokenHoldingResponse;
import com.kunfeng2002.be002.entity.Portfolio;
import com.kunfeng2002.be002.entity.PortfolioToken;
import com.kunfeng2002.be002.entity.Token;
import com.kunfeng2002.be002.exception.DataNotFoundException;
import com.kunfeng2002.be002.exception.ResourceAlreadyExistException;
import com.kunfeng2002.be002.repository.PortfolioRepository;
import com.kunfeng2002.be002.repository.PortfolioTokenRepository;
import com.kunfeng2002.be002.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final TokenRepository tokenRepository;
    private final PortfolioTokenRepository portfolioTokenRepository;
    private final TokenPriceService tokenPriceService;

    @Transactional
    public PortfolioResponse createPortfolio(Long userId, PortfolioRequest request) {
        if (portfolioRepository.existsByUserIdAndNameAndIsActiveTrue(userId, request.getName())) {
            throw new ResourceAlreadyExistException("Portfolio with name '" + request.getName() + "' already exists");
        }

        Portfolio portfolio = Portfolio.builder()
                .userId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .isActive(true)
                .build();

        portfolio = portfolioRepository.save(portfolio);
        return buildPortfolioResponse(portfolio);
    }

    public List<PortfolioResponse> getUserPortfolios(Long userId) {
        List<Portfolio> portfolios = portfolioRepository.findActivePortfoliosByUserId(userId);
        return portfolios.stream()
                .map(this::buildPortfolioResponse)
                .collect(Collectors.toList());
    }

    public PortfolioResponse getPortfolio(Long userId, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdAndIsActiveTrue(portfolioId, userId)
                .orElseThrow(() -> new DataNotFoundException("Portfolio not found"));
        return buildPortfolioResponse(portfolio);
    }

    @Transactional
    public PortfolioResponse addTokenToPortfolio(Long userId, AddTokenRequest request) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdAndIsActiveTrue(request.getPortfolioId(), userId)
                .orElseThrow(() -> new DataNotFoundException("Portfolio not found"));

        Token token = tokenRepository.findBySymbolAndIsActiveTrue(request.getTokenSymbol())
                .orElseThrow(() -> new DataNotFoundException("Token not found"));

        PortfolioToken existingHolding = portfolioTokenRepository.findByPortfolioIdAndTokenId(request.getPortfolioId(), token.getId())
                .orElse(null);

        if (existingHolding != null) {
            BigDecimal newTotalAmount = existingHolding.getAmount().add(request.getAmount());
            BigDecimal newTotalValue = existingHolding.getAverageBuyPrice().multiply(existingHolding.getAmount())
                    .add(request.getBuyPrice().multiply(request.getAmount()));
            BigDecimal newAveragePrice = newTotalValue.divide(newTotalAmount, 18, RoundingMode.HALF_UP);
            
            existingHolding.setAmount(newTotalAmount);
            existingHolding.setAverageBuyPrice(newAveragePrice);
            portfolioTokenRepository.save(existingHolding);
        } else {
            PortfolioToken newHolding = PortfolioToken.builder()
                    .portfolio(portfolio)
                    .token(token)
                    .amount(request.getAmount())
                    .averageBuyPrice(request.getBuyPrice())
                    .build();
            portfolioTokenRepository.save(newHolding);
        }

        updatePortfolioTokenPrices(request.getPortfolioId());
        return getPortfolio(userId, request.getPortfolioId());
    }

    @Transactional
    public void updatePortfolioTokenPrices(Long portfolioId) {
        List<PortfolioToken> holdings = portfolioTokenRepository.findByPortfolioId(portfolioId);
        
        for (PortfolioToken holding : holdings) {
            BigDecimal currentPrice = tokenPriceService.getTokenPrice(holding.getToken().getSymbol());
            if (currentPrice.compareTo(BigDecimal.ZERO) > 0) {
                holding.setCurrentPrice(currentPrice);
                holding.setTotalValue(holding.getAmount().multiply(currentPrice));
                
                if (holding.getAverageBuyPrice() != null && holding.getAverageBuyPrice().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal pnlAmount = holding.getTotalValue().subtract(holding.getAmount().multiply(holding.getAverageBuyPrice()));
                    holding.setPnlAmount(pnlAmount);
                    
                    BigDecimal pnlPercentage = holding.getAverageBuyPrice().compareTo(BigDecimal.ZERO) > 0 
                        ? pnlAmount.divide(holding.getAmount().multiply(holding.getAverageBuyPrice()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        : BigDecimal.ZERO;
                    holding.setPnlPercentage(pnlPercentage);
                }
                
                portfolioTokenRepository.save(holding);
            }
        }
    }

    @Transactional
    public void removeTokenFromPortfolio(Long userId, Long portfolioId, Long tokenId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdAndIsActiveTrue(portfolioId, userId)
                .orElseThrow(() -> new DataNotFoundException("Portfolio not found"));

        PortfolioToken holding = portfolioTokenRepository.findByPortfolioIdAndTokenId(portfolioId, tokenId)
                .orElseThrow(() -> new DataNotFoundException("Token holding not found"));

        portfolioTokenRepository.delete(holding);
    }

    @Transactional
    public void deletePortfolio(Long userId, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdAndIsActiveTrue(portfolioId, userId)
                .orElseThrow(() -> new DataNotFoundException("Portfolio not found"));

        portfolio.setIsActive(false);
        portfolioRepository.save(portfolio);
    }

    private PortfolioResponse buildPortfolioResponse(Portfolio portfolio) {
        List<PortfolioToken> holdings = portfolioTokenRepository.findByPortfolioId(portfolio.getId());
        
        BigDecimal totalValue = holdings.stream()
                .map(PortfolioToken::getTotalValue)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPnl = holdings.stream()
                .map(PortfolioToken::getPnlAmount)
                .filter(pnl -> pnl != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPnlPercentage = totalValue.compareTo(BigDecimal.ZERO) > 0 
            ? totalPnl.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
            : BigDecimal.ZERO;

        List<TokenHoldingResponse> tokenHoldings = holdings.stream()
                .map(this::buildTokenHoldingResponse)
                .collect(Collectors.toList());

        return PortfolioResponse.builder()
                .id(portfolio.getId())
                .name(portfolio.getName())
                .description(portfolio.getDescription())
                .totalValue(totalValue)
                .totalPnl(totalPnl)
                .totalPnlPercentage(totalPnlPercentage)
                .tokenCount(holdings.size())
                .createdAt(portfolio.getCreatedAt())
                .tokens(tokenHoldings)
                .build();
    }

    private TokenHoldingResponse buildTokenHoldingResponse(PortfolioToken holding) {
        return TokenHoldingResponse.builder()
                .id(holding.getId())
                .symbol(holding.getToken().getSymbol())
                .name(holding.getToken().getName())
                .amount(holding.getAmount())
                .averageBuyPrice(holding.getAverageBuyPrice())
                .currentPrice(holding.getCurrentPrice())
                .totalValue(holding.getTotalValue())
                .pnlPercentage(holding.getPnlPercentage())
                .pnlAmount(holding.getPnlAmount())
                .updatedAt(holding.getUpdatedAt())
                .build();
    }
}
