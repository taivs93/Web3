<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Search Header -->
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-4">Tìm kiếm Coin & Token</h1>
        <p class="text-lg text-gray-600">Tìm kiếm coin, token theo tên, symbol hoặc địa chỉ contract</p>
      </div>

      <!-- Search Form -->
      <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
        <div class="flex flex-col md:flex-row gap-4">
          <div class="flex-1">
            <input
              v-model="searchQuery"
              @keyup.enter="performSearch"
              type="text"
              placeholder="Nhập tên coin, symbol (BTC, ETH) hoặc địa chỉ contract..."
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <div class="flex gap-2">
            <select
              v-model="searchType"
              class="px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="search">Tìm kiếm tổng quát</option>
              <option value="symbol">Tìm theo Symbol</option>
              <option value="address">Tìm theo địa chỉ</option>
              <option value="online">Tìm kiếm online</option>
            </select>
            <button
              @click="performSearch"
              :disabled="loading || !searchQuery.trim()"
              class="px-6 py-3 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              <svg v-if="loading" class="w-5 h-5 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
              </svg>
              <span v-else>Tìm kiếm</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Search Results -->
      <div v-if="searchResults" class="space-y-6">
        <!-- Coins -->
        <div v-if="searchResults.length > 0" class="bg-white rounded-lg shadow-lg p-6">
          <h3 class="text-xl font-semibold text-gray-900 mb-4 flex items-center">
            <svg class="w-5 h-5 mr-2 text-yellow-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
            </svg>
            Kết quả tìm kiếm ({{ searchResults.length }})
          </h3>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div
              v-for="coin in searchResults"
              :key="coin.id"
              class="p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer"
              @click="selectCoin(coin)"
            >
              <div class="flex items-start justify-between mb-3">
                <div class="flex items-center">
                  <div class="w-10 h-10 bg-gradient-to-br from-yellow-400 to-orange-500 rounded-full mr-3 flex items-center justify-center text-white font-bold">
                    {{ coin.symbol?.charAt(0) || '?' }}
                  </div>
                  <div>
                    <div class="font-medium text-gray-900">{{ coin.name || 'Unknown' }}</div>
                    <div class="text-sm text-gray-600 font-mono">{{ coin.symbol || 'N/A' }}</div>
                  </div>
                </div>
                <div class="text-right">
                  <div v-if="coin.currentPrice" class="text-sm font-medium text-gray-900">
                    ${{ formatPrice(coin.currentPrice) }}
                  </div>
                  <div v-if="coin.priceChangePercentage24h" 
                       :class="coin.priceChangePercentage24h >= 0 ? 'text-green-600' : 'text-red-600'"
                       class="text-xs">
                    {{ coin.priceChangePercentage24h >= 0 ? '+' : '' }}{{ formatPrice(coin.priceChangePercentage24h) }}%
                  </div>
                </div>
              </div>
              
              <div class="space-y-2 text-sm">
                <div v-if="coin.address" class="flex items-center">
                  <span class="text-gray-600 w-16">Address:</span>
                  <span class="font-mono text-gray-900 break-all">{{ coin.address }}</span>
                  <button @click.stop="copyAddress(coin.address)" class="ml-2 text-gray-400 hover:text-gray-600">
                    📋
                  </button>
                </div>
                <div v-if="coin.marketCap" class="flex items-center">
                  <span class="text-gray-600 w-16">Market Cap:</span>
                  <span class="text-gray-900">${{ formatNumber(coin.marketCap) }}</span>
                </div>
                <div v-if="coin.volume24h" class="flex items-center">
                  <span class="text-gray-600 w-16">Volume 24h:</span>
                  <span class="text-gray-900">${{ formatNumber(coin.volume24h) }}</span>
                </div>
                <div v-if="coin.totalSupply" class="flex items-center">
                  <span class="text-gray-600 w-16">Supply:</span>
                  <span class="text-gray-900">{{ formatNumber(coin.totalSupply) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- No Results -->
        <div v-else class="text-center py-12">
          <svg class="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
          </svg>
          <h3 class="text-lg font-medium text-gray-900 mb-2">Không tìm thấy coin nào</h3>
          <p class="text-gray-600">Thử tìm kiếm với từ khóa khác hoặc thay đổi loại tìm kiếm</p>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="text-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">Đang tìm kiếm...</p>
      </div>
      </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { coinAPI } from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()

const searchQuery = ref('')
const searchType = ref('search')
const loading = ref(false)
const searchResults = ref(null)

const performSearch = async () => {
  if (!searchQuery.value.trim()) return
  
  loading.value = true
  searchResults.value = null
  
  try {
    let response
    
    switch (searchType.value) {
      case 'symbol':
        response = await coinAPI.getCoinsBySymbol(searchQuery.value.trim().toUpperCase())
        break
      case 'address':
        response = await coinAPI.getCoinByAddress(searchQuery.value.trim())
        searchResults.value = response.data ? [response.data] : []
        loading.value = false
        return
      case 'online':
        response = await coinAPI.searchCoinsOnline(searchQuery.value.trim())
        break
      default:
        response = await coinAPI.searchCoins(searchQuery.value.trim())
    }
    
    searchResults.value = response.data || []
  } catch (error) {
    console.error('Search error:', error)
    alert('Lỗi tìm kiếm: ' + (error.response?.data?.message || error.message))
    searchResults.value = []
  } finally {
    loading.value = false
  }
}

const selectCoin = (coin) => {
  // Có thể mở modal chi tiết coin hoặc chuyển đến trang chi tiết
  console.log('Selected coin:', coin)
  alert(`Đã chọn coin: ${coin.name} (${coin.symbol})`)
}

const copyAddress = async (address) => {
  try {
    await navigator.clipboard.writeText(address)
    alert('Đã sao chép địa chỉ!')
  } catch (error) {
    console.error('Copy error:', error)
  }
}

const formatPrice = (price) => {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(6)
}

const formatNumber = (value) => {
  if (!value) return '0'
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2
  }).format(value)
}

const logout = () => {
  authStore.logout()
  router.push('/')
}

onMounted(() => {
  // Auto focus search input
  const searchInput = document.querySelector('input[type="text"]')
  if (searchInput) {
    searchInput.focus()
  }
})
</script>
