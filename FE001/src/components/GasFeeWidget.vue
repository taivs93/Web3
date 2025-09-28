<template>
  <div class="fixed top-4 left-4 z-50 bg-white rounded-lg shadow-lg p-3 max-w-xs">
    <div class="flex items-center justify-between mb-2">
      <h3 class="text-sm font-semibold text-gray-900 flex items-center">
        <svg class="w-4 h-4 mr-1 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
        </svg>
        Gas {{ selectedNetwork.toUpperCase() }}
      </h3>
      <div class="flex items-center space-x-1">
        <select 
          v-model="selectedNetwork" 
          @change="fetchGasFee"
          class="px-2 py-1 border border-gray-300 rounded text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500"
        >
          <option value="bsc">BSC</option>
          <option value="ethereum">ETH</option>
          <option value="arbitrum">ARB</option>
          <option value="optimism">OP</option>
          <option value="avalanche">AVAX</option>
        </select>
        <button 
          @click="fetchGasFee"
          :disabled="loading"
          class="p-1 bg-indigo-600 text-white rounded text-xs hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <svg v-if="loading" class="w-3 h-3 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          <svg v-else class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-2">
      <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-indigo-600"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="text-center py-2">
      <p class="text-red-600 text-xs">{{ error }}</p>
      <button 
        @click="fetchGasFee"
        class="mt-1 px-2 py-1 bg-red-600 text-white rounded text-xs hover:bg-red-700"
      >
        Thử lại
      </button>
    </div>

    <!-- Gas Fee Display -->
    <div v-else-if="gasData" class="space-y-1">
      <!-- Slow | RC | Fast -->
      <div class="flex items-center justify-between text-xs">
        <div class="flex items-center space-x-1">
          <span class="w-2 h-2 bg-red-500 rounded-full"></span>
          <span class="text-gray-600">Slow:</span>
          <span class="font-medium text-red-600">{{ formatGasPrice(gasData.slow?.maxFeePerGas) }}</span>
        </div>
        <div class="flex items-center space-x-1">
          <span class="w-2 h-2 bg-green-500 rounded-full"></span>
          <span class="text-gray-600">RC:</span>
          <span class="font-medium text-green-600">{{ formatGasPrice(gasData.recommended?.maxFeePerGas) }}</span>
        </div>
        <div class="flex items-center space-x-1">
          <span class="w-2 h-2 bg-blue-500 rounded-full"></span>
          <span class="text-gray-600">Fast:</span>
          <span class="font-medium text-blue-600">{{ formatGasPrice(gasData.fast?.maxFeePerGas) }}</span>
        </div>
      </div>
      
      <!-- Total fees -->
      <div class="text-xs text-gray-500 text-center">
        <span class="text-red-500">{{ formatTotalFee(gasData.slow?.totalFee) }}</span> | 
        <span class="text-green-500">{{ formatTotalFee(gasData.recommended?.totalFee) }}</span> | 
        <span class="text-blue-500">{{ formatTotalFee(gasData.fast?.totalFee) }}</span>
        <span class="ml-1">{{ getCurrencySymbol() }}</span>
      </div>
    </div>

    <!-- No Data State -->
    <div v-else class="text-center py-2 text-gray-500">
      <p class="text-xs">Chưa có dữ liệu</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { gasAPI } from '@/services/api'

const selectedNetwork = ref('bsc')
const loading = ref(false)
const error = ref(null)
const gasData = ref(null)
const lastUpdated = ref('')

// Fetch gas fee data
const fetchGasFee = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await gasAPI.getGasEstimateQuick(selectedNetwork.value)
    gasData.value = response.data
    lastUpdated.value = new Date().toLocaleTimeString('vi-VN')
  } catch (err) {
    console.error('Error fetching gas fee:', err)
    error.value = err.response?.data?.message || 'Không thể tải dữ liệu phí gas'
  } finally {
    loading.value = false
  }
}

// Format gas price to Gwei
const formatGasPrice = (price) => {
  if (!price) return '0'
  // Convert from Wei to Gwei (divide by 10^9)
  const gwei = parseFloat(price) / 1e9
  return gwei.toFixed(2)
}

// Format total fee
const formatTotalFee = (totalFee) => {
  if (!totalFee) return '0'
  // Convert from Wei to BNB/ETH (divide by 10^18)
  const bnb = parseFloat(totalFee) / 1e18
  return bnb.toFixed(6)
}

// Get currency symbol based on network
const getCurrencySymbol = () => {
  switch (selectedNetwork.value) {
    case 'bsc': return 'BNB'
    case 'ethereum': return 'ETH'
    case 'arbitrum': return 'ETH'
    case 'optimism': return 'ETH'
    case 'avalanche': return 'AVAX'
    default: return 'BNB'
  }
}

// Auto refresh every 30 seconds
let refreshInterval = null

onMounted(() => {
  fetchGasFee()
  
  // Set up auto refresh
  refreshInterval = setInterval(() => {
    fetchGasFee()
  }, 30000)
})

// Clean up interval on unmount
onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>
