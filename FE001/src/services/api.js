import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

// Tạo axios instance với config mặc định
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
})

// Request interceptor để thêm auth token nếu có
apiClient.interceptors.request.use(
  (config) => {
    // Có thể thêm token vào header nếu cần
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor để xử lý lỗi chung
apiClient.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    console.error('API Error:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)

// Auth API
export const authAPI = {
  // Đăng nhập với Web3 signature
  login: (address, message, signature) => 
    apiClient.post('/login', { address, message, signature }),
  
  // Lấy nonce cho địa chỉ ví
  getNonce: (address) => 
    apiClient.get('/nonce', { params: { address } }),
  
  // Lấy thông tin profile user
  getProfile: (address) => 
    apiClient.get('/profile', { params: { address } }),
  
  // Cập nhật profile user
  updateProfile: (data) => 
    apiClient.put('/profile', data),
  
  // Health check
  health: () => 
    apiClient.get('/health')
}

// Chat API
export const chatAPI = {
  // Gửi message đến Telegram bot
  sendMessage: (command, walletAddress, argument) => 
    apiClient.post('/chat/send', { command, walletAddress, argument }),
  
  // Tạo linking code (POST)
  linkAccount: (walletAddress) => 
    apiClient.post('/chat/link-account', { walletAddress }),
  
  // Tạo linking code (GET)
  getLinkAccount: (walletAddress) => 
    apiClient.get('/chat/link_account', { params: { walletAddress } }),
  
  // Health check chat service
  health: () => 
    apiClient.get('/chat/health')
}

// Gas API
export const gasAPI = {
  // Lấy ước tính phí gas cho network
  getGasEstimate: (network, request = null) => 
    apiClient.post(`/gas/estimate/${network}`, request),
  
  // Lấy ước tính phí gas nhanh với default values
  getGasEstimateQuick: (network) => 
    apiClient.get(`/gas/estimate/${network}`)
}

// Search API
export const searchAPI = {
  // Tìm kiếm tổng quát
  generalSearch: (query, network, page = 0, size = 20) => 
    apiClient.post('/search/general', { query, network, page, size }),
  
  // Tìm kiếm block
  searchBlock: (query, network) => 
    apiClient.get('/search/block', { params: { query, network } }),
  
  // Tìm kiếm transaction
  searchTransaction: (query, network) => 
    apiClient.get('/search/transaction', { params: { query, network } }),
  
  // Tìm kiếm address
  searchAddress: (query, network, page = 0, size = 20) => 
    apiClient.get('/search/address', { params: { query, network, page, size } }),
  
  // Tìm kiếm token
  searchToken: (query, network, page = 0, size = 20) => 
    apiClient.get('/search/token', { params: { query, network, page, size } }),
  
  // Lấy thống kê network
  getNetworkStats: (network) => 
    apiClient.get(`/search/stats/${network}`),
  
  // Lấy top tokens
  getTopTokens: (network, limit = 10) => 
    apiClient.get('/search/tokens/top', { params: { network, limit } }),
  
  // Lấy recent transactions
  getRecentTransactions: (network, limit = 10) => 
    apiClient.get('/search/transactions/recent', { params: { network, limit } })
}

// WebSocket API
export const websocketAPI = {
  // WebSocket endpoint
  getWebSocketUrl: () => {
    const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
    const host = window.location.hostname
    const port = '8080'
    return `${protocol}//${host}:${port}/api/ws`
  }
}

export default apiClient
