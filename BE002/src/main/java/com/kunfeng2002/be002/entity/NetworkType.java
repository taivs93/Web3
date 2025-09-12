package com.kunfeng2002.be002.entity;

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
}