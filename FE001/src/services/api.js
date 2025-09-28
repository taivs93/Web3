import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

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
  
  health: () => 
    apiClient.get('/health')
}


export const chatAPI = {
  sendMessage: (command, walletAddress, argument) => 
    apiClient.post('/chat/send', { command, walletAddress, argument }),
  
  linkAccount: (walletAddress) => 
    apiClient.post('/chat/link-account', { walletAddress }),
  
  getLinkAccount: (walletAddress) => 
    apiClient.get('/chat/link_account', { params: { walletAddress } }),
  
  health: () => 
    apiClient.get('/chat/health')
}

export default apiClient
