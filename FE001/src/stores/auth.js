import { defineStore } from 'pinia'
import { ethers } from 'ethers'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    walletAddress: null,
    tokenBalance: null,
    tokenInfo: null,
    isConnected: false,
    isLoading: false,
    error: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.user && !!state.walletAddress,
    shortAddress: (state) => {
      if (!state.walletAddress) return ''
      return `${state.walletAddress.slice(0, 6)}...${state.walletAddress.slice(-4)}`
    }
  },

  actions: {
    async connectWallet() {
      try {
        this.isLoading = true
        this.error = null

        // Kiểm tra MetaMask
        if (!window.ethereum) {
          throw new Error('MetaMask không được cài đặt!')
        }

        // Yêu cầu kết nối
        const accounts = await window.ethereum.request({
          method: 'eth_requestAccounts'
        })

        if (accounts.length === 0) {
          throw new Error('Không có tài khoản nào được chọn!')
        }

        this.walletAddress = accounts[0]
        this.isConnected = true

        return accounts[0]
      } catch (error) {
        this.error = error.message
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async signMessage(message) {
      try {
        if (!this.walletAddress) {
          throw new Error('Chưa kết nối ví!')
        }

        const provider = new ethers.BrowserProvider(window.ethereum)
        const signer = await provider.getSigner()

        const signature = await signer.signMessage(message)
        return signature
      } catch (error) {
        this.error = error.message
        throw error
      }
    },

    async login() {
      try {
        this.isLoading = true
        this.error = null

        // Kết nối ví nếu chưa kết nối
        if (!this.walletAddress) {
          await this.connectWallet()
        }

        // Lấy nonce từ server
        const nonceResponse = await axios.get(`${API_BASE_URL}/nonce`, {
          params: { address: this.walletAddress }
        })

        const nonce = nonceResponse.data.data
        const message = `Dang nhap voi Web3 Auth\nNonce: ${nonce}\nTimestamp: ${Date.now()}`

        // Sign message
        const signature = await this.signMessage(message)

        // Gửi request đăng nhập
        const response = await axios.post(`${API_BASE_URL}/login`, {
          address: this.walletAddress,
          message: message,
          signature: signature
        })

        // Backend trả về ResponseDTO với structure: { status, message, data }
        console.log('Login response:', response.data)
        
        if (response.data && response.data.data) {
          this.user = response.data.data.user
          this.walletAddress = response.data.data.walletAddress || this.walletAddress
          this.isConnected = true
          console.log('User set:', this.user)
          console.log('Wallet address set:', this.walletAddress)
          console.log('Is authenticated:', this.isAuthenticated)
        }

        return response.data.data
      } catch (error) {
        this.error = error.response?.data?.message || error.message
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async logout() {
      this.user = null
      this.walletAddress = null
      this.tokenBalance = null
      this.tokenInfo = null
      this.isConnected = false
      this.error = null
    },

    async getUserProfile() {
      try {
        if (!this.walletAddress) {
          throw new Error('Chưa kết nối ví!')
        }

        const response = await axios.get(`${API_BASE_URL}/profile`, {
          params: { address: this.walletAddress }
        })

        // Backend trả về ResponseDTO với structure: { status, message, data }
        if (response.data && response.data.data) {
          this.user = response.data.data
        }

        return response.data.data
      } catch (error) {
        this.error = error.response?.data?.message || error.message
        throw error
      }
    },

    async updateProfile(profileData) {
      try {
        this.isLoading = true
        this.error = null

        // Map field names to match backend DTO
        const requestData = {
          address: this.walletAddress,
          username: profileData.username,
          email: profileData.email,
          bio: profileData.bio
        }

        const response = await axios.put(`${API_BASE_URL}/profile`, requestData)

        // Backend trả về ResponseDTO với structure: { status, message, data }
        if (response.data && response.data.data) {
          this.user = response.data.data
        }

        return response.data.data
      } catch (error) {
        this.error = error.response?.data?.message || error.message
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // Token methods removed - not available in backend
  }
})
