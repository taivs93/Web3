# Web3 Frontend Application

Ứng dụng frontend Vue.js 3 cho hệ thống Web3 Authentication và Portfolio Management.

## 🚀 Tính năng chính

### 1. Authentication & User Management
- **MetaMask Integration**: Đăng nhập bằng ví Ethereum
- **Signature Verification**: Xác thực danh tính bằng chữ ký số
- **Profile Management**: Quản lý thông tin cá nhân
- **Session Persistence**: Lưu trạng thái đăng nhập

### 2. Portfolio Management
- **Portfolio Creation**: Tạo và quản lý portfolio
- **Token Tracking**: Theo dõi token và giá trị
- **P&L Calculation**: Tính toán lãi/lỗ
- **Price Updates**: Cập nhật giá real-time

### 3. Coin & Token Search
- **Multi-search**: Tìm kiếm theo tên, symbol, địa chỉ contract
- **Online Search**: Tìm kiếm trực tuyến từ API
- **Address Lookup**: Tìm kiếm theo địa chỉ contract
- **Real-time Data**: Dữ liệu cập nhật liên tục

### 4. Gas Fee Tracker
- **Multi-network Support**: Hỗ trợ nhiều mạng blockchain
- **Real-time Gas Prices**: Giá gas theo thời gian thực
- **Enhanced Data**: Dữ liệu gas nâng cao
- **Cost Estimation**: Ước tính chi phí giao dịch

### 5. Admin Panel
- **System Health**: Kiểm tra trạng thái hệ thống
- **Cache Management**: Quản lý cache
- **Price Updates**: Cập nhật giá thủ công
- **Service Monitoring**: Giám sát các dịch vụ

### 6. Chat Bot Integration
- **Telegram Bot**: Tích hợp với Telegram bot
- **Wallet Management**: Quản lý ví qua chat
- **Command Processing**: Xử lý lệnh chat
- **Real-time Notifications**: Thông báo real-time

## 🛠️ Công nghệ sử dụng

### Frontend
- **Vue.js 3**: Framework chính
- **Vue Router**: Routing
- **Pinia**: State management
- **TailwindCSS**: Styling
- **Axios**: HTTP client
- **Ethers.js**: Web3 integration

### Backend Integration
- **REST API**: Giao tiếp với Spring Boot backend
- **WebSocket**: Kết nối real-time
- **JWT Authentication**: Xác thực token

## 📁 Cấu trúc thư mục

```
src/
├── components/          # Components tái sử dụng
│   ├── AppLayout.vue   # Layout chính
│   ├── ChatWidget.vue  # Widget chat
│   ├── GasFeeWidget.vue # Widget gas fee
│   └── NotificationBell.vue # Thông báo
├── views/              # Các trang chính
│   ├── Home.vue        # Trang chủ
│   ├── Login.vue       # Đăng nhập
│   ├── Dashboard.vue   # Dashboard
│   ├── Portfolio.vue   # Quản lý portfolio
│   ├── Search.vue      # Tìm kiếm coin
│   ├── GasFee.vue      # Gas fee tracker
│   ├── Wallet.vue      # Quản lý ví
│   ├── Profile.vue     # Hồ sơ cá nhân
│   └── Admin.vue       # Admin panel
├── services/           # API services
│   ├── api.js         # API client
│   ├── websocket.js   # WebSocket service
│   └── simple-websocket.js # Simple WebSocket
├── stores/             # Pinia stores
│   └── auth.js        # Authentication store
├── router/             # Vue Router
│   └── index.js       # Router configuration
└── composables/        # Vue composables
    └── useAuthInit.js  # Auth initialization
```

## 🚀 Cài đặt và chạy

### Yêu cầu hệ thống
- Node.js >= 16
- npm hoặc yarn
- MetaMask extension

### Cài đặt
```bash
# Clone repository
git clone <repository-url>
cd FE001

# Cài đặt dependencies
npm install

# Chạy development server
npm run dev

# Build cho production
npm run build
```

### Cấu hình
1. Đảm bảo backend đang chạy trên `http://localhost:8080`
2. Cài đặt MetaMask extension
3. Cấu hình network trong MetaMask nếu cần

## 📱 Sử dụng

### 1. Đăng nhập
1. Truy cập trang chủ
2. Click "Bắt đầu ngay" hoặc "Đăng nhập"
3. Kết nối MetaMask
4. Ký thông điệp xác thực
5. Đăng nhập thành công

### 2. Quản lý Portfolio
1. Vào trang "Portfolio"
2. Click "Create New Portfolio"
3. Nhập tên và mô tả
4. Thêm token vào portfolio
5. Theo dõi P&L

### 3. Tìm kiếm Coin
1. Vào trang "Tìm kiếm"
2. Chọn loại tìm kiếm
3. Nhập từ khóa
4. Xem kết quả

### 4. Gas Fee Tracker
1. Vào trang "Gas Fee"
2. Chọn network
3. Xem giá gas real-time
4. Sử dụng enhanced data

### 5. Admin Panel
1. Đăng nhập với quyền admin
2. Vào trang "Admin"
3. Kiểm tra health status
4. Quản lý cache và services

## 🔧 API Endpoints

### Authentication
- `POST /api/login` - Đăng nhập
- `GET /api/profile` - Lấy profile
- `PUT /api/profile` - Cập nhật profile
- `GET /api/nonce` - Lấy nonce

### Portfolio
- `POST /api/portfolio` - Tạo portfolio
- `GET /api/portfolio` - Lấy danh sách portfolio
- `GET /api/portfolio/{id}` - Lấy chi tiết portfolio
- `POST /api/portfolio/tokens` - Thêm token
- `DELETE /api/portfolio/{id}/tokens/{tokenId}` - Xóa token
- `PUT /api/portfolio/{id}/refresh` - Refresh giá
- `DELETE /api/portfolio/{id}` - Xóa portfolio

### Coin Search
- `GET /api/coins` - Lấy tất cả coins
- `GET /api/coins/{address}` - Lấy coin theo địa chỉ
- `GET /api/coins/symbol/{symbol}` - Lấy coin theo symbol
- `GET /api/coins/search` - Tìm kiếm coin
- `GET /api/coins/search-online` - Tìm kiếm online

### Gas Fee
- `GET /api/gas/estimate/{network}` - Ước tính gas
- `POST /api/gas/estimate/{network}` - Ước tính gas chi tiết

### Admin
- `POST /api/admin/portfolio/update-prices` - Cập nhật giá
- `GET /api/admin/gas/enhanced/{network}` - Gas nâng cao
- `GET /api/admin/price/enhanced` - Giá token nâng cao
- `POST /api/admin/cache/clear` - Xóa cache
- `GET /api/admin/health/services` - Kiểm tra health

### Chat & Wallet
- `POST /api/chat/send` - Gửi tin nhắn
- `POST /api/chat/link-account` - Liên kết tài khoản
- `GET /api/wallet/followed-addresses` - Ví đang theo dõi
- `POST /api/wallet/follow` - Theo dõi ví
- `POST /api/wallet/unfollow` - Bỏ theo dõi ví

## 🔒 Bảo mật

- **Signature Verification**: Xác thực chữ ký MetaMask
- **Nonce Protection**: Bảo vệ chống replay attacks
- **Session Management**: Quản lý phiên đăng nhập
- **Input Validation**: Validate dữ liệu đầu vào

## 🌐 WebSocket Integration

- **Real-time Updates**: Cập nhật real-time
- **Price Alerts**: Cảnh báo giá
- **Wallet Activity**: Hoạt động ví
- **System Notifications**: Thông báo hệ thống

## 📊 State Management

### Auth Store
- User information
- Wallet connection
- Authentication state
- Session persistence

### Features
- Reactive state
- Local storage sync
- Error handling
- Loading states

## 🎨 UI/UX Features

- **Responsive Design**: Tương thích mọi thiết bị
- **Dark/Light Mode**: Chế độ sáng/tối
- **Smooth Animations**: Hiệu ứng mượt mà
- **Loading States**: Trạng thái loading
- **Error Handling**: Xử lý lỗi thân thiện

## 🚀 Performance

- **Lazy Loading**: Tải trang lazy
- **Code Splitting**: Chia nhỏ code
- **Caching**: Cache dữ liệu
- **Optimized Images**: Tối ưu hình ảnh

## 🔧 Development

### Scripts
```bash
npm run dev          # Development server
npm run build        # Build production
npm run preview      # Preview build
```

### Linting
```bash
npm run lint         # Check linting
npm run lint:fix     # Fix linting issues
```

## 📝 Changelog

### v1.0.0
- Initial release
- Authentication system
- Portfolio management
- Coin search
- Gas fee tracker
- Admin panel
- Chat bot integration

## 🤝 Contributing

1. Fork repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## 📄 License

MIT License - xem file LICENSE để biết thêm chi tiết.

## 🆘 Support

Nếu gặp vấn đề, vui lòng tạo issue trên GitHub hoặc liên hệ team phát triển.

---

**Lưu ý**: Đây là phiên bản development. Không sử dụng trong môi trường production mà không có kiểm tra bảo mật đầy đủ.