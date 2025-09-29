import { defineStore } from 'pinia'
import { ethers } from 'ethers'
import { authAPI } from '../services/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    walletAddress: null,
    isConnected: false,
    isLoading: false,
    error: null
  }),

  // Khôi phục state từ localStorage khi khởi tạo
  hydrate(state, initialState) {
    if (typeof window !== 'undefined') {
      const saved = localStorage.getItem('auth-store')
      if (saved) {
        const parsed = JSON.parse(saved)
        state.user = parsed.user
        state.walletAddress = parsed.walletAddress
        state.isConnected = parsed.isConnected
      }
    }
  },

  getters: {
    isAuthenticated: (state) => !!state.user && !!state.walletAddress,
    shortAddress: (state) => {
      if (!state.walletAddress) return ''
      return `${state.walletAddress.slice(0, 6)}...${state.walletAddress.slice(-4)}`
    }
  },

  actions: {
    // Lưu state vào localStorage
    saveToLocalStorage() {
      if (typeof window !== 'undefined') {
        const state = {
          user: this.user,
          walletAddress: this.walletAddress,
          isConnected: this.isConnected
        }
        localStorage.setItem('auth-store', JSON.stringify(state))
      }
    },

    // Xóa dữ liệu từ localStorage
    clearFromLocalStorage() {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('auth-store')
      }
    },

    // Khôi phục từ localStorage
    restoreFromLocalStorage() {
      if (typeof window !== 'undefined') {
        const saved = localStorage.getItem('auth-store')
        if (saved) {
          try {
            const parsed = JSON.parse(saved)
            this.user = parsed.user
            this.walletAddress = parsed.walletAddress
            this.isConnected = parsed.isConnected
            return true
          } catch (error) {
            console.error('Error parsing saved auth data:', error)
            this.clearFromLocalStorage()
            return false
          }
        }
      }
      return false
    },

    async connectWallet() {
      try {
        this.isLoading = true
        this.error = null

        if (!window.ethereum) {
          throw new Error('MetaMask không được cài đặt!')
        }

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

        if (!this.walletAddress) {
          await this.connectWallet()
        }

        
        const nonceResponse = await authAPI.getNonce(this.walletAddress)
        const nonce = nonceResponse.data.data
        const message = `Dang nhap voi Web3 Auth\nNonce: ${nonce}\nTimestamp: ${Date.now()}`

        
        const signature = await this.signMessage(message)

        
        const response = await authAPI.login(this.walletAddress, message, signature)

        if (response.data && response.data.data) {
          this.user = response.data.data.user
          this.walletAddress = response.data.data.walletAddress || this.walletAddress
          this.isConnected = true
          
          // Lưu vào localStorage
          this.saveToLocalStorage()
          
          // Hiển thị thông báo thành công
          console.log('Đăng nhập thành công!')
        }

        return response.data.data
      } catch (error) {
        this.error = error.response?.data?.message || error.message
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async getUserProfile() {
      try {
        this.isLoading = true
        this.error = null

        if (!this.walletAddress) {
          throw new Error('Chưa kết nối ví!')
        }

        const response = await authAPI.getProfile(this.walletAddress)
        
        if (response.data && response.data.data) {
          this.user = response.data.data
          
          // Lưu vào localStorage
          this.saveToLocalStorage()
        }

        return response.data.data
      } catch (error) {
        this.error = error.response?.data?.message || error.message
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async updateProfile(profileData) {
      try {
        this.isLoading = true
        this.error = null

        if (!this.walletAddress) {
          throw new Error('Chưa kết nối ví!')
        }

        const response = await authAPI.updateProfile(
          this.walletAddress,
          profileData.username,
          profileData.email,
          profileData.avatarUrl,
          profileData.bio
        )

        if (response.data && response.data.data) {
          this.user = response.data.data
          
          // Lưu vào localStorage
          this.saveToLocalStorage()
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
      this.isConnected = false
      this.error = null
      
      // Xóa khỏi localStorage
      this.clearFromLocalStorage()
      
      // Hiển thị thông báo đăng xuất
      console.log('Đã đăng xuất!')
    }
  }
})
