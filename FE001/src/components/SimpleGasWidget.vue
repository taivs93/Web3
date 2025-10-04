<template>
  <div v-if="isAuthenticated" class="fixed top-4 left-4 z-50 bg-white rounded-lg shadow-lg p-3 max-w-xs">
    <div class="flex items-center justify-between mb-2">
      <h3 class="text-sm font-semibold text-gray-900">Gas Widget</h3>
      <button @click="testAPI" class="px-2 py-1 bg-blue-600 text-white rounded text-xs">
        Test API
      </button>
    </div>
    
    <div v-if="loading" class="text-center py-2">
      <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600 mx-auto"></div>
    </div>
    
    <div v-else-if="error" class="text-center py-2">
      <p class="text-red-600 text-xs">{{ error }}</p>
      <button @click="testAPI" class="mt-1 px-2 py-1 bg-red-600 text-white rounded text-xs">
        Thử lại
      </button>
    </div>
    
    <div v-else-if="data" class="text-xs">
      <p class="text-green-600">API hoạt động!</p>
      <p class="text-gray-600">{{ data }}</p>
    </div>
    
    <div v-else class="text-center py-2 text-gray-500">
      <p class="text-xs">Click "Test API" để kiểm tra</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { gasAPI } from '../services/api'

const authStore = useAuthStore()
const loading = ref(false)
const error = ref(null)
const data = ref(null)

const isAuthenticated = computed(() => authStore.isAuthenticated)

const testAPI = async () => {
  loading.value = true
  error.value = null
  data.value = null
  
  try {
    console.log('Testing gas API...')
    
    // Test health endpoint first (using proxy)
    const healthResponse = await fetch('/api/health')
    if (!healthResponse.ok) {
      throw new Error(`Health check failed: ${healthResponse.status}`)
    }
    
    const healthData = await healthResponse.text()
    console.log('Health check passed:', healthData)
    
    // Test gas API
    const gasResponse = await gasAPI.getGasEstimateQuick('bsc')
    console.log('Gas API response:', gasResponse)
    
    data.value = `Health: OK, Gas API: ${gasResponse.status}`
    
  } catch (err) {
    console.error('API test error:', err)
    error.value = err.message
  } finally {
    loading.value = false
  }
}
</script>
