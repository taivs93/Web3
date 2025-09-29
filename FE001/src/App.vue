<template>
  <div id="app">
    <!-- Loading state khi đang khôi phục trạng thái đăng nhập -->
    <div v-if="isInitializing" class="min-h-screen flex items-center justify-center bg-gray-100">
      <div class="text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto mb-4"></div>
        <p class="text-gray-600">Đang khôi phục trạng thái đăng nhập...</p>
      </div>
    </div>
    
    <!-- Nội dung chính với AppLayout -->
    <AppLayout v-else>
      <router-view />
    </AppLayout>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthInit } from './composables/useAuthInit'
import AppLayout from './components/AppLayout.vue'

const isInitializing = ref(true)

onMounted(async () => {
  console.log('App mounted, starting auth initialization...')
  try {
    // Khởi tạo trạng thái đăng nhập với timeout
    const initPromise = useAuthInit()
    const timeoutPromise = new Promise((_, reject) => 
      setTimeout(() => reject(new Error('Timeout')), 5000)
    )
    
    await Promise.race([initPromise, timeoutPromise])
    console.log('Auth initialization completed successfully')
  } catch (error) {
    console.warn('Auth initialization timeout or error:', error)
  } finally {
    // Kết thúc loading
    console.log('Setting isInitializing to false')
    isInitializing.value = false
  }
})
</script>

<style>
/* Global styles */
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  min-height: 100vh;
}

/* Custom scrollbar */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
