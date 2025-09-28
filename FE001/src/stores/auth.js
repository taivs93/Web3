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
    }
  }
})
