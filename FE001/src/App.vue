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
import { useAuthStore } from './stores/auth'
import AppLayout from './components/AppLayout.vue'

const isInitializing = ref(true)
const authStore = useAuthStore()

onMounted(async () => {
  console.log('App mounted, starting auth initialization...')
  
  // Timeout để tránh loading quá lâu
  const timeoutId = setTimeout(() => {
    console.log('Auth initialization timeout, ending loading...')
    isInitializing.value = false
  }, 3000) // 3 giây timeout
  
  try {
    // Khôi phục ngay lập tức
    const restored = authStore.restoreFromLocalStorage()
    console.log('Immediate restore result:', restored)
    
    if (restored && authStore.walletAddress) {
      // Kiểm tra MetaMask connection với timeout
      if (window.ethereum) {
        try {
          const accountsPromise = window.ethereum.request({ method: 'eth_accounts' })
          const timeoutPromise = new Promise((_, reject) => 
            setTimeout(() => reject(new Error('MetaMask timeout')), 2000)
          )
          
          const accounts = await Promise.race([accountsPromise, timeoutPromise])
          
          if (accounts.length > 0 && accounts[0].toLowerCase() === authStore.walletAddress.toLowerCase()) {
            // Ví vẫn kết nối, load profile với timeout
            try {
              const profilePromise = authStore.getUserProfile()
              const profileTimeoutPromise = new Promise((_, reject) => 
                setTimeout(() => reject(new Error('Profile load timeout')), 2000)
              )
              
              await Promise.race([profilePromise, profileTimeoutPromise])
              console.log('Profile loaded successfully')
            } catch (profileError) {
              console.warn('Profile load failed, but keeping auth state:', profileError.message)
            }
          } else {
            console.log('Wallet disconnected, logging out')
            authStore.logout()
          }
        } catch (ethError) {
          console.warn('MetaMask check failed, keeping auth state:', ethError.message)
        }
      }
    }
    
    console.log('Auth initialization completed')
  } catch (error) {
    console.warn('Auth initialization error:', error)
  } finally {
    // Clear timeout và kết thúc loading
    clearTimeout(timeoutId)
    console.log('Setting isInitializing to false')
    console.log('Final auth state:', {
      isAuthenticated: authStore.isAuthenticated,
      walletAddress: authStore.walletAddress,
      user: authStore.user
    })
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
