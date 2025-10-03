import axios from 'axios'

const API_BASE_URL = '/api'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
})


export const authAPI = {
  login: (address, message, signature) => 
    apiClient.post('/login', { address, message, signature }),

  getNonce: (address) => 
    apiClient.get('/nonce', { params: { address } }),

  getProfile: (address) => 
    apiClient.get('/profile', { params: { address } }),

  updateProfile: (address, username, email, avatarUrl, bio) =>
    apiClient.put('/profile', { address, username, email, avatarUrl, bio }),

  health: () => 
    apiClient.get('/health')
}


export const chatAPI = {
  sendMessage: (command, walletAddress, argument) =>
    apiClient.post('/chat/send', { command, walletAddress, argument }),

  linkAccount: (walletAddress) => 
    apiClient.post('/chat/link-account', { walletAddress }),
  
  // Tạo linking code (GET từ ChatController)
  getLinkAccount: (walletAddress) => 
    apiClient.get('/chat/link_account', { params: { walletAddress } }),
  
  // Lấy danh sách địa chỉ đang theo dõi
  getFollowedAddresses: (walletAddress) =>
    apiClient.get('/chat/followed-addresses', { params: { walletAddress } }),

  // Tìm kiếm coin
  searchCoins: (query, walletAddress) =>
    apiClient.post('/chat/search', { argument: query, walletAddress }),

  // Lấy gas estimate
  getGasEstimate: (argument, walletAddress) =>
    apiClient.post('/chat/gas', { argument, walletAddress }),

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

// Admin API
export const adminAPI = {
  // Trigger portfolio price update
  triggerPortfolioPriceUpdate: () =>
    apiClient.post('/admin/portfolio/update-prices'),

  // Get enhanced gas price
  getEnhancedGasPrice: (network) =>
    apiClient.get(`/admin/gas/enhanced/${network}`),

  // Get enhanced token prices
  getEnhancedTokenPrices: (symbols) =>
    apiClient.get('/admin/price/enhanced', { params: { symbols: symbols.join(',') } }),

  // Clear all caches
  clearAllCaches: () =>
    apiClient.post('/admin/cache/clear'),

  // Check services health
  checkServicesHealth: () =>
    apiClient.get('/admin/health/services')
}

// Wallet API
export const walletAPI = {
  // Lấy danh sách địa chỉ đang theo dõi theo chatId
  getFollowedAddresses: (chatId) =>
    apiClient.get('/wallet/followed-addresses', { params: { chatId } }),

  // Lấy danh sách địa chỉ được theo dõi toàn cục
  getGlobalFollowedAddresses: () =>
    apiClient.get('/wallet/global-addresses'),

  // Theo dõi địa chỉ ví
  followWallet: (chatId, address) =>
    apiClient.post('/wallet/follow', null, { params: { chatId, address } }),

  // Bỏ theo dõi địa chỉ ví
  unfollowWallet: (chatId, address) =>
    apiClient.post('/wallet/unfollow', null, { params: { chatId, address } }),

  // Health check wallet service
  health: () =>
    apiClient.get('/wallet/health')
}

// Portfolio API
export const portfolioAPI = {
  // Tạo portfolio mới
  createPortfolio: (userId, portfolioData) =>
    apiClient.post('/portfolio', portfolioData, { headers: { 'X-User-Id': userId } }),

  // Lấy danh sách portfolio của user
  getUserPortfolios: (userId) =>
    apiClient.get('/portfolio', { headers: { 'X-User-Id': userId } }),

  // Lấy chi tiết portfolio
  getPortfolio: (userId, portfolioId) =>
    apiClient.get(`/portfolio/${portfolioId}`, { headers: { 'X-User-Id': userId } }),

  // Thêm token vào portfolio
  addTokenToPortfolio: (userId, tokenData) =>
    apiClient.post('/portfolio/tokens', tokenData, { headers: { 'X-User-Id': userId } }),

  // Xóa token khỏi portfolio
  removeTokenFromPortfolio: (userId, portfolioId, tokenId) =>
    apiClient.delete(`/portfolio/${portfolioId}/tokens/${tokenId}`, { headers: { 'X-User-Id': userId } }),

  // Refresh giá token trong portfolio
  refreshPortfolio: (userId, portfolioId) =>
    apiClient.put(`/portfolio/${portfolioId}/refresh`, {}, { headers: { 'X-User-Id': userId } }),

  // Xóa portfolio
  deletePortfolio: (userId, portfolioId) =>
    apiClient.delete(`/portfolio/${portfolioId}`, { headers: { 'X-User-Id': userId } })
}

// Coin API
export const coinAPI = {
  // Lấy tất cả coins
  getAllCoins: () =>
    apiClient.get('/coins'),

  // Lấy coin theo địa chỉ
  getCoinByAddress: (address) =>
    apiClient.get(`/coins/${address}`),

  // Lấy coin theo symbol
  getCoinsBySymbol: (symbol) =>
    apiClient.get(`/coins/symbol/${symbol}`),

  // Tìm kiếm coin
  searchCoins: (query) =>
    apiClient.get('/coins/search', { params: { q: query } }),

  // Tìm kiếm coin online
  searchCoinsOnline: (query) =>
    apiClient.get('/coins/search-online', { params: { q: query } }),

  // Tìm kiếm địa chỉ online
  searchAddressOnline: (address) =>
    apiClient.get('/coins/search-address-online', { params: { address } }),

  // Test search
  testSearch: () =>
    apiClient.get('/coins/test-search')
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
