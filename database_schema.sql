-- =============================================
-- DATABASE SCHEMA CHO DỰ ÁN BE002
-- =============================================

-- Tạo database (nếu cần)
-- CREATE DATABASE be002_db;
-- USE be002_db;

-- =============================================
-- 1. BẢNG WALLETS
-- =============================================
CREATE TABLE wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    address VARCHAR(42) NOT NULL UNIQUE,
    nonce VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =============================================
-- 2. BẢNG USERS
-- =============================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wallet_id BIGINT NOT NULL UNIQUE,
    username VARCHAR(100) UNIQUE,
    email VARCHAR(255) UNIQUE,
    telegram_user_id BIGINT,
    telegram_linking_code VARCHAR(50) UNIQUE,
    telegram_linking_code_expires_at TIMESTAMP NULL,
    avatar_url VARCHAR(500),
    bio TEXT,
    is_active INT DEFAULT 1,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE
);

-- =============================================
-- 3. BẢNG CHATS
-- =============================================
CREATE TABLE chats (
    chat_id BIGINT PRIMARY KEY,
    chat_type ENUM('PRIVATE', 'GROUP', 'SUPERGROUP', 'CHANNEL') NOT NULL,
    title VARCHAR(255),
    description TEXT,
    is_active INT DEFAULT 1,
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- =============================================
-- 4. BẢNG FOLLOWS
-- =============================================
CREATE TABLE follows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    wallet_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_chat_wallet (chat_id, wallet_id),
    FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
    FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE
);

-- =============================================
-- 5. BẢNG COINS
-- =============================================
CREATE TABLE coins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    address VARCHAR(42) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    deploy_at TIMESTAMP NULL,
    decimals INT,
    total_supply DECIMAL(36,18),
    current_price DECIMAL(36,18),
    market_cap DECIMAL(36,18),
    volume_24h DECIMAL(36,18),
    price_change_24h DECIMAL(36,18),
    price_change_percentage_24h DECIMAL(36,18),
    circulating_supply DECIMAL(36,18),
    max_supply DECIMAL(36,18),
    logo_url VARCHAR(500),
    description TEXT,
    website VARCHAR(500),
    twitter VARCHAR(500),
    telegram VARCHAR(500),
    discord VARCHAR(500),
    reddit VARCHAR(500),
    github VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    last_updated TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =============================================
-- 6. BẢNG TOKENS
-- =============================================
CREATE TABLE tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    contract_address VARCHAR(42),
    network ENUM('ETHEREUM', 'BSC', 'AVALANCHE', 'OPTIMISM', 'ARBITRUM') NOT NULL,
    decimals INT DEFAULT 18,
    logo_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =============================================
-- 7. BẢNG PORTFOLIOS
-- =============================================
CREATE TABLE portfolios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =============================================
-- 8. BẢNG PORTFOLIO_TOKENS
-- =============================================
CREATE TABLE portfolio_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    token_id BIGINT NOT NULL,
    amount DECIMAL(36,18) DEFAULT 0.000000000000000000,
    average_buy_price DECIMAL(36,18),
    current_price DECIMAL(36,18),
    total_value DECIMAL(36,18),
    pnl_percentage DECIMAL(10,4),
    pnl_amount DECIMAL(36,18),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    FOREIGN KEY (token_id) REFERENCES tokens(id) ON DELETE CASCADE
);

-- =============================================
-- TẠO CÁC INDEX ĐỂ TỐI ƯU PERFORMANCE
-- =============================================

-- Index cho bảng users
CREATE INDEX idx_users_telegram_user_id ON users(telegram_user_id);
CREATE INDEX idx_users_telegram_linking_code ON users(telegram_linking_code);
CREATE INDEX idx_users_is_active ON users(is_active);
CREATE INDEX idx_users_last_login_at ON users(last_login_at);

-- Index cho bảng wallets
CREATE INDEX idx_wallets_address ON wallets(address);

-- Index cho bảng chats
CREATE INDEX idx_chats_chat_type ON chats(chat_type);
CREATE INDEX idx_chats_user_id ON chats(user_id);
CREATE INDEX idx_chats_is_active ON chats(is_active);

-- Index cho bảng follows
CREATE INDEX idx_follows_chat_id ON follows(chat_id);
CREATE INDEX idx_follows_wallet_id ON follows(wallet_id);
CREATE INDEX idx_follows_is_active ON follows(is_active);

-- Index cho bảng coins
CREATE INDEX idx_coins_symbol ON coins(symbol);
CREATE INDEX idx_coins_is_active ON coins(is_active);
CREATE INDEX idx_coins_current_price ON coins(current_price);
CREATE INDEX idx_coins_market_cap ON coins(market_cap);
CREATE INDEX idx_coins_last_updated ON coins(last_updated);

-- Index cho bảng tokens
CREATE INDEX idx_tokens_symbol ON tokens(symbol);
CREATE INDEX idx_tokens_network ON tokens(network);
CREATE INDEX idx_tokens_contract_address ON tokens(contract_address);
CREATE INDEX idx_tokens_is_active ON tokens(is_active);

-- Index cho bảng portfolios
CREATE INDEX idx_portfolios_user_id ON portfolios(user_id);
CREATE INDEX idx_portfolios_is_active ON portfolios(is_active);

-- Index cho bảng portfolio_tokens
CREATE INDEX idx_portfolio_tokens_portfolio_id ON portfolio_tokens(portfolio_id);
CREATE INDEX idx_portfolio_tokens_token_id ON portfolio_tokens(token_id);
CREATE INDEX idx_portfolio_tokens_added_at ON portfolio_tokens(added_at);

-- =============================================
-- TẠO CÁC TRIGGER ĐỂ TỰ ĐỘNG CẬP NHẬT TIMESTAMP
-- =============================================

-- Trigger cho bảng portfolios
DELIMITER $$
CREATE TRIGGER portfolios_before_insert
    BEFORE INSERT ON portfolios
    FOR EACH ROW
BEGIN
    SET NEW.created_at = CURRENT_TIMESTAMP;
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$

CREATE TRIGGER portfolios_before_update
    BEFORE UPDATE ON portfolios
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$
DELIMITER ;

-- Trigger cho bảng tokens
DELIMITER $$
CREATE TRIGGER tokens_before_insert
    BEFORE INSERT ON tokens
    FOR EACH ROW
BEGIN
    SET NEW.created_at = CURRENT_TIMESTAMP;
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$

CREATE TRIGGER tokens_before_update
    BEFORE UPDATE ON tokens
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$
DELIMITER ;

-- Trigger cho bảng portfolio_tokens
DELIMITER $$
CREATE TRIGGER portfolio_tokens_before_insert
    BEFORE INSERT ON portfolio_tokens
    FOR EACH ROW
BEGIN
    SET NEW.added_at = CURRENT_TIMESTAMP;
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$

CREATE TRIGGER portfolio_tokens_before_update
    BEFORE UPDATE ON portfolio_tokens
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$
DELIMITER ;

-- =============================================
-- INSERT DỮ LIỆU MẪU (OPTIONAL)
-- =============================================

-- Insert một số network types mặc định
-- INSERT INTO tokens (symbol, name, network, is_active) VALUES
-- ('ETH', 'Ethereum', 'ETHEREUM', TRUE),
-- ('BNB', 'Binance Coin', 'BSC', TRUE),
-- ('AVAX', 'Avalanche', 'AVALANCHE', TRUE),
-- ('OP', 'Optimism', 'OPTIMISM', TRUE),
-- ('ARB', 'Arbitrum', 'ARBITRUM', TRUE);

-- =============================================
-- CÁC QUERY HỮU ÍCH
-- =============================================

-- Query để lấy thông tin portfolio của user
-- SELECT 
--     p.id as portfolio_id,
--     p.name as portfolio_name,
--     pt.amount,
--     t.symbol,
--     t.name as token_name,
--     pt.current_price,
--     pt.total_value,
--     pt.pnl_percentage
-- FROM portfolios p
-- JOIN portfolio_tokens pt ON p.id = pt.portfolio_id
-- JOIN tokens t ON pt.token_id = t.id
-- WHERE p.user_id = ? AND p.is_active = TRUE;

-- Query để lấy danh sách coin theo market cap
-- SELECT 
--     symbol,
--     name,
--     current_price,
--     market_cap,
--     price_change_percentage_24h
-- FROM coins 
-- WHERE is_active = TRUE 
-- ORDER BY market_cap DESC 
-- LIMIT 100;

-- Query để lấy thông tin follow của chat
-- SELECT 
--     c.chat_id,
--     c.title,
--     w.address as wallet_address,
--     f.created_at as follow_date
-- FROM follows f
-- JOIN chats c ON f.chat_id = c.chat_id
-- JOIN wallets w ON f.wallet_id = w.id
-- WHERE f.is_active = TRUE;
