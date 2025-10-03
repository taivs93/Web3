<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Navigation -->
    <nav class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <router-link 
              to="/" 
              class="text-xl font-bold text-gray-900 hover:text-indigo-600 transition-colors duration-200 flex items-center space-x-2"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
              </svg>
              <span>Web3 Chat Bot</span>
            </router-link>
          </div>
          <div class="flex items-center space-x-4">
            <template v-if="authStore.isAuthenticated">
              <router-link
                to="/dashboard"
                class="text-gray-600 hover:text-indigo-600 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 hover:bg-indigo-50 hover:scale-105 transform flex items-center space-x-2 hover:bounce"
                :class="{ 'text-indigo-600 bg-indigo-100 shadow-md': $route.name === 'dashboard' }"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
                </svg>
                <span>Dashboard</span>
              </router-link>
              <router-link
                to="/portfolio"
                class="text-gray-600 hover:text-indigo-600 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 hover:bg-indigo-50 hover:scale-105 transform flex items-center space-x-2 hover:bounce"
                :class="{ 'text-indigo-600 bg-indigo-100 shadow-md': $route.name === 'portfolio' }"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
                </svg>
                <span>Portfolio</span>
              </router-link>
              <router-link
                to="/wallet"
                class="text-gray-600 hover:text-indigo-600 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 hover:bg-indigo-50 hover:scale-105 transform flex items-center space-x-2 hover:bounce"
                :class="{ 'text-indigo-600 bg-indigo-100 shadow-md': $route.name === 'wallet' }"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"></path>
                </svg>
                <span>Ví</span>
              </router-link>
              <router-link
                to="/search"
                class="text-gray-600 hover:text-indigo-600 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 hover:bg-indigo-50 hover:scale-105 transform flex items-center space-x-2 hover:bounce"
                :class="{ 'text-indigo-600 bg-indigo-100 shadow-md': $route.name === 'search' }"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                </svg>
                <span>Tìm kiếm</span>
              </router-link>
              <router-link
                to="/profile"
                class="text-gray-600 hover:text-indigo-600 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 hover:bg-indigo-50 hover:scale-105 transform flex items-center space-x-2 hover:bounce"
                :class="{ 'text-indigo-600 bg-indigo-100 shadow-md': $route.name === 'profile' }"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
                <span>Hồ sơ</span>
              </router-link>
              <router-link
                to="/admin"
                class="text-gray-600 hover:text-indigo-600 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 hover:bg-indigo-50 hover:scale-105 transform flex items-center space-x-2 hover:bounce"
                :class="{ 'text-indigo-600 bg-indigo-100 shadow-md': $route.name === 'admin' }"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                </svg>
                <span>Admin</span>
              </router-link>
              <div class="flex items-center space-x-2 px-3 py-1 bg-gray-100 rounded-lg">
                <svg class="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"></path>
                </svg>
                <span class="text-sm text-gray-600 font-mono">{{ authStore.shortAddress }}</span>
              </div>
              <NotificationBell />
              <button
                @click="logout"
                class="bg-red-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-red-700 hover:scale-105 transition-all duration-200 transform flex items-center space-x-2"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
                </svg>
                <span>Đăng xuất</span>
              </button>
            </template>
            <template v-else>
              <router-link
                to="/login"
                class="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 hover:scale-105 transition-all duration-200 transform flex items-center space-x-2"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"></path>
                </svg>
                <span>Đăng nhập</span>
              </router-link>
            </template>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="min-h-screen">
      <slot />
    </main>

    <!-- Gas Fee Widget - Chỉ hiển thị khi đã đăng nhập -->
    <GasFeeWidget />
    
    <!-- Chat Widget - Chỉ hiển thị khi đã đăng nhập -->
    <ChatWidget />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import NotificationBell from './NotificationBell.vue'
import GasFeeWidget from './GasFeeWidget.vue'
import ChatWidget from './ChatWidget.vue'

const router = useRouter()
const authStore = useAuthStore()

const logout = () => {
  authStore.logout()
  router.push('/')
}

onMounted(() => {
  console.log('AppLayout mounted')
  console.log('Auth state in AppLayout:', {
    isAuthenticated: authStore.isAuthenticated,
    user: authStore.user,
    walletAddress: authStore.walletAddress
  })
})
</script>

<style scoped>
/* Hiệu ứng hover mượt mà cho navigation */
.router-link-active {
  @apply text-indigo-600 bg-indigo-100 shadow-md;
}

/* Hiệu ứng ripple khi click */
.router-link-active::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(99, 102, 241, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.router-link-active:active::after {
  width: 300px;
  height: 300px;
}

/* Hiệu ứng bounce cho các icon */
@keyframes bounce {
  0%, 20%, 53%, 80%, 100% {
    transform: translate3d(0,0,0);
  }
  40%, 43% {
    transform: translate3d(0, -8px, 0);
  }
  70% {
    transform: translate3d(0, -4px, 0);
  }
  90% {
    transform: translate3d(0, -2px, 0);
  }
}

.hover\:bounce:hover svg {
  animation: bounce 1s;
}

/* Hiệu ứng glow cho active state */
.router-link-active {
  box-shadow: 0 0 20px rgba(99, 102, 241, 0.3);
}

/* Hiệu ứng gradient cho background */
nav {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
}
</style>
