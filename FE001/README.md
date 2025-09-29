# Web3 Chat Bot Frontend

Frontend cho ứng dụng Web3 Chat Bot, được xây dựng với Vue 3 và Tailwind CSS.

## 🚀 Tính năng chính

### 🔐 Xác thực Web3
- Đăng nhập bằng MetaMask
- Xác thực bằng chữ ký số
- Quản lý profile người dùng

### 💼 Portfolio Management
- Tạo và quản lý portfolio
- Thêm/xóa token
- Theo dõi giá real-time
- Tính toán P&L

### 🔍 Tìm kiếm Coin & Token
- Tìm kiếm theo tên, symbol
- Tìm kiếm theo địa chỉ contract
- Tìm kiếm online
- Hiển thị thông tin chi tiết

### 👛 Quản lý Ví
- Theo dõi địa chỉ ví
- Danh sách ví toàn cầu
- Thông báo hoạt động ví

### ⚡ Gas Fee Widget
- Hiển thị phí gas real-time
- Hỗ trợ nhiều network
- Tự động cập nhật

### 🔔 Thông báo Real-time
- WebSocket integration
- Thông báo hoạt động ví
- Cảnh báo giá
- Cập nhật portfolio

## 🛠️ Công nghệ sử dụng

- **Vue 3** - Framework JavaScript
- **Vue Router** - Routing
- **Pinia** - State management
- **Tailwind CSS** - Styling
- **Axios** - HTTP client
- **Ethers.js** - Web3 integration
- **WebSocket** - Real-time communication

## 📦 Cài đặt

```bash
# Cài đặt dependencies
npm install

# Chạy development server
npm run dev

# Build cho production
npm run build

# Preview build
npm run preview
```

## 🔧 Cấu hình

### Backend API
Cập nhật `API_BASE_URL` trong `src/services/api.js`:

```javascript
const API_BASE_URL = 'http://localhost:8080/api'
```

### WebSocket
Cập nhật WebSocket URL trong `src/services/simple-websocket.js`:

```javascript
this.ws = new WebSocket('ws://localhost:8080/api/ws')
```

## 📱 Các trang chính

### 🏠 Dashboard (`/dashboard`)
- Trang chủ với chat bot
- Thông tin tài khoản
- Quick commands
- Thông báo real-time

### 💼 Portfolio (`/portfolio`)
- Quản lý portfolio
- Thêm/xóa token
- Theo dõi hiệu suất

### 🔍 Search (`/search`)
- Tìm kiếm coin/token
- Nhiều loại tìm kiếm
- Hiển thị thông tin chi tiết

### 👛 Wallet (`/wallet`)
- Quản lý ví theo dõi
- Danh sách ví toàn cầu
- Thêm/bỏ theo dõi

### 👤 Profile (`/profile`)
- Thông tin cá nhân
- Liên kết Telegram
- Cập nhật profile

## 🔌 API Integration

### Auth API
- `POST /login` - Đăng nhập
- `GET /profile` - Lấy profile
- `PUT /profile` - Cập nhật profile
- `GET /nonce` - Lấy nonce

### Portfolio API
- `POST /portfolio` - Tạo portfolio
- `GET /portfolio` - Lấy danh sách
- `POST /portfolio/tokens` - Thêm token
- `DELETE /portfolio/{id}` - Xóa portfolio

### Coin API
- `GET /coins` - Lấy tất cả coins
- `GET /coins/search` - Tìm kiếm
- `GET /coins/{address}` - Lấy theo địa chỉ

### Wallet API
- `GET /wallet/followed-addresses` - Ví đang theo dõi
- `POST /wallet/follow` - Theo dõi ví
- `POST /wallet/unfollow` - Bỏ theo dõi

### Gas API
- `GET /gas/estimate/{network}` - Ước tính gas fee

## 🌐 WebSocket Events

### Các loại thông báo:
- `notification` - Thông báo hệ thống
- `wallet-activity` - Hoạt động ví
- `price-alert` - Cảnh báo giá
- `portfolio-update` - Cập nhật portfolio
- `gas-update` - Cập nhật gas fee

## 🎨 UI Components

### Components chính:
- `GasFeeWidget` - Widget hiển thị gas fee
- `NotificationBell` - Thông báo
- `ChatWidget` - Chat bot interface

## 🔒 Bảo mật

- Xác thực bằng MetaMask
- Chữ ký số cho đăng nhập
- CORS configuration
- Input validation

## 📱 Responsive Design

- Mobile-first approach
- Tailwind CSS responsive utilities
- Optimized cho mobile và desktop

## 🚀 Deployment

### Development
```bash
npm run dev
```

### Production
```bash
npm run build
# Deploy thư mục dist/
```

## 🤝 Contributing

1. Fork repository
2. Tạo feature branch
3. Commit changes
4. Push to branch
5. Tạo Pull Request

## 📄 License

MIT License

## 🆘 Support

Nếu gặp vấn đề, vui lòng tạo issue trên GitHub repository.