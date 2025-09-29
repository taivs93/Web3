<template>
  <div class="min-h-screen bg-gray-100 flex items-center justify-center">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Đăng nhập Web3
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          Kết nối ví MetaMask để bắt đầu
        </p>
      </div>
      
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <div v-if="authStore.isLoading" class="text-center">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
          <p class="mt-4 text-gray-600">Đang xử lý...</p>
        </div>
        
        <div v-else>
          <button
            @click="handleLogin"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 0C5.374 0 0 5.373 0 12s5.374 12 12 12 12-5.373 12-12S18.626 0 12 0zm5.568 8.16c-.169 1.858-.896 6.728-.896 6.728-.896 6.728-1.268 7.893-2.965 7.893-.897 0-1.596-.83-1.596-1.854 0-.896.598-1.567 1.326-2.295.728-.728 1.567-1.326 2.295-1.326.83 0 1.854.699 1.854 1.596 0 1.697-1.165 2.069-7.893 2.965 0 0-4.87.727-6.728.896-1.675.152-2.965-.598-2.965-2.965 0-1.858 1.29-3.117 2.965-2.965z"/>
            </svg>
            Kết nối MetaMask
          </button>
          
          <div v-if="authStore.error" class="mt-4 p-4 bg-red-50 border border-red-200 rounded-md">
            <p class="text-sm text-red-600">{{ authStore.error }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const handleLogin = async () => {
  try {
    await authStore.login()
    router.push('/dashboard')
  } catch (error) {
    console.error('Login error:', error)
  }
}
</script>
