<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Quản lý Ví</h1>
        <p class="text-gray-600 mt-2">Theo dõi và quản lý các địa chỉ ví quan trọng</p>
      </div>

      <!-- Add Wallet Form -->
      <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Thêm ví để theo dõi</h2>
        <form @submit.prevent="addWallet" class="flex flex-col md:flex-row gap-4">
          <div class="flex-1">
            <input
              v-model="newWalletAddress"
              type="text"
              placeholder="Nhập địa chỉ ví (0x...)"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              required
            />
          </div>
          <button
            type="submit"
            :disabled="isLoading || !newWalletAddress.trim()"
            class="px-6 py-3 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {{ isLoading ? 'Đang thêm...' : 'Thêm ví' }}
          </button>
        </form>
      </div>

      <!-- Followed Wallets -->
      <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-lg font-semibold text-gray-900">Ví đang theo dõi</h2>
          <button
            @click="refreshWallets"
            :disabled="isLoading"
            class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 disabled:opacity-50 transition-colors"
          >
            🔄 Làm mới
          </button>
        </div>

        <!-- Loading State -->
        <div v-if="isLoading && followedWallets.length === 0" class="text-center py-8">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600 mx-auto"></div>
          <p class="mt-2 text-gray-600">Đang tải danh sách ví...</p>
        </div>

        <!-- Wallet List -->
        <div v-else-if="followedWallets.length > 0" class="space-y-4">
          <div
            v-for="wallet in followedWallets"
            :key="wallet"
            class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div class="flex items-center space-x-4">
              <div class="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-bold">
                {{ wallet.charAt(2).toUpperCase() }}
              </div>
              <div>
                <div class="font-mono text-sm text-gray-900">{{ wallet }}</div>
                <div class="text-xs text-gray-500">Địa chỉ ví</div>
              </div>
            </div>
            <div class="flex items-center space-x-2">
              <button
                @click="copyAddress(wallet)"
                class="p-2 text-gray-400 hover:text-gray-600 transition-colors"
                title="Sao chép địa chỉ"
              >
                📋
              </button>
              <button
                @click="removeWallet(wallet)"
                class="p-2 text-red-400 hover:text-red-600 transition-colors"
                title="Bỏ theo dõi"
              >
                🗑️
              </button>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else class="text-center py-12">
          <div class="text-gray-400 text-6xl mb-4">👛</div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">Chưa theo dõi ví nào</h3>
          <p class="text-gray-600 mb-4">Thêm địa chỉ ví để bắt đầu theo dõi hoạt động</p>
        </div>
      </div>

      <!-- Global Followed Wallets -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-lg font-semibold text-gray-900">Ví được theo dõi toàn cầu</h2>
          <button
            @click="refreshGlobalWallets"
            :disabled="isLoadingGlobal"
            class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 disabled:opacity-50 transition-colors"
          >
            🔄 Làm mới
          </button>
        </div>

        <!-- Loading State -->
        <div v-if="isLoadingGlobal && globalWallets.length === 0" class="text-center py-8">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600 mx-auto"></div>
          <p class="mt-2 text-gray-600">Đang tải danh sách ví toàn cầu...</p>
        </div>

        <!-- Global Wallet List -->
        <div v-else-if="globalWallets.length > 0" class="space-y-4">
          <div
            v-for="wallet in globalWallets"
            :key="wallet"
            class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div class="flex items-center space-x-4">
              <div class="w-10 h-10 bg-gradient-to-br from-green-500 to-teal-600 rounded-full flex items-center justify-center text-white font-bold">
                {{ wallet.charAt(2).toUpperCase() }}
              </div>
              <div>
                <div class="font-mono text-sm text-gray-900">{{ wallet }}</div>
                <div class="text-xs text-gray-500">Ví toàn cầu</div>
              </div>
            </div>
            <div class="flex items-center space-x-2">
              <button
                @click="copyAddress(wallet)"
                class="p-2 text-gray-400 hover:text-gray-600 transition-colors"
                title="Sao chép địa chỉ"
              >
                📋
              </button>
              <button
                @click="followWallet(wallet)"
                class="px-3 py-1 bg-indigo-600 text-white text-xs rounded-lg hover:bg-indigo-700 transition-colors"
              >
                Theo dõi
              </button>
            </div>
          </div>
        </div>

        <!-- Empty Global State -->
        <div v-else class="text-center py-12">
          <div class="text-gray-400 text-6xl mb-4">🌍</div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">Không có ví toàn cầu</h3>
          <p class="text-gray-600">Chưa có ví nào được theo dõi toàn cầu</p>
        </div>
      </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { walletAPI } from '@/services/api'

const authStore = useAuthStore()

const newWalletAddress = ref('')
const followedWallets = ref([])
const globalWallets = ref([])
const isLoading = ref(false)
const isLoadingGlobal = ref(false)

// Load followed wallets
const loadFollowedWallets = async () => {
  try {
    isLoading.value = true
    
    // Sử dụng chatId = 1 làm default (có thể thay đổi theo user)
    const response = await walletAPI.getFollowedAddresses(1)
    followedWallets.value = response.data.data || []
  } catch (error) {
    console.error('Error loading followed wallets:', error)
    alert('Lỗi tải danh sách ví: ' + (error.response?.data?.message || error.message))
  } finally {
    isLoading.value = false
  }
}

// Load global wallets
const loadGlobalWallets = async () => {
  try {
    isLoadingGlobal.value = true
    
    const response = await walletAPI.getGlobalFollowedAddresses()
    globalWallets.value = response.data.data || []
  } catch (error) {
    console.error('Error loading global wallets:', error)
    alert('Lỗi tải danh sách ví toàn cầu: ' + (error.response?.data?.message || error.message))
  } finally {
    isLoadingGlobal.value = false
  }
}

// Add wallet to follow
const addWallet = async () => {
  if (!newWalletAddress.value.trim()) return
  
  try {
    isLoading.value = true
    
    // Sử dụng chatId = 1 làm default
    await walletAPI.followWallet(1, newWalletAddress.value.trim())
    
    newWalletAddress.value = ''
    await loadFollowedWallets()
    alert('Thêm ví thành công!')
  } catch (error) {
    console.error('Error adding wallet:', error)
    alert('Lỗi thêm ví: ' + (error.response?.data?.message || error.message))
  } finally {
    isLoading.value = false
  }
}

// Follow wallet from global list
const followWallet = async (address) => {
  try {
    isLoading.value = true
    
    await walletAPI.followWallet(1, address)
    await loadFollowedWallets()
    alert('Theo dõi ví thành công!')
  } catch (error) {
    console.error('Error following wallet:', error)
    alert('Lỗi theo dõi ví: ' + (error.response?.data?.message || error.message))
  } finally {
    isLoading.value = false
  }
}

// Remove wallet from follow list
const removeWallet = async (address) => {
  if (!confirm('Bạn có chắc chắn muốn bỏ theo dõi ví này?')) return
  
  try {
    isLoading.value = true
    
    await walletAPI.unfollowWallet(1, address)
    await loadFollowedWallets()
    alert('Bỏ theo dõi ví thành công!')
  } catch (error) {
    console.error('Error removing wallet:', error)
    alert('Lỗi bỏ theo dõi ví: ' + (error.response?.data?.message || error.message))
  } finally {
    isLoading.value = false
  }
}

// Copy address to clipboard
const copyAddress = async (address) => {
  try {
    await navigator.clipboard.writeText(address)
    alert('Đã sao chép địa chỉ ví!')
  } catch (error) {
    console.error('Copy error:', error)
    alert('Lỗi sao chép địa chỉ!')
  }
}

// Refresh functions
const refreshWallets = () => {
  loadFollowedWallets()
}

const refreshGlobalWallets = () => {
  loadGlobalWallets()
}

onMounted(() => {
  if (authStore.isAuthenticated) {
    loadFollowedWallets()
    loadGlobalWallets()
  } else {
    alert('Vui lòng đăng nhập để sử dụng chức năng này!')
  }
})
</script>
