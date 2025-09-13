package com.kunfeng2002.be002.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NetworkType {
    ETHEREUM(NetworkCategory.EVM),
    BSC(NetworkCategory.EVM),
    AVALANCHE(NetworkCategory.EVM),
    OPTIMISM(NetworkCategory.L2_ROLLUP),
    ARBITRUM(NetworkCategory.L2_ROLLUP);

    private final NetworkCategory category;

    NetworkType(NetworkCategory category) {
        this.category = category;
    }

    public NetworkCategory getCategory() {
        return category;
    }
    @JsonCreator
    public static NetworkType fromString(String value) {
        return value == null ? null : NetworkType.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return this.name();
    }
}