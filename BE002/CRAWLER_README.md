# Blockchain Crawler System

## Tổng quan

Hệ thống crawler blockchain được thiết kế để thu thập và tìm kiếm dữ liệu từ các blockchain networks, hỗ trợ tìm kiếm theo:
- **Block**: Số block, hash block
- **Transaction**: Hash transaction, địa chỉ gửi/nhận
- **Address**: Địa chỉ ví, hoạt động giao dịch
- **Token/Coin**: Token address, symbol, tên token

## Kiến trúc hệ thống

### 1. Entities (Database Models)
- `Block`: Lưu trữ thông tin block
- `Transaction`: Lưu trữ thông tin giao dịch
- `Token`: Lưu trữ thông tin token/coin
- `AddressActivity`: Lưu trữ hoạt động của địa chỉ

### 2. Services
- `BlockchainCrawlerService`: Crawl dữ liệu từ blockchain
- `SearchService`: Xử lý tìm kiếm dữ liệu

### 3. Controllers
- `SearchController`: REST API endpoints cho tìm kiếm

## Cấu hình

### application.yml
```yaml
app:
  crawler:
    batch-size: 100                    # Số block xử lý mỗi batch
    max-blocks-per-run: 1000          # Số block tối đa mỗi lần crawl
    enabled-networks: BSC             # Networks được kích hoạt
    crawl-interval: 30000             # Khoảng thời gian crawl (ms)
    enable-scheduled-crawling: true    # Bật/tắt scheduled crawling
```

## API Endpoints

### 1. Tìm kiếm tổng quát
```http
POST /api/search/general
Content-Type: application/json

{
    "query": "0x123...",
    "network": "BSC",
    "page": 0,
    "size": 20
}
```

### 2. Tìm kiếm Block
```http
GET /api/search/block?query=12345&network=BSC
```

### 3. Tìm kiếm Transaction
```http
GET /api/search/transaction?query=0xabc...&network=BSC
```

### 4. Tìm kiếm Address
```http
GET /api/search/address?query=0xdef...&network=BSC&page=0&size=20
```

### 5. Tìm kiếm Token
```http
GET /api/search/token?query=USDT&network=BSC&page=0&size=20
```

### 6. Thống kê Network
```http
GET /api/search/stats/BSC
```

### 7. Top Tokens
```http
GET /api/search/tokens/top?network=BSC&limit=10
```

### 8. Recent Transactions
```http
GET /api/search/transactions/recent?network=BSC&limit=10
```

## Cách sử dụng

### 1. Khởi tạo Database
```sql
-- Chạy script tạo bảng
source src/main/resources/sql/create_search_tables.sql
```

### 2. Cấu hình Network
Cập nhật `application.yml` để thêm networks mới:
```yaml
network:
  bsc:
    rpc-url: wss://your-bsc-rpc-url
    chain-id: 56
  ethereum:
    rpc-url: wss://your-eth-rpc-url
    chain-id: 1
```

### 3. Khởi động ứng dụng
```bash
mvn spring-boot:run
```

### 4. Kiểm tra Crawler
- Crawler sẽ tự động bắt đầu crawl dữ liệu sau khi khởi động
- Kiểm tra logs để xem tiến trình crawl
- Sử dụng API để tìm kiếm dữ liệu đã crawl

## Tính năng Crawler

### 1. Scheduled Crawling
- Tự động crawl dữ liệu mới mỗi 30 giây
- Chỉ crawl blocks mới chưa được xử lý
- Hỗ trợ multiple networks

### 2. Manual Crawling
```java
// Crawl một range blocks cụ thể
crawlerService.crawlBlockRange("BSC", startBlock, endBlock);

// Crawl dữ liệu token
crawlerService.crawlTokenData("BSC", contractAddress);
```

### 3. Error Handling
- Retry mechanism cho failed requests
- Logging chi tiết cho debugging
- Graceful handling của network errors

## Performance Optimization

### 1. Database Indexing
- Indexes được tạo cho các trường thường xuyên query
- Composite indexes cho multi-column queries

### 2. Batch Processing
- Xử lý blocks theo batch để tối ưu performance
- Async processing cho non-blocking operations

### 3. Caching
- Redis caching cho frequently accessed data
- Configurable cache TTL

## Monitoring

### 1. Logs
- Structured logging với log levels
- Performance metrics logging
- Error tracking và alerting

### 2. Health Checks
- Database connectivity
- Network RPC availability
- Crawler status monitoring

## Mở rộng

### 1. Thêm Network mới
1. Cập nhật `Web3Config.java`
2. Thêm network vào `application.yml`
3. Cập nhật `enabled-networks` config

### 2. Thêm tính năng tìm kiếm
1. Tạo method mới trong `SearchService`
2. Thêm endpoint trong `SearchController`
3. Tạo DTO classes tương ứng

### 3. Tối ưu Performance
1. Thêm database indexes
2. Implement caching strategies
3. Optimize query patterns

## Troubleshooting

### 1. Crawler không hoạt động
- Kiểm tra RPC URL có đúng không
- Kiểm tra network connectivity
- Xem logs để tìm lỗi cụ thể

### 2. Tìm kiếm chậm
- Kiểm tra database indexes
- Optimize query patterns
- Scale database nếu cần

### 3. Memory issues
- Giảm `batch-size` và `max-blocks-per-run`
- Tăng JVM heap size
- Monitor memory usage

## Security

### 1. API Security
- Input validation
- Rate limiting
- CORS configuration

### 2. Database Security
- Connection encryption
- Access control
- Data sanitization

## Backup & Recovery

### 1. Database Backup
- Regular automated backups
- Point-in-time recovery
- Cross-region replication

### 2. Data Integrity
- Transaction consistency
- Data validation
- Error recovery mechanisms
