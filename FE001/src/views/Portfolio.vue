<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Portfolio Tracker</h1>
        <p class="text-gray-600 mt-2">Manage your crypto investments and track performance</p>
      </div>

      <!-- Create Portfolio Button -->
      <div class="mb-6">
        <button
          @click="showCreateModal = true"
          class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
        >
          Create New Portfolio
        </button>
      </div>

      <!-- Portfolios Grid -->
      <div v-if="portfolios.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        <div
          v-for="portfolio in portfolios"
          :key="portfolio.id"
          class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow"
        >
          <div class="flex justify-between items-start mb-4">
            <h3 class="text-xl font-semibold text-gray-900">{{ portfolio.name }}</h3>
            <div class="flex space-x-2">
              <button
                @click="refreshPortfolio(portfolio.id)"
                class="text-gray-400 hover:text-gray-600"
                title="Refresh"
              >
                🔄
              </button>
              <button
                @click="deletePortfolio(portfolio.id)"
                class="text-red-400 hover:text-red-600"
                title="Delete"
              >
                🗑️
              </button>
            </div>
          </div>

          <div class="space-y-2">
            <div class="flex justify-between">
              <span class="text-gray-600">Total Value:</span>
              <span class="font-semibold">${{ formatNumber(portfolio.totalValue) }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">P&L:</span>
              <span :class="portfolio.totalPnl >= 0 ? 'text-green-600' : 'text-red-600'">
                ${{ formatNumber(portfolio.totalPnl) }} ({{ formatNumber(portfolio.totalPnlPercentage) }}%)
              </span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Tokens:</span>
              <span class="font-semibold">{{ portfolio.tokenCount }}</span>
            </div>
          </div>

          <div class="mt-4 pt-4 border-t">
            <button
              @click="viewPortfolio(portfolio)"
              class="w-full bg-gray-100 text-gray-700 py-2 px-4 rounded-lg hover:bg-gray-200 transition-colors"
            >
              View Details
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-12">
        <div class="text-gray-400 text-6xl mb-4">📊</div>
        <h3 class="text-xl font-semibold text-gray-900 mb-2">No Portfolios Yet</h3>
        <p class="text-gray-600 mb-4">Create your first portfolio to start tracking your crypto investments</p>
        <button
          @click="showCreateModal = true"
          class="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors"
        >
          Create Portfolio
        </button>
      </div>

      <!-- Create Portfolio Modal -->
      <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg p-6 w-full max-w-md mx-4">
          <h3 class="text-lg font-semibold mb-4">Create New Portfolio</h3>
          
          <form @submit.prevent="createPortfolio">
            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 mb-2">Portfolio Name</label>
              <input
                v-model="newPortfolio.name"
                type="text"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="My Crypto Portfolio"
              />
            </div>
            
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 mb-2">Description (Optional)</label>
              <textarea
                v-model="newPortfolio.description"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                rows="3"
                placeholder="Portfolio description..."
              ></textarea>
            </div>

            <div class="flex justify-end space-x-3">
              <button
                type="button"
                @click="showCreateModal = false"
                class="px-4 py-2 text-gray-600 hover:text-gray-800"
              >
                Cancel
              </button>
              <button
                type="submit"
                :disabled="isLoading"
                class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {{ isLoading ? 'Creating...' : 'Create' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Portfolio Details Modal -->
      <div v-if="selectedPortfolio" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg p-6 w-full max-w-4xl mx-4 max-h-[90vh] overflow-y-auto">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-xl font-semibold">{{ selectedPortfolio.name }}</h3>
            <button
              @click="selectedPortfolio = null"
              class="text-gray-400 hover:text-gray-600"
            >
              ✕
            </button>
          </div>

          <!-- Portfolio Summary -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div class="bg-gray-50 p-4 rounded-lg">
              <div class="text-sm text-gray-600">Total Value</div>
              <div class="text-2xl font-bold">${{ formatNumber(selectedPortfolio.totalValue) }}</div>
            </div>
            <div class="bg-gray-50 p-4 rounded-lg">
              <div class="text-sm text-gray-600">P&L</div>
              <div :class="selectedPortfolio.totalPnl >= 0 ? 'text-green-600' : 'text-red-600'" class="text-2xl font-bold">
                ${{ formatNumber(selectedPortfolio.totalPnl) }}
              </div>
            </div>
            <div class="bg-gray-50 p-4 rounded-lg">
              <div class="text-sm text-gray-600">Tokens</div>
              <div class="text-2xl font-bold">{{ selectedPortfolio.tokenCount }}</div>
            </div>
          </div>

          <!-- Add Token Button -->
          <div class="mb-4">
            <button
              @click="showAddTokenModal = true"
              class="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors"
            >
              Add Token
            </button>
          </div>

          <!-- Tokens Table -->
          <div v-if="selectedPortfolio.tokens && selectedPortfolio.tokens.length > 0" class="overflow-x-auto">
            <table class="w-full">
              <thead>
                <tr class="border-b">
                  <th class="text-left py-3 px-4">Token</th>
                  <th class="text-right py-3 px-4">Amount</th>
                  <th class="text-right py-3 px-4">Avg Price</th>
                  <th class="text-right py-3 px-4">Current Price</th>
                  <th class="text-right py-3 px-4">Value</th>
                  <th class="text-right py-3 px-4">P&L</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="token in selectedPortfolio.tokens" :key="token.id" class="border-b">
                  <td class="py-3 px-4">
                    <div class="font-semibold">{{ token.symbol }}</div>
                    <div class="text-sm text-gray-600">{{ token.name }}</div>
                  </td>
                  <td class="text-right py-3 px-4">{{ formatNumber(token.amount) }}</td>
                  <td class="text-right py-3 px-4">${{ formatNumber(token.averageBuyPrice) }}</td>
                  <td class="text-right py-3 px-4">${{ formatNumber(token.currentPrice) }}</td>
                  <td class="text-right py-3 px-4">${{ formatNumber(token.totalValue) }}</td>
                  <td class="text-right py-3 px-4">
                    <span :class="token.pnlAmount >= 0 ? 'text-green-600' : 'text-red-600'">
                      ${{ formatNumber(token.pnlAmount) }} ({{ formatNumber(token.pnlPercentage) }}%)
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else class="text-center py-8 text-gray-500">
            No tokens in this portfolio yet
          </div>
        </div>
      </div>

      <!-- Add Token Modal -->
      <div v-if="showAddTokenModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg p-6 w-full max-w-md mx-4">
          <h3 class="text-lg font-semibold mb-4">Add Token to Portfolio</h3>
          
          <form @submit.prevent="addToken">
            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 mb-2">Token Symbol</label>
              <input
                v-model="newToken.symbol"
                type="text"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="BTC, ETH, etc."
              />
            </div>
            
            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 mb-2">Amount</label>
              <input
                v-model="newToken.amount"
                type="number"
                step="0.00000001"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="0.5"
              />
            </div>
            
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 mb-2">Buy Price (USD)</label>
              <input
                v-model="newToken.buyPrice"
                type="number"
                step="0.01"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="45000"
              />
            </div>

            <div class="flex justify-end space-x-3">
              <button
                type="button"
                @click="showAddTokenModal = false"
                class="px-4 py-2 text-gray-600 hover:text-gray-800"
              >
                Cancel
              </button>
              <button
                type="submit"
                :disabled="isLoading"
                class="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 disabled:opacity-50"
              >
                {{ isLoading ? 'Adding...' : 'Add Token' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { portfolioAPI } from '../services/api'

const authStore = useAuthStore()

const portfolios = ref([])
const selectedPortfolio = ref(null)
const showCreateModal = ref(false)
const showAddTokenModal = ref(false)
const isLoading = ref(false)

const newPortfolio = ref({
  name: '',
  description: ''
})

const newToken = ref({
  symbol: '',
  amount: '',
  buyPrice: ''
})

const loadPortfolios = async () => {
  try {
    if (!authStore.user?.id) {
      console.error('User ID not found')
      return
    }
    
    const response = await portfolioAPI.getUserPortfolios(authStore.user.id)
    portfolios.value = response.data || []
  } catch (error) {
    console.error('Error loading portfolios:', error)
    alert('Lỗi tải danh sách portfolio: ' + (error.response?.data?.message || error.message))
  }
}

const createPortfolio = async () => {
  try {
    isLoading.value = true
    
    if (!authStore.user?.id) {
      alert('Vui lòng đăng nhập để tạo portfolio')
      return
    }
    
    const response = await portfolioAPI.createPortfolio(authStore.user.id, newPortfolio.value)
    await loadPortfolios()
    showCreateModal.value = false
    newPortfolio.value = { name: '', description: '' }
    alert('Tạo portfolio thành công!')
  } catch (error) {
    console.error('Error creating portfolio:', error)
    alert('Lỗi tạo portfolio: ' + (error.response?.data?.message || error.message))
  } finally {
    isLoading.value = false
  }
}

const addToken = async () => {
  try {
    isLoading.value = true
    
    if (!authStore.user?.id) {
      alert('Vui lòng đăng nhập để thêm token')
      return
    }
    
    const tokenData = {
      portfolioId: selectedPortfolio.value.id,
      tokenSymbol: newToken.value.symbol.toUpperCase(),
      amount: parseFloat(newToken.value.amount),
      buyPrice: parseFloat(newToken.value.buyPrice)
    }
    
    await portfolioAPI.addTokenToPortfolio(authStore.user.id, tokenData)
    await loadPortfolios()
    showAddTokenModal.value = false
    newToken.value = { symbol: '', amount: '', buyPrice: '' }
    alert('Thêm token thành công!')
  } catch (error) {
    console.error('Error adding token:', error)
    alert('Lỗi thêm token: ' + (error.response?.data?.message || error.message))
  } finally {
    isLoading.value = false
  }
}

const viewPortfolio = (portfolio) => {
  selectedPortfolio.value = portfolio
}

const refreshPortfolio = async (portfolioId) => {
  try {
    if (!authStore.user?.id) {
      alert('Vui lòng đăng nhập để refresh portfolio')
      return
    }
    
    await portfolioAPI.refreshPortfolio(authStore.user.id, portfolioId)
    await loadPortfolios()
    alert('Refresh portfolio thành công!')
  } catch (error) {
    console.error('Error refreshing portfolio:', error)
    alert('Lỗi refresh portfolio: ' + (error.response?.data?.message || error.message))
  }
}

const deletePortfolio = async (portfolioId) => {
  if (confirm('Bạn có chắc chắn muốn xóa portfolio này?')) {
    try {
      if (!authStore.user?.id) {
        alert('Vui lòng đăng nhập để xóa portfolio')
        return
      }
      
      await portfolioAPI.deletePortfolio(authStore.user.id, portfolioId)
      await loadPortfolios()
      alert('Xóa portfolio thành công!')
    } catch (error) {
      console.error('Error deleting portfolio:', error)
      alert('Lỗi xóa portfolio: ' + (error.response?.data?.message || error.message))
    }
  }
}

const formatNumber = (value) => {
  if (!value) return '0'
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 8
  }).format(value)
}

onMounted(() => {
  if (authStore.isAuthenticated) {
    loadPortfolios()
  }
})
</script>
