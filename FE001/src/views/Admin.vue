<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Admin Panel</h1>
        <p class="text-gray-600 mt-2">Quản lý hệ thống và theo dõi trạng thái</p>
      </div>

      <!-- System Status -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <!-- Services Health -->
        <div class="bg-white rounded-lg shadow-lg p-6">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-xl font-semibold text-gray-900">Services Health</h2>
            <button
              @click="checkServicesHealth"
              :disabled="loadingHealth"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
            >
              <svg v-if="loadingHealth" class="w-5 h-5 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
              </svg>
              <span v-else>Check Health</span>
            </button>
          </div>

          <div v-if="healthStatus" class="space-y-3">
            <div
              v-for="(status, service) in healthStatus"
              :key="service"
              class="flex items-center justify-between p-3 rounded-lg"
              :class="status === 'OK' ? 'bg-green-50' : 'bg-red-50'"
            >
              <div class="flex items-center space-x-3">
                <div
                  class="w-3 h-3 rounded-full"
                  :class="status === 'OK' ? 'bg-green-500' : 'bg-red-500'"
                ></div>
                <span class="font-medium text-gray-900 capitalize">{{ service }}</span>
              </div>
              <span
                class="text-sm font-medium"
                :class="status === 'OK' ? 'text-green-600' : 'text-red-600'"
              >
                {{ status }}
              </span>
            </div>
          </div>

          <div v-else-if="loadingHealth" class="text-center py-4">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
            <p class="mt-2 text-gray-600">Checking services...</p>
          </div>

          <div v-else class="text-center py-4 text-gray-500">
            Click "Check Health" to load status
          </div>
        </div>

        <!-- System Actions -->
        <div class="bg-white rounded-lg shadow-lg p-6">
          <h2 class="text-xl font-semibold text-gray-900 mb-4">System Actions</h2>
          <div class="space-y-3">
            <button
              @click="triggerPortfolioUpdate"
              :disabled="loadingActions"
              class="w-full px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 transition-colors flex items-center justify-center space-x-2"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
              </svg>
              <span>Update Portfolio Prices</span>
            </button>

            <button
              @click="clearAllCaches"
              :disabled="loadingActions"
              class="w-full px-4 py-2 bg-orange-600 text-white rounded-lg hover:bg-orange-700 disabled:opacity-50 transition-colors flex items-center justify-center space-x-2"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
              </svg>
              <span>Clear All Caches</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Enhanced Services -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <!-- Enhanced Gas Price -->
        <div class="bg-white rounded-lg shadow-lg p-6">
          <h2 class="text-xl font-semibold text-gray-900 mb-4">Enhanced Gas Price</h2>
          <div class="space-y-4">
            <div class="flex space-x-2">
              <select
                v-model="selectedNetwork"
                class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="ethereum">Ethereum</option>
                <option value="bsc">BSC</option>
                <option value="arbitrum">Arbitrum</option>
                <option value="optimism">Optimism</option>
                <option value="avalanche">Avalanche</option>
              </select>
              <button
                @click="loadEnhancedGasPrice"
                :disabled="loadingEnhancedGas"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
              >
                <svg v-if="loadingEnhancedGas" class="w-5 h-5 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
                </svg>
                <span v-else>Load</span>
              </button>
            </div>

            <div v-if="enhancedGasData" class="p-4 bg-blue-50 rounded-lg">
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <div class="text-sm text-gray-600">Gas Price (Wei)</div>
                  <div class="text-lg font-semibold text-gray-900">{{ enhancedGasData.gasPriceWei }}</div>
                </div>
                <div>
                  <div class="text-sm text-gray-600">Gas Price (Gwei)</div>
                  <div class="text-lg font-semibold text-gray-900">{{ enhancedGasData.gasPriceGwei }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Enhanced Token Prices -->
        <div class="bg-white rounded-lg shadow-lg p-6">
          <h2 class="text-xl font-semibold text-gray-900 mb-4">Enhanced Token Prices</h2>
          <div class="space-y-4">
            <div class="flex space-x-2">
              <input
                v-model="tokenSymbols"
                type="text"
                placeholder="BTC,ETH,BNB (comma separated)"
                class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <button
                @click="loadEnhancedTokenPrices"
                :disabled="loadingEnhancedTokens"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
              >
                <svg v-if="loadingEnhancedTokens" class="w-5 h-5 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
                </svg>
                <span v-else>Load</span>
              </button>
            </div>

            <div v-if="enhancedTokenPrices" class="space-y-2">
              <div
                v-for="(price, symbol) in enhancedTokenPrices"
                :key="symbol"
                class="flex justify-between items-center p-3 bg-gray-50 rounded-lg"
              >
                <span class="font-medium text-gray-900">{{ symbol }}</span>
                <span class="text-lg font-semibold text-green-600">${{ formatPrice(price) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Actions Log -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <h2 class="text-xl font-semibold text-gray-900 mb-4">Recent Actions</h2>
        <div class="space-y-2">
          <div
            v-for="(action, index) in recentActions"
            :key="index"
            class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
          >
            <div class="flex items-center space-x-3">
              <div class="w-2 h-2 bg-green-500 rounded-full"></div>
              <span class="text-gray-900">{{ action.message }}</span>
            </div>
            <span class="text-sm text-gray-500">{{ formatTime(action.timestamp) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminAPI } from '../services/api'

const healthStatus = ref(null)
const enhancedGasData = ref(null)
const enhancedTokenPrices = ref(null)
const recentActions = ref([])

const loadingHealth = ref(false)
const loadingActions = ref(false)
const loadingEnhancedGas = ref(false)
const loadingEnhancedTokens = ref(false)

const selectedNetwork = ref('ethereum')
const tokenSymbols = ref('BTC,ETH,BNB')

const checkServicesHealth = async () => {
  loadingHealth.value = true
  try {
    const response = await adminAPI.checkServicesHealth()
    healthStatus.value = response.data.data
    addAction('Services health check completed')
  } catch (error) {
    console.error('Error checking health:', error)
    addAction('Health check failed: ' + error.message, 'error')
  } finally {
    loadingHealth.value = false
  }
}

const triggerPortfolioUpdate = async () => {
  loadingActions.value = true
  try {
    await adminAPI.triggerPortfolioPriceUpdate()
    addAction('Portfolio prices update triggered successfully')
  } catch (error) {
    console.error('Error triggering portfolio update:', error)
    addAction('Portfolio update failed: ' + error.message, 'error')
  } finally {
    loadingActions.value = false
  }
}

const clearAllCaches = async () => {
  loadingActions.value = true
  try {
    await adminAPI.clearAllCaches()
    addAction('All caches cleared successfully')
  } catch (error) {
    console.error('Error clearing caches:', error)
    addAction('Cache clear failed: ' + error.message, 'error')
  } finally {
    loadingActions.value = false
  }
}

const loadEnhancedGasPrice = async () => {
  loadingEnhancedGas.value = true
  try {
    const response = await adminAPI.getEnhancedGasPrice(selectedNetwork.value)
    enhancedGasData.value = response.data.data
    addAction(`Enhanced gas price loaded for ${selectedNetwork.value}`)
  } catch (error) {
    console.error('Error loading enhanced gas price:', error)
    addAction('Enhanced gas price load failed: ' + error.message, 'error')
  } finally {
    loadingEnhancedGas.value = false
  }
}

const loadEnhancedTokenPrices = async () => {
  loadingEnhancedTokens.value = true
  try {
    const symbols = tokenSymbols.value.split(',').map(s => s.trim()).filter(s => s)
    const response = await adminAPI.getEnhancedTokenPrices(symbols)
    enhancedTokenPrices.value = response.data.data
    addAction(`Enhanced token prices loaded for ${symbols.join(', ')}`)
  } catch (error) {
    console.error('Error loading enhanced token prices:', error)
    addAction('Enhanced token prices load failed: ' + error.message, 'error')
  } finally {
    loadingEnhancedTokens.value = false
  }
}

const addAction = (message, type = 'success') => {
  const action = {
    message,
    type,
    timestamp: new Date()
  }
  recentActions.value.unshift(action)
  
  // Keep only last 10 actions
  if (recentActions.value.length > 10) {
    recentActions.value = recentActions.value.slice(0, 10)
  }
}

const formatPrice = (price) => {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  addAction('Admin panel loaded')
})
</script>
