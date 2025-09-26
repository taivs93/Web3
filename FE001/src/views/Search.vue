<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
    <!-- Navigation -->
    <nav class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <router-link to="/" class="text-xl font-bold text-gray-900">Web3 Explorer</router-link>
          </div>
          <div class="flex items-center space-x-4">
            <router-link
              to="/"
              class="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
            >
              Trang chủ
            </router-link>
            <template v-if="authStore.isAuthenticated">
              <router-link
                to="/profile"
                class="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
              >
                Hồ sơ
              </router-link>
              <button
                @click="logout"
                class="bg-red-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-red-700 transition-colors"
              >
                Đăng xuất
              </button>
            </template>
            <template v-else>
              <router-link
                to="/login"
                class="bg-indigo-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-indigo-700 transition-colors"
              >
                Đăng nhập
              </router-link>
            </template>
          </div>
        </div>
      </div>
    </nav>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Search Header -->
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-4">Tìm kiếm Blockchain</h1>
        <p class="text-lg text-gray-600">Tìm kiếm blocks, transactions, addresses và tokens</p>
      </div>

      <!-- Search Form -->
      <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
        <div class="flex flex-col md:flex-row gap-4">
          <div class="flex-1">
            <input
              v-model="searchQuery"
              @keyup.enter="performSearch"
              type="text"
              placeholder="Nhập block hash, transaction hash, address hoặc token..."
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <div class="flex gap-2">
            <select
              v-model="selectedNetwork"
              class="px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="bsc">BSC</option>
              <option value="ethereum">Ethereum</option>
              <option value="arbitrum">Arbitrum</option>
              <option value="optimism">Optimism</option>
              <option value="avalanche">Avalanche</option>
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

      <!-- Gas Fee Widget -->
      <GasFeeWidget />

      <!-- Search Results -->
      <div v-if="searchResults" class="space-y-6">
        <!-- Blocks -->
        <div v-if="searchResults.blocks && searchResults.blocks.length > 0" class="bg-white rounded-lg shadow-lg p-6">
          <h3 class="text-xl font-semibold text-gray-900 mb-4 flex items-center">
            <svg class="w-5 h-5 mr-2 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
            </svg>
            Blocks ({{ searchResults.totalBlocks }})
          </h3>
          <div class="space-y-3">
            <div
              v-for="block in searchResults.blocks"
              :key="block.id"
              class="p-4 border border-gray-200 rounded-lg hover:bg-gray-50"
            >
              <div class="flex items-center justify-between">
                <div>
                  <div class="font-medium text-gray-900">Block #{{ block.blockNumber }}</div>
                  <div class="text-sm text-gray-600 font-mono">{{ block.blockHash }}</div>
                </div>
                <div class="text-right text-sm text-gray-600">
                  <div>{{ formatTimestamp(block.timestamp) }}</div>
                  <div>{{ block.transactionCount }} txs</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Transactions -->
        <div v-if="searchResults.transactions && searchResults.transactions.length > 0" class="bg-white rounded-lg shadow-lg p-6">
          <h3 class="text-xl font-semibold text-gray-900 mb-4 flex items-center">
            <svg class="w-5 h-5 mr-2 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M9 19l3 3m0 0l3-3m-3 3V10"></path>
            </svg>
            Transactions ({{ searchResults.totalTransactions }})
          </h3>
          <div class="space-y-3">
            <div
              v-for="tx in searchResults.transactions"
              :key="tx.id"
              class="p-4 border border-gray-200 rounded-lg hover:bg-gray-50"
            >
              <div class="flex items-center justify-between">
                <div class="flex-1">
                  <div class="font-mono text-sm text-gray-900 break-all">{{ tx.transactionHash }}</div>
                  <div class="text-sm text-gray-600 mt-1">
                    From: {{ tx.fromAddress }} → To: {{ tx.toAddress }}
                  </div>
                </div>
                <div class="text-right text-sm text-gray-600 ml-4">
                  <div>{{ formatTimestamp(tx.timestamp) }}</div>
                  <div>{{ formatValue(tx.value) }} BNB</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Tokens -->
        <div v-if="searchResults.tokens && searchResults.tokens.length > 0" class="bg-white rounded-lg shadow-lg p-6">
          <h3 class="text-xl font-semibold text-gray-900 mb-4 flex items-center">
            <svg class="w-5 h-5 mr-2 text-yellow-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
            </svg>
            Tokens ({{ searchResults.totalTokens }})
          </h3>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div
              v-for="token in searchResults.tokens"
              :key="token.id"
              class="p-4 border border-gray-200 rounded-lg hover:bg-gray-50"
            >
              <div class="flex items-center mb-2">
                <div class="w-8 h-8 bg-gray-200 rounded-full mr-3"></div>
                <div>
                  <div class="font-medium text-gray-900">{{ token.name }}</div>
                  <div class="text-sm text-gray-600">{{ token.symbol }}</div>
                </div>
              </div>
              <div class="text-sm text-gray-600 font-mono break-all">{{ token.tokenAddress }}</div>
            </div>
          </div>
        </div>

        <!-- No Results -->
        <div v-if="!searchResults.found" class="text-center py-12">
          <svg class="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
          </svg>
          <h3 class="text-lg font-medium text-gray-900 mb-2">Không tìm thấy kết quả</h3>
          <p class="text-gray-600">Thử tìm kiếm với từ khóa khác hoặc kiểm tra lại network</p>
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
import { searchAPI } from '@/services/api'
import GasFeeWidget from '@/components/GasFeeWidget.vue'

const router = useRouter()
const authStore = useAuthStore()

const searchQuery = ref('')
const selectedNetwork = ref('bsc')
const loading = ref(false)
const searchResults = ref(null)

const performSearch = async () => {
  if (!searchQuery.value.trim()) return
  
  loading.value = true
  searchResults.value = null
  
  try {
    const response = await searchAPI.generalSearch(
      searchQuery.value.trim(),
      selectedNetwork.value,
      0,
      20
    )
    searchResults.value = response.data
  } catch (error) {
    console.error('Search error:', error)
    // Handle error
  } finally {
    loading.value = false
  }
}

const formatTimestamp = (timestamp) => {
  if (!timestamp) return 'N/A'
  return new Date(timestamp).toLocaleString('vi-VN')
}

const formatValue = (value) => {
  if (!value) return '0'
  const bnb = parseFloat(value) / 1e18
  return bnb.toFixed(6)
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
