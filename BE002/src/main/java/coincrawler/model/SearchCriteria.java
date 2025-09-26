package coincrawler.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    
    private String query;
    
    private String firstLetter;
    
    private String symbolPattern;
    
    private String namePattern;
    
    private String addressPattern;
    
    private BigDecimal minPriceUsd;
    
    private BigDecimal maxPriceUsd;
    
    private BigDecimal minMarketCapUsd;
    
    private BigDecimal maxMarketCapUsd;
    
    private BigDecimal minVolumeUsd;
    
    private BigDecimal maxVolumeUsd;
    
    private BigDecimal minLiquidityUsd;
    
    private BigDecimal maxLiquidityUsd;
    
    private BigDecimal minPriceChangePercentage24h;
    
    private BigDecimal maxPriceChangePercentage24h;
    
    private LocalDateTime createdAfter;
    
    private LocalDateTime createdBefore;
    
    private LocalDateTime updatedAfter;
    
    private LocalDateTime updatedBefore;
    
    private Boolean isVerified;
    
    private Boolean isScam;
    
    private Boolean isStablecoin;
    
    private Boolean isNew;
    
    private Boolean isTrending;
    
    @Min(0)
    private Integer minSecurityScore;
    
    @Min(0)
    private Integer maxSecurityScore;
    
    private List<String> dexNames;
    
    private List<String> factoryAddresses;
    
    private String sortBy;
    
    private SortDirection sortDirection = SortDirection.DESC;
    
    @PositiveOrZero
    private int page = 0;
    
    @Min(1)
    private int size = 20;
    
    public enum SortDirection {
        ASC, DESC
    }
    
    public int getOffset() {
        return page * size;
    }
    
    public boolean hasFilters() {
        return query != null || firstLetter != null || symbolPattern != null || 
               namePattern != null || addressPattern != null || minPriceUsd != null ||
               maxPriceUsd != null || minMarketCapUsd != null || maxMarketCapUsd != null ||
               minVolumeUsd != null || maxVolumeUsd != null || minLiquidityUsd != null ||
               maxLiquidityUsd != null || minPriceChangePercentage24h != null ||
               maxPriceChangePercentage24h != null || createdAfter != null ||
               createdBefore != null || updatedAfter != null || updatedBefore != null ||
               isVerified != null || isScam != null || isStablecoin != null ||
               isNew != null || isTrending != null || minSecurityScore != null ||
               maxSecurityScore != null || (dexNames != null && !dexNames.isEmpty()) ||
               (factoryAddresses != null && !factoryAddresses.isEmpty());
    }
}
