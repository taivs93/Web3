import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const portfolioClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
})

portfolioClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    const userId = localStorage.getItem('userId')
    if (userId) {
      config.headers['X-User-Id'] = userId
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

export const portfolioAPI = {
  createPortfolio: (data) => portfolioClient.post('/portfolio', data),
  
  getUserPortfolios: () => portfolioClient.get('/portfolio'),
  
  getPortfolio: (portfolioId) => portfolioClient.get(`/portfolio/${portfolioId}`),
  
  addToken: (data) => portfolioClient.post('/portfolio/tokens', data),
  
  removeToken: (portfolioId, tokenId) => portfolioClient.delete(`/portfolio/${portfolioId}/tokens/${tokenId}`),
  
  refreshPortfolio: (portfolioId) => portfolioClient.put(`/portfolio/${portfolioId}/refresh`),
  
  deletePortfolio: (portfolioId) => portfolioClient.delete(`/portfolio/${portfolioId}`)
}
