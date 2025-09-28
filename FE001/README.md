# Web3 Dashboard Frontend

Frontend ứng dụng Web3 Dashboard với đầy đủ tính năng quản lý ví, theo dõi giao dịch và thông báo real-time.

## 🚀 Tính năng chính

### 📊 Dashboard
- Tổng quan hoạt động ví
- Thống kê giao dịch
- Theo dõi địa chỉ ví
- Quản lý cảnh báo giá

### 🔔 Thông báo Real-time
- WebSocket notifications
- Thông báo hoạt động ví
- Cảnh báo giá crypto
- Tích hợp Telegram bot

### ⛽ Gas Calculator
- Tính toán phí gas cho nhiều networks
- Hỗ trợ Ethereum, BSC, Arbitrum, Optimism, Avalanche
- Ước tính thời gian giao dịch
- So sánh chi phí giữa các networks

### 🤖 Telegram Integration
- Chat bot với các lệnh:
  - `/start` - Lời chào mừng
  - `/help` - Xem danh sách lệnh
  - `/follow <address>` - Theo dõi ví
  - `/unfollow <address>` - Bỏ theo dõi ví
  - `/list` - Xem danh sách ví đang theo dõi
  - `/gas <network>` - Xem giá gas
  - `/setalert <symbol> <price>` - Đặt cảnh báo giá
  - `/link <code>` - Liên kết tài khoản

## 🛠️ Công nghệ sử dụng

- **Frontend**: Vue.js 3 + Composition API
- **UI Framework**: TailwindCSS
- **State Management**: Pinia
- **Routing**: Vue Router 4
- **Web3**: Ethers.js + Web3.js
- **Real-time**: WebSocket + SockJS + STOMP
- **Charts**: Chart.js + Vue-ChartJS
- **Date**: date-fns
- **Build Tool**: Vite

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

### Environment Variables
Tạo file `.env.local`:
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_URL=http://localhost:8080/api/ws
```

### Backend Integration
Frontend tích hợp với backend Spring Boot qua các API endpoints:

- **Authentication**: `/api/login`, `/api/profile`, `/api/nonce`
- **Chat**: `/api/chat/send`, `/api/chat/link-account`
- **WebSocket**: `/api/ws` cho real-time notifications

## 📱 Cấu trúc dự án

```
src/
├── components/          # Reusable components
│   ├── AppLayout.vue    # Main layout
│   ├── ChatWidget.vue   # Telegram chat widget
│   ├── NotificationBell.vue # Notification component
│   ├── AddAddressModal.vue  # Add address modal
│   └── PriceAlertModal.vue  # Price alert modal
├── views/               # Page components
│   ├── Home.vue         # Landing page
│   ├── Login.vue        # Login page
│   ├── Dashboard.vue    # Main dashboard
│   ├── Profile.vue      # User profile
│   ├── Notifications.vue # Notifications page
│   └── GasCalculator.vue # Gas calculator
├── stores/              # Pinia stores
│   └── auth.js          # Authentication store
├── services/            # API services
│   └── api.js           # API client
└── router/              # Vue Router
    └── index.js         # Route configuration
```

## 🎯 Tính năng chi tiết

### Dashboard
- **Stats Cards**: Hiển thị thống kê tổng quan
- **Recent Activity**: Danh sách hoạt động gần đây
- **Watched Addresses**: Quản lý địa chỉ theo dõi
- **Gas Prices**: Giá gas hiện tại các networks
- **Price Alerts**: Cảnh báo giá crypto
- **Quick Actions**: Thao tác nhanh

### Notifications
- **Real-time**: Thông báo qua WebSocket
- **Filtering**: Lọc theo loại thông báo
- **Pagination**: Phân trang thông báo
- **Bulk Actions**: Đánh dấu đã đọc, xóa hàng loạt

### Gas Calculator
- **Multi-network**: Hỗ trợ nhiều blockchain
- **Real-time Prices**: Giá gas cập nhật real-time
- **Cost Estimation**: Ước tính chi phí giao dịch
- **Speed Estimates**: Ước tính thời gian giao dịch

### Telegram Bot
- **Command Interface**: Giao diện lệnh trực quan
- **Address Management**: Quản lý địa chỉ theo dõi
- **Price Alerts**: Cảnh báo giá qua Telegram
- **Account Linking**: Liên kết tài khoản Web3 với Telegram

## 🔐 Authentication

Sử dụng MetaMask để xác thực:
1. Kết nối ví MetaMask
2. Ký thông điệp xác thực
3. Backend xác minh chữ ký
4. Tạo session và redirect đến dashboard

## 📡 Real-time Features

- **WebSocket Connection**: Kết nối real-time với backend
- **Wallet Activity**: Thông báo hoạt động ví
- **Price Alerts**: Cảnh báo giá crypto
- **Telegram Integration**: Đồng bộ với Telegram bot

## 🎨 UI/UX

- **Responsive Design**: Tương thích mobile và desktop
- **Dark/Light Mode**: Chế độ sáng/tối (có thể thêm)
- **Smooth Animations**: Hiệu ứng mượt mà
- **Accessibility**: Hỗ trợ người dùng khuyết tật

## 🚀 Deployment

### Development
```bash
npm run dev
```

### Production
```bash
npm run build
npm run preview
```

### Docker (Optional)
```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "run", "preview"]
```

## 🤝 Contributing

1. Fork repository
2. Tạo feature branch
3. Commit changes
4. Push to branch
5. Tạo Pull Request

## 📄 License

MIT License - xem file LICENSE để biết thêm chi tiết.

## 🆘 Support

Nếu gặp vấn đề, vui lòng tạo issue trên GitHub hoặc liên hệ qua Telegram bot.