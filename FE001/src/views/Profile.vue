<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Loading State -->
      <div v-if="authStore.isLoading" class="text-center py-12">
        <svg class="animate-spin h-12 w-12 text-indigo-600 mx-auto" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <p class="mt-4 text-gray-600">Đang tải thông tin...</p>
      </div>

      <!-- Profile Content -->
      <div v-else class="space-y-8">

        <!-- User Profile Form -->
        <div class="bg-white shadow rounded-lg p-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-6">Thông tin cá nhân</h2>
          
          <!-- Current User Info Display -->
          <div v-if="authStore.user" class="mb-6 p-4 bg-gray-50 rounded-lg">
            <div class="flex justify-between items-center mb-3">
              <h3 class="text-sm font-medium text-gray-700">Thông tin hiện tại:</h3>
              <button
                @click="refreshProfile"
                class="px-3 py-1 bg-indigo-100 text-indigo-700 rounded-md text-sm hover:bg-indigo-200 transition-colors flex items-center space-x-1"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
                </svg>
                <span>Refresh</span>
              </button>
            </div>
            <div class="space-y-2 text-sm">
              <div><span class="font-medium">ID:</span> {{ authStore.user.id || 'Chưa có' }}</div>
              <div><span class="font-medium">Tên người dùng:</span> {{ authStore.user.username || 'Chưa có' }}</div>
              <div><span class="font-medium">Email:</span> {{ authStore.user.email || 'Chưa có' }}</div>
              <div><span class="font-medium">Telegram ID:</span> 
                <span v-if="authStore.user.telegram_user_id" class="text-blue-600">{{ authStore.user.telegram_user_id }}</span>
                <span v-else class="text-gray-500">Chưa liên kết</span>
              </div>
              <div class="flex items-center justify-between">
                <div class="flex items-center">
                  <span class="font-medium">Địa chỉ ví:</span>
                  <span class="ml-2 font-mono">{{ authStore.walletAddress }}</span>
                  <button
                    @click="copyAddress"
                    class="ml-2 text-indigo-600 hover:text-indigo-500"
                    title="Sao chép địa chỉ"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"></path>
                    </svg>
                  </button>
                </div>
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  <svg class="w-2 h-2 mr-1" fill="currentColor" viewBox="0 0 8 8">
                    <circle cx="4" cy="4" r="3" />
                  </svg>
                  Đã kết nối
                </span>
              </div>
              <!-- Linking Code -->
              <div v-if="authStore.user.telegram_user_id">
                <span class="font-medium">Linking Code:</span>
                <div class="flex items-center space-x-3 mt-1">
                  <div class="flex items-center space-x-2 px-3 py-2 bg-green-50 border border-green-200 rounded-md">
                    <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                    </svg>
                    <span class="text-sm font-medium text-green-800">Đã liên kết Telegram</span>
                  </div>
                  <button
                    @click="getLinkingCode"
                    class="px-3 py-2 bg-gray-100 text-gray-600 rounded-md text-sm font-medium hover:bg-gray-200 transition-colors"
                    title="Lấy Linking Code mới"
                  >
                    Lấy Code mới
                  </button>
                </div>
              </div>
              <div v-else>
                <span class="font-medium">Linking Code:</span>
                <button
                  @click="getLinkingCode"
                  class="ml-2 bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700 transition-colors"
                >
                  Lấy Linking Code
                </button>
              </div>
            </div>
          </div>
          
          <form @submit.prevent="updateProfile" class="space-y-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label for="username" class="block text-sm font-medium text-gray-700">Tên người dùng</label>
                <input
                  id="username"
                  v-model="form.username"
                  type="text"
                  class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  placeholder="Nhập tên người dùng"
                />
              </div>

              <div>
                <label for="email" class="block text-sm font-medium text-gray-700">Email</label>
                <input
                  id="email"
                  v-model="form.email"
                  type="email"
                  class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  placeholder="Nhập email"
                />
              </div>
            </div>


            <div class="col-span-2">
              <label for="bio" class="block text-sm font-medium text-gray-700">Giới thiệu</label>
              <textarea
                id="bio"
                v-model="form.bio"
                rows="4"
                class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                placeholder="Viết gì đó về bản thân..."
              ></textarea>
            </div>

            <div class="flex justify-end space-x-4">
              <button
                type="button"
                @click="resetForm"
                class="bg-gray-300 text-gray-700 px-4 py-2 rounded-md text-sm font-medium hover:bg-gray-400 transition-colors"
              >
                Đặt lại
              </button>
              <button
                type="submit"
                :disabled="authStore.isLoading"
                class="bg-indigo-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {{ authStore.isLoading ? 'Đang cập nhật...' : 'Cập nhật' }}
              </button>
            </div>
          </form>
        </div>

        <!-- User Stats -->
        <div class="bg-white shadow rounded-lg p-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Thống kê đơn giản</h2>
          <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div class="text-center p-4 bg-indigo-50 rounded-lg">
              <div class="text-2xl font-bold text-indigo-600">{{ authStore.user?.id || 'N/A' }}</div>
              <div class="text-sm text-gray-600">User ID</div>
            </div>
            <div class="text-center p-4 bg-green-50 rounded-lg">
              <div class="text-2xl font-bold text-green-600">
                {{ authStore.user?.lastLoginAt ? formatDate(authStore.user.lastLoginAt) : 'N/A' }}
              </div>
              <div class="text-sm text-gray-600">Lần đăng nhập cuối</div>
            </div>
            <div class="text-center p-4 bg-purple-50 rounded-lg">
              <div class="text-2xl font-bold text-purple-600">
                {{ authStore.user?.isActive ? 'Hoạt động' : 'Không hoạt động' }}
              </div>
              <div class="text-sm text-gray-600">Trạng thái</div>
            </div>
            <div class="text-center p-4 rounded-lg" :class="authStore.user?.telegramUserId ? 'bg-green-50' : 'bg-red-50'">
              <div class="text-2xl font-bold" :class="authStore.user?.telegramUserId ? 'text-green-600' : 'text-red-600'">
                {{ authStore.user?.telegramUserId ? 'Linked' : 'Not Linked' }}
              </div>
              <div class="text-sm text-gray-600">Telegram</div>
            </div>
          </div>
        </div>
      </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { chatAPI } from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  username: '',
  email: '',
  bio: ''
})

const isLoadingBalance = ref(false)
const isTelegramLinked = ref(false)

// Methods

const copyAddress = async () => {
  try {
    await navigator.clipboard.writeText(authStore.walletAddress)
    alert('Đã sao chép địa chỉ ví!')
  } catch (error) {
    console.error('Lỗi sao chép:', error)
  }
}

const updateProfile = async () => {
  try {
    await authStore.updateProfile(form.value)
    alert('Cập nhật thành công!')
  } catch (error) {
    console.error('Lỗi cập nhật:', error)
    alert('Lỗi cập nhật: ' + error.message)
  }
}

const resetForm = () => {
  if (authStore.user) {
    form.value = {
      username: authStore.user.username || '',
      email: authStore.user.email || '',
      bio: authStore.user.bio || ''
    }
  }
}

const logout = () => {
  authStore.logout()
  router.push('/')
}

const refreshProfile = async () => {
  try {
    console.log('Manually refreshing profile...')
    await authStore.getUserProfile()
    console.log('Profile refreshed manually')
    console.log('Current user after refresh:', authStore.user)
    console.log('Telegram User ID after refresh:', authStore.user?.telegram_user_id)
    alert('Đã cập nhật thông tin profile!')
  } catch (error) {
    console.error('Error refreshing profile:', error)
    alert('Lỗi khi cập nhật profile: ' + error.message)
  }
}

const getLinkingCode = async () => {
  try {
    const response = await chatAPI.getLinkingCode()
    if (response.data && response.data.data) {
      const code = response.data.data.code
      await navigator.clipboard.writeText(code)
      alert(`Đã sao chép linking code: ${code}`)
    }
  } catch (error) {
    console.error('Error getting linking code:', error)
    alert('Lỗi khi lấy linking code: ' + error.message)
  }
}


const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  try {
    return new Date(dateString).toLocaleDateString('vi-VN')
  } catch (error) {
    return 'N/A'
  }
}

// Watch for user changes and update form
watch(() => authStore.user, (newUser) => {
  if (newUser) {
    form.value = {
      username: newUser.username || '',
      email: newUser.email || '',
      bio: newUser.bio || ''
    }
    // Update Telegram linking status
    isTelegramLinked.value = !!newUser.telegram_user_id
  }
}, { immediate: true })

onMounted(async () => {
  // Kiểm tra nếu chưa đăng nhập thì chuyển về login
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  // Luôn refresh profile để đảm bảo thông tin Telegram được cập nhật
  try {
    console.log('Refreshing profile to sync Telegram status...')
    await authStore.getUserProfile()
    console.log('Profile refreshed successfully')
    console.log('Current user data:', authStore.user)
  } catch (error) {
    console.error('Lỗi tải profile:', error)
    // Không redirect về login nếu có lỗi, chỉ log lỗi
  }
})

// Lắng nghe sự kiện từ chat để refresh profile khi Telegram được link
window.addEventListener('telegram-linked', async () => {
  console.log('Telegram linked event received, refreshing profile...')
  try {
    await authStore.getUserProfile()
    console.log('Profile refreshed after Telegram link')
  } catch (error) {
    console.error('Error refreshing profile after Telegram link:', error)
  }
})
</script>
