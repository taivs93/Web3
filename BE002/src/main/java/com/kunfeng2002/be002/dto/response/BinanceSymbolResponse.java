package com.kunfeng2002.be002.dto.response;

public class BinanceSymbolResponse {
    private String symbol;
    private String status;
    private String baseAsset;
    private String baseAssetPrecision;
    private String quoteAsset;
    private String quotePrecision;
    private String quoteAssetPrecision;
    private String orderTypes;
    private boolean icebergAllowed;
    private boolean ocoAllowed;
    private boolean isSpotTradingAllowed;
    private boolean isMarginTradingAllowed;
    private String[] filters;
    private String[] permissions;
    private long time;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBaseAsset() { return baseAsset; }
    public void setBaseAsset(String baseAsset) { this.baseAsset = baseAsset; }

    public String getBaseAssetPrecision() { return baseAssetPrecision; }
    public void setBaseAssetPrecision(String baseAssetPrecision) { this.baseAssetPrecision = baseAssetPrecision; }

    public String getQuoteAsset() { return quoteAsset; }
    public void setQuoteAsset(String quoteAsset) { this.quoteAsset = quoteAsset; }

    public String getQuotePrecision() { return quotePrecision; }
    public void setQuotePrecision(String quotePrecision) { this.quotePrecision = quotePrecision; }

    public String getQuoteAssetPrecision() { return quoteAssetPrecision; }
    public void setQuoteAssetPrecision(String quoteAssetPrecision) { this.quoteAssetPrecision = quoteAssetPrecision; }

    public String getOrderTypes() { return orderTypes; }
    public void setOrderTypes(String orderTypes) { this.orderTypes = orderTypes; }

    public boolean isIcebergAllowed() { return icebergAllowed; }
    public void setIcebergAllowed(boolean icebergAllowed) { this.icebergAllowed = icebergAllowed; }

    public boolean isOcoAllowed() { return ocoAllowed; }
    public void setOcoAllowed(boolean ocoAllowed) { this.ocoAllowed = ocoAllowed; }

    public boolean isSpotTradingAllowed() { return isSpotTradingAllowed; }
    public void setSpotTradingAllowed(boolean spotTradingAllowed) { isSpotTradingAllowed = spotTradingAllowed; }

    public boolean isMarginTradingAllowed() { return isMarginTradingAllowed; }
    public void setMarginTradingAllowed(boolean marginTradingAllowed) { isMarginTradingAllowed = marginTradingAllowed; }

    public String[] getFilters() { return filters; }
    public void setFilters(String[] filters) { this.filters = filters; }

    public String[] getPermissions() { return permissions; }
    public void setPermissions(String[] permissions) { this.permissions = permissions; }

    public long getTime() { return time; }
    public void setTime(long time) { this.time = time; }
}
