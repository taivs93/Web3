-- Tạo bảng blocks
CREATE TABLE IF NOT EXISTS blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    block_number DECIMAL(20,0) NOT NULL,
    block_hash VARCHAR(66) NOT NULL UNIQUE,
    parent_hash VARCHAR(66),
    network VARCHAR(20) NOT NULL,
    timestamp DATETIME NOT NULL,
    gas_limit DECIMAL(20,0),
    gas_used DECIMAL(20,0),
    difficulty DECIMAL(30,0),
    total_difficulty DECIMAL(30,0),
    size BIGINT,
    transaction_count INT,
    base_fee_per_gas DECIMAL(20,0),
    extra_data TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_block_number (block_number),
    INDEX idx_block_hash (block_hash),
    INDEX idx_network (network),
    INDEX idx_timestamp (timestamp)
);

-- Tạo bảng transactions
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_hash VARCHAR(66) NOT NULL UNIQUE,
    block_number DECIMAL(20,0) NOT NULL,
    block_id BIGINT,
    from_address VARCHAR(42),
    to_address VARCHAR(42),
    value DECIMAL(36,18),
    gas DECIMAL(20,0) NOT NULL,
    gas_price DECIMAL(36,18),
    gas_used DECIMAL(20,0),
    nonce DECIMAL(20,0),
    network VARCHAR(20) NOT NULL,
    timestamp DATETIME NOT NULL,
    status VARCHAR(20),
    input_data TEXT,
    transaction_index INT,
    is_contract_creation BOOLEAN DEFAULT FALSE,
    contract_address VARCHAR(42),
    logs_count INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (block_id) REFERENCES blocks(id),
    INDEX idx_tx_hash (transaction_hash),
    INDEX idx_from_address (from_address),
    INDEX idx_to_address (to_address),
    INDEX idx_block_number (block_number),
    INDEX idx_network (network),
    INDEX idx_timestamp (timestamp)
);

-- Tạo bảng tokens
CREATE TABLE IF NOT EXISTS tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_address VARCHAR(42) NOT NULL,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    decimals INT NOT NULL,
    total_supply DECIMAL(36,18),
    network VARCHAR(20) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    logo_url VARCHAR(500),
    description TEXT,
    website VARCHAR(500),
    twitter VARCHAR(500),
    telegram VARCHAR(500),
    discord VARCHAR(500),
    market_cap DECIMAL(36,18),
    price_usd DECIMAL(36,18),
    volume_24h DECIMAL(36,18),
    price_change_24h DECIMAL(10,4),
    last_price_update DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_token_network (token_address, network),
    INDEX idx_token_address (token_address),
    INDEX idx_symbol (symbol),
    INDEX idx_network (network),
    INDEX idx_name (name)
);

-- Tạo bảng address_activities
CREATE TABLE IF NOT EXISTS address_activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    address VARCHAR(42) NOT NULL,
    network VARCHAR(20) NOT NULL,
    activity_type VARCHAR(20) NOT NULL,
    transaction_hash VARCHAR(66),
    block_number DECIMAL(20,0) NOT NULL,
    value DECIMAL(36,18),
    token_address VARCHAR(42),
    token_symbol VARCHAR(20),
    from_address VARCHAR(42),
    to_address VARCHAR(42),
    timestamp DATETIME NOT NULL,
    gas_used DECIMAL(20,0),
    gas_price DECIMAL(36,18),
    is_contract BOOLEAN DEFAULT FALSE,
    contract_name VARCHAR(100),
    method_name VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_address (address),
    INDEX idx_network (network),
    INDEX idx_timestamp (timestamp),
    INDEX idx_activity_type (activity_type)
);

-- Thêm một số dữ liệu mẫu cho testing
INSERT IGNORE INTO tokens (token_address, name, symbol, decimals, network, is_verified, is_active) VALUES
('0x0000000000000000000000000000000000000000', 'BNB', 'BNB', 18, 'BSC', TRUE, TRUE),
('0x55d398326f99059fF775485246999027B3197955', 'Tether USD', 'USDT', 18, 'BSC', TRUE, TRUE),
('0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d', 'USD Coin', 'USDC', 18, 'BSC', TRUE, TRUE),
('0x1AF3F329e8BE154074D8769D1FFa4eE058B1DBc3', 'Dai Token', 'DAI', 18, 'BSC', TRUE, TRUE);
