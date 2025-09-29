package com.kunfeng2002.be002.dto.response;

import java.time.LocalDateTime;

public class NewCoinResponse {
    private String name;
    private String symbol;
    private String addressContract;
    private LocalDateTime listed;

    public NewCoinResponse(String name, String symbol, String addressContract, LocalDateTime listed) {
        this.name = name;
        this.symbol = symbol;
        this.addressContract = addressContract;
        this.listed = listed;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getAddressContract() { return addressContract; }
    public void setAddressContract(String addressContract) { this.addressContract = addressContract; }

    public LocalDateTime getListed() { return listed; }
    public void setListed(LocalDateTime listed) { this.listed = listed; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String symbol;
        private String addressContract;
        private LocalDateTime listed;

        public Builder name(String name) { this.name = name; return this; }
        public Builder symbol(String symbol) { this.symbol = symbol; return this; }
        public Builder addressContract(String addressContract) { this.addressContract = addressContract; return this; }
        public Builder listed(LocalDateTime listed) { this.listed = listed; return this; }

        public NewCoinResponse build() {
            return new NewCoinResponse(name, symbol, addressContract, listed);
        }
    }
}
