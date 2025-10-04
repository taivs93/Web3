<template>
  <div 
    v-if="isAuthenticated"
    ref="widgetRef"
    class="fixed top-4 left-4 z-50 bg-white rounded-lg shadow-lg max-w-xs select-none"
    :class="isCollapsed ? 'p-2' : 'p-3'"
    :style="{ 
      transform: `translate(${position.x}px, ${position.y}px)`,
      cursor: isDragging ? 'grabbing' : 'grab'
    }"
    @mousedown="startDrag"
  >
    <!-- Header với nút thu gọn/mở rộng -->
    <div class="flex items-center justify-between mb-2">
      <div class="flex items-center space-x-2">
        <!-- Icon tia sét và symbol network -->
        <div class="flex items-center space-x-1">
          <svg class="w-4 h-4 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
          </svg>
          <span class="text-sm font-semibold text-gray-900">{{ getCurrencySymbol() }}</span>
        </div>
        
        <!-- Nút mở rộng -->
        <button 
          @click="toggleCollapse"
          class="p-1 hover:bg-gray-100 rounded transition-colors"
        >
          <svg 
            class="w-3 h-3 text-gray-500" 
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
            :style="{ transform: isCollapsed ? 'rotate(0deg)' : 'rotate(180deg)' }"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
          </svg>
        </button>
      </div>
      
      <!-- Controls chỉ hiển thị khi mở rộng -->
      <div v-if="!isCollapsed" class="flex items-center space-x-1">
        <select 
          v-model="selectedNetwork" 
          @change="fetchGasFee"
          class="px-2 py-1 border border-gray-300 rounded text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500"
        >
          <option value="bsc">BSC</option>
          <option value="ethereum">ETH</option>
          <option value="polygon">MATIC</option>
          <option value="arbitrum">ARB</option>
          <option value="optimism">OP</option>
          <option value="avalanche">AVAX</option>
          <option value="base">BASE</option>
          <option value="fantom">FTM</option>
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

    <!-- Collapsed State - Hiển thị tóm gọn -->
    <div v-if="isCollapsed && gasData" class="text-xs text-center">
      <div class="flex items-center justify-center space-x-2">
        <span class="text-red-500">{{ formatTotalFeeCompact(gasData.slow?.totalFee) }}</span>
        <span class="text-gray-400">|</span>
        <span class="text-green-500">{{ formatTotalFeeCompact(gasData.recommended?.totalFee) }}</span>
        <span class="text-gray-400">|</span>
        <span class="text-blue-500">{{ formatTotalFeeCompact(gasData.fast?.totalFee) }}</span>
        <span class="text-gray-500">-{{ getCurrencySymbol() }}</span>
      </div>
    </div>

    <!-- Expanded State - Hiển thị đầy đủ -->
    <div v-else-if="!isCollapsed">
      <!-- Loading State -->
      <div v-if="loading" class="flex items-center justify-center py-2">
        <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-indigo-600"></div>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="text-center py-2">
        <p class="text-red-600 text-xs">{{ error }}</p>
        <div class="flex space-x-1 mt-2">
          <button 
            @click="fetchGasFee"
            class="px-2 py-1 bg-red-600 text-white rounded text-xs hover:bg-red-700"
          >
            Thử lại
          </button>
          <button 
            @click="testBackendHealth"
            class="px-2 py-1 bg-blue-600 text-white rounded text-xs hover:bg-blue-700"
          >
            Test Backend
          </button>
        </div>
      </div>

      <!-- Gas Fee Display -->
      <div v-else-if="gasData" class="space-y-2">
        <!-- Network Info -->
        <div class="text-center text-xs text-gray-600">
          {{ selectedNetwork.toUpperCase() }} Network
        </div>
        
        <!-- Slow | RC | Fast -->
        <div class="space-y-1">
          <div class="flex items-center justify-between text-xs">
            <div class="flex items-center space-x-1">
              <span class="w-2 h-2 bg-red-500 rounded-full"></span>
              <span class="text-gray-600">Slow:</span>
            </div>
            <div class="text-right">
              <div class="font-medium text-red-600">{{ formatGasPrice(gasData.slow?.maxFeePerGas) }} Gwei</div>
              <div class="text-gray-500">{{ formatTotalFee(gasData.slow?.totalFee) }} {{ getCurrencySymbol() }}</div>
            </div>
          </div>
          
          <div class="flex items-center justify-between text-xs">
            <div class="flex items-center space-x-1">
              <span class="w-2 h-2 bg-green-500 rounded-full"></span>
              <span class="text-gray-600">RC:</span>
            </div>
            <div class="text-right">
              <div class="font-medium text-green-600">{{ formatGasPrice(gasData.recommended?.maxFeePerGas) }} Gwei</div>
              <div class="text-gray-500">{{ formatTotalFee(gasData.recommended?.totalFee) }} {{ getCurrencySymbol() }}</div>
            </div>
          </div>
          
          <div class="flex items-center justify-between text-xs">
            <div class="flex items-center space-x-1">
              <span class="w-2 h-2 bg-blue-500 rounded-full"></span>
              <span class="text-gray-600">Fast:</span>
            </div>
            <div class="text-right">
              <div class="font-medium text-blue-600">{{ formatGasPrice(gasData.fast?.maxFeePerGas) }} Gwei</div>
              <div class="text-gray-500">{{ formatTotalFee(gasData.fast?.totalFee) }} {{ getCurrencySymbol() }}</div>
            </div>
          </div>
        </div>
        
        <!-- Last Updated -->
        <div v-if="lastUpdated" class="text-xs text-gray-400 text-center">
          Cập nhật: {{ lastUpdated }}
        </div>
      </div>

      <!-- No Data State -->
      <div v-else class="text-center py-2 text-gray-500">
        <p class="text-xs">Chưa có dữ liệu</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { gasAPI } from '../services/api'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const selectedNetwork = ref('bsc')
const loading = ref(false)
const error = ref(null)
const gasData = ref(null)
const lastUpdated = ref('')
const isCollapsed = ref(false)
const isDragging = ref(false)
const position = ref({ x: 0, y: 0 })
const dragStart = ref({ x: 0, y: 0 })
const widgetRef = ref(null)

// Chỉ hiển thị khi đã đăng nhập
const isAuthenticated = computed(() => authStore.isAuthenticated)

// Fetch gas fee data
const fetchGasFee = async () => {
  loading.value = true
  error.value = null
  
  try {
    console.log('[GasWidget] Fetching gas data for network:', selectedNetwork.value, '(attempt 1)')
    
    // Thêm timeout cho API call
    const timeoutPromise = new Promise((_, reject) => {
      setTimeout(() => reject(new Error('Gas API timeout - backend có thể đang chậm')), 10000)
    })
    
    const response = await Promise.race([
      gasAPI.getGasEstimateQuick(selectedNetwork.value),
      timeoutPromise
    ])
    
    console.log('[GasWidget] Gas API response:', response)
    
    // Xử lý response từ Backend
    if (response.data) {
      console.log('[GasWidget] Processing gas data:', response.data)
      
      // Tạo gas data structure
      gasData.value = {
        slow: {
          maxFeePerGas: response.data.slow?.maxFeePerGas || response.data.slow?.gasPrice || '20000000000',
          totalFee: response.data.slow?.totalFee || response.data.slow?.estimatedFee || '0.00042'
        },
        recommended: {
          maxFeePerGas: response.data.recommended?.maxFeePerGas || response.data.recommended?.gasPrice || '50000000000',
          totalFee: response.data.recommended?.totalFee || response.data.recommended?.estimatedFee || '0.00105'
        },
        fast: {
          maxFeePerGas: response.data.fast?.maxFeePerGas || response.data.fast?.gasPrice || '100000000000',
          totalFee: response.data.fast?.totalFee || response.data.fast?.estimatedFee || '0.0021'
        }
      }
      
      console.log('[GasWidget] Processed gas data:', gasData.value)
    } else {
      console.log('[GasWidget] No data in response, using fallback')
      // Fallback data nếu không có response
      gasData.value = {
        slow: { maxFeePerGas: '20000000000', totalFee: '0.00042' },
        recommended: { maxFeePerGas: '50000000000', totalFee: '0.00105' },
        fast: { maxFeePerGas: '100000000000', totalFee: '0.0021' }
      }
    }
    
    lastUpdated.value = new Date().toLocaleTimeString('vi-VN')
  } catch (err) {
    console.error('[GasWidget] Error fetching gas fee:', err)
    console.error('[GasWidget] Error details:', {
      message: err.message,
      response: err.response,
      status: err.response?.status,
      code: err.code
    })
    
    // Kiểm tra nếu là lỗi CORS hoặc network
    if (err.code === 'ERR_NETWORK' || err.message.includes('CORS')) {
      error.value = 'Backend không khả dụng hoặc CORS error'
    } else {
      error.value = err.response?.data?.message || 'Không thể tải dữ liệu phí gas'
    }
    
    // Fallback data khi có lỗi
    gasData.value = {
      slow: { maxFeePerGas: '20000000000', totalFee: '0.00042' },
      recommended: { maxFeePerGas: '50000000000', totalFee: '0.00105' },
      fast: { maxFeePerGas: '100000000000', totalFee: '0.0021' }
    }
  } finally {
    loading.value = false
  }
}

// Format gas price to Gwei
const formatGasPrice = (price) => {
  if (!price) return '0'
  // Convert from Wei to Gwei (divide by 10^9)
  const gwei = parseFloat(price) / 1e9
  return gwei.toFixed(1)
}

// Format total fee
const formatTotalFee = (totalFee) => {
  if (!totalFee) return '0'
  // Convert from Wei to BNB/ETH (divide by 10^18)
  const bnb = parseFloat(totalFee) / 1e18
  return bnb.toFixed(6)
}

// Format total fee for collapsed view (shorter format)
const formatTotalFeeCompact = (totalFee) => {
  if (!totalFee) return '0'
  // Convert from Wei to BNB/ETH (divide by 10^18)
  const bnb = parseFloat(totalFee) / 1e18
  if (bnb < 0.001) {
    return bnb.toFixed(6)
  } else if (bnb < 0.01) {
    return bnb.toFixed(4)
  } else if (bnb < 0.1) {
    return bnb.toFixed(3)
  } else {
    return bnb.toFixed(2)
  }
}

// Get currency symbol based on network
const getCurrencySymbol = () => {
  switch (selectedNetwork.value) {
    case 'bsc': return 'BNB'
    case 'ethereum': return 'ETH'
    case 'polygon': return 'MATIC'
    case 'arbitrum': return 'ETH'
    case 'optimism': return 'ETH'
    case 'avalanche': return 'AVAX'
    case 'base': return 'ETH'
    case 'fantom': return 'FTM'
    default: return 'ETH'
  }
}

// Toggle collapse state
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

// Drag and drop functionality
const startDrag = (e) => {
  if (e.target.closest('button') || e.target.closest('select')) {
    return // Không kéo khi click vào button hoặc select
  }
  
  isDragging.value = true
  dragStart.value = {
    x: e.clientX - position.value.x,
    y: e.clientY - position.value.y
  }
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  e.preventDefault()
}

const onDrag = (e) => {
  if (!isDragging.value) return
  
  position.value = {
    x: e.clientX - dragStart.value.x,
    y: e.clientY - dragStart.value.y
  }
}

const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

// Watch authentication state
// Auto refresh every 30 seconds
let refreshInterval = null

// Test backend health
const testBackendHealth = async () => {
  try {
    console.log('[GasWidget] Testing backend health...')
    const healthUrl = '/api/health'
    console.log('[GasWidget] Health URL:', healthUrl)
    
    const response = await fetch(healthUrl)
    if (response.ok) {
      const data = await response.text()
      console.log('[GasWidget] Backend is healthy:', data)
      alert('Backend đang hoạt động bình thường!')
    } else {
      console.error('[GasWidget] Backend health check failed:', response.status)
      alert('Backend có vấn đề: ' + response.status)
    }
  } catch (err) {
    console.error('[GasWidget] Health check error:', err)
    alert('Không thể kết nối đến backend: ' + err.message)
  }
}

// Watch for authentication changes
watch(isAuthenticated, (newValue) => {
  if (newValue) {
    console.log('[GasWidget] Auto-refreshing gas data')
    fetchGasFee()
    
    // Set up auto refresh
    if (refreshInterval) {
      clearInterval(refreshInterval)
    }
    refreshInterval = setInterval(() => {
      console.log('[GasWidget] Auto-refreshing gas data')
      fetchGasFee()
    }, 30000)
  } else {
    // Clear interval when logged out
    if (refreshInterval) {
      clearInterval(refreshInterval)
      refreshInterval = null
    }
  }
}, { immediate: true })

onMounted(() => {
  // Initial setup is handled by the watcher above
})

// Clean up interval on unmount
onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
})
</script>
