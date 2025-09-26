<template>
  <div class="bg-white rounded-lg shadow-lg p-6 mb-6">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-semibold text-gray-900 flex items-center">
        <svg class="w-5 h-5 mr-2 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
        </svg>
        Phí Gas {{ selectedNetwork.toUpperCase() }}
      </h3>
      <div class="flex items-center space-x-2">
        <select 
          v-model="selectedNetwork" 
          @change="fetchGasFee"
          class="px-3 py-1 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        >
          <option value="bsc">BSC</option>
          <option value="ethereum">Ethereum</option>
          <option value="arbitrum">Arbitrum</option>
          <option value="optimism">Optimism</option>
          <option value="avalanche">Avalanche</option>
        </select>
        <button 
          @click="fetchGasFee"
          :disabled="loading"
          class="px-3 py-1 bg-indigo-600 text-white rounded-md text-sm hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <svg v-if="loading" class="w-4 h-4 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          <span v-else>Làm mới</span>
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-8">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="text-center py-8">
      <div class="text-red-500 mb-2">
        <svg class="w-8 h-8 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
      </div>
      <p class="text-red-600 font-medium">{{ error }}</p>
      <button 
        @click="fetchGasFee"
        class="mt-2 px-4 py-2 bg-red-600 text-white rounded-md text-sm hover:bg-red-700"
      >
        Thử lại
      </button>
    </div>

    <!-- Gas Fee Display -->
    <div v-else-if="gasData" class="space-y-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <!-- Slow -->
        <div class="bg-red-50 border border-red-200 rounded-lg p-4">
          <div class="flex items-center justify-between mb-2">
            <h4 class="font-medium text-red-800">Chậm</h4>
            <span class="text-xs bg-red-100 text-red-800 px-2 py-1 rounded">~5 phút</span>
          </div>
          <div class="space-y-1">
            <div class="text-sm text-gray-600">
              <span class="font-medium">{{ formatGasPrice(gasData.slow?.maxFeePerGas) }}</span> Gwei
            </div>
            <div class="text-sm text-gray-600">
              Tổng: <span class="font-medium">{{ formatTotalFee(gasData.slow?.totalFee) }}</span> BNB
            </div>
          </div>
        </div>

        <!-- Recommended -->
        <div class="bg-green-50 border border-green-200 rounded-lg p-4">
          <div class="flex items-center justify-between mb-2">
            <h4 class="font-medium text-green-800">Khuyến nghị</h4>
            <span class="text-xs bg-green-100 text-green-800 px-2 py-1 rounded">~2 phút</span>
          </div>
          <div class="space-y-1">
            <div class="text-sm text-gray-600">
              <span class="font-medium">{{ formatGasPrice(gasData.recommended?.maxFeePerGas) }}</span> Gwei
            </div>
            <div class="text-sm text-gray-600">
              Tổng: <span class="font-medium">{{ formatTotalFee(gasData.recommended?.totalFee) }}</span> BNB
            </div>
          </div>
        </div>

        <!-- Fast -->
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div class="flex items-center justify-between mb-2">
            <h4 class="font-medium text-blue-800">Nhanh</h4>
            <span class="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">~30 giây</span>
          </div>
          <div class="space-y-1">
            <div class="text-sm text-gray-600">
              <span class="font-medium">{{ formatGasPrice(gasData.fast?.maxFeePerGas) }}</span> Gwei
            </div>
            <div class="text-sm text-gray-600">
              Tổng: <span class="font-medium">{{ formatTotalFee(gasData.fast?.totalFee) }}</span> BNB
            </div>
          </div>
        </div>
      </div>

      <!-- Additional Info -->
      <div class="mt-4 p-3 bg-gray-50 rounded-lg">
        <div class="flex items-center justify-between text-sm text-gray-600">
          <span>Cập nhật lần cuối: {{ lastUpdated }}</span>
          <span>Gas Limit: {{ gasData.slow?.gasLimit || '21,000' }}</span>
        </div>
      </div>
    </div>

    <!-- No Data State -->
    <div v-else class="text-center py-8 text-gray-500">
      <svg class="w-8 h-8 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
      </svg>
      <p>Chưa có dữ liệu phí gas</p>
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
