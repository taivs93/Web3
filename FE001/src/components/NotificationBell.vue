<template>
  <div class="relative">
    <!-- Notification Bell Button -->
    <button
      @click="toggleNotifications"
      class="relative p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-full transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500"
    >
      <!-- Bell Icon -->
      <svg 
        class="w-6 h-6 transition-transform duration-200" 
        :class="{ 'animate-pulse': hasNewNotifications }"
        fill="none" 
        stroke="currentColor" 
        viewBox="0 0 24 24"
      >
        <path 
          stroke-linecap="round" 
          stroke-linejoin="round" 
          stroke-width="2" 
          d="M15 17h5l-5 5v-5zM4.828 7l2.586 2.586a2 2 0 002.828 0L12 7H4.828zM4.828 17l2.586-2.586a2 2 0 012.828 0L12 17H4.828zM15 7h5l-5-5v5z"
        />
        <path 
          stroke-linecap="round" 
          stroke-linejoin="round" 
          stroke-width="2" 
          d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"
        />
        <path 
          stroke-linecap="round" 
          stroke-linejoin="round" 
          stroke-width="2" 
          d="M12 6v6l4 2"
        />
      </svg>
      
      <!-- Notification Badge -->
      <span 
        v-if="unreadCount > 0" 
        class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center animate-bounce"
      >
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
      
      <!-- Pulse Ring for New Notifications -->
      <span 
        v-if="hasNewNotifications" 
        class="absolute -top-1 -right-1 w-6 h-6 bg-red-400 rounded-full animate-ping opacity-75"
      ></span>
    </button>

    <!-- Notifications Dropdown -->
    <div 
      v-if="showNotifications" 
      class="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-xl border border-gray-200 z-50 max-h-96 overflow-hidden"
    >
      <!-- Header -->
      <div class="px-4 py-3 border-b border-gray-200 bg-gray-50">
        <div class="flex items-center justify-between">
          <h3 class="text-sm font-semibold text-gray-900">Thông báo</h3>
          <div class="flex items-center space-x-2">
            <button 
              @click="markAllAsRead"
              v-if="unreadCount > 0"
              class="text-xs text-blue-600 hover:text-blue-800"
            >
              Đọc tất cả
            </button>
            <button 
              @click="clearAllNotifications"
              class="text-xs text-gray-500 hover:text-gray-700"
            >
              Xóa tất cả
            </button>
          </div>
        </div>
      </div>

      <!-- Notifications List -->
      <div class="max-h-80 overflow-y-auto">
        <div v-if="notifications.length === 0" class="p-4 text-center text-gray-500">
          <svg class="w-12 h-12 mx-auto mb-2 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-5 5v-5zM4.828 7l2.586 2.586a2 2 0 002.828 0L12 7H4.828zM4.828 17l2.586-2.586a2 2 0 012.828 0L12 17H4.828zM15 7h5l-5-5v5z"></path>
          </svg>
          <p class="text-sm">Chưa có thông báo nào</p>
        </div>
        
        <div v-else>
          <div 
            v-for="notification in notifications" 
            :key="notification.id"
            @click="markAsRead(notification.id)"
            class="px-4 py-3 border-b border-gray-100 hover:bg-gray-50 cursor-pointer transition-colors"
            :class="{ 'bg-blue-50': !notification.read }"
          >
            <div class="flex items-start space-x-3">
              <!-- Notification Icon -->
              <div class="flex-shrink-0">
                <div 
                  class="w-8 h-8 rounded-full flex items-center justify-center"
                  :class="getNotificationIconClass(notification.type)"
                >
                  <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                    <path :d="getNotificationIcon(notification.type)"></path>
                  </svg>
                </div>
              </div>
              
              <!-- Notification Content -->
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-gray-900 truncate">
                  {{ notification.title }}
                </p>
                <p class="text-sm text-gray-600 mt-1">
                  {{ notification.message }}
                </p>
                <div class="flex items-center justify-between mt-2">
                  <p class="text-xs text-gray-500">
                    {{ formatTime(notification.timestamp) }}
                  </p>
                  <div v-if="!notification.read" class="w-2 h-2 bg-blue-500 rounded-full"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-4 py-2 border-t border-gray-200 bg-gray-50">
        <button 
          @click="viewAllNotifications"
          class="w-full text-center text-sm text-blue-600 hover:text-blue-800"
        >
          Xem tất cả thông báo
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'
import { websocketAPI } from '@/services/api'

const authStore = useAuthStore()

// Reactive data
const showNotifications = ref(false)
const notifications = ref([])
const hasNewNotifications = ref(false)
const stompClient = ref(null)
const isConnected = ref(false)

// Computed properties
const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.read).length
})

// WebSocket connection
const connectWebSocket = () => {
  try {
    const socket = new SockJS(websocketAPI.getWebSocketUrl())
    stompClient.value = Stomp.over(socket)
    
    stompClient.value.connect({}, (frame) => {
      console.log('Connected to WebSocket:', frame)
      isConnected.value = true
      
      // Subscribe to wallet activity notifications
      stompClient.value.subscribe('/topic/wallet-activity', (message) => {
        const data = JSON.parse(message.body)
        addNotification({
          id: Date.now() + Math.random(),
          type: 'wallet',
          title: 'Hoạt động ví mới',
          message: `Có giao dịch mới từ địa chỉ ${data.fromAddress?.substring(0, 6)}...`,
          timestamp: new Date(),
          read: false,
          data: data
        })
      })
    }, (error) => {
      console.error('WebSocket connection error:', error)
      isConnected.value = false
    })
  } catch (error) {
    console.error('Failed to connect to WebSocket:', error)
  }
}

// Notification methods
const addNotification = (notification) => {
  notifications.value.unshift(notification)
  hasNewNotifications.value = true
  
  // Auto-hide new notification indicator after 3 seconds
  setTimeout(() => {
    hasNewNotifications.value = false
  }, 3000)
  
  // Keep only last 50 notifications
  if (notifications.value.length > 50) {
    notifications.value = notifications.value.slice(0, 50)
  }
}

const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value
  if (showNotifications.value) {
    hasNewNotifications.value = false
  }
}

const markAsRead = (notificationId) => {
  const notification = notifications.value.find(n => n.id === notificationId)
  if (notification) {
    notification.read = true
  }
}

const markAllAsRead = () => {
  notifications.value.forEach(notification => {
    notification.read = true
  })
}

const clearAllNotifications = () => {
  notifications.value = []
}

const viewAllNotifications = () => {
  // In a real app, this would navigate to a full notifications page
  console.log('View all notifications')
  showNotifications.value = false
}

// Utility functions
const getNotificationIcon = (type) => {
  const icons = {
    wallet: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z',
    chat: 'M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z',
    system: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
    error: 'M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
    success: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z'
  }
  return icons[type] || icons.system
}

const getNotificationIconClass = (type) => {
  const classes = {
    wallet: 'bg-blue-100 text-blue-600',
    chat: 'bg-green-100 text-green-600',
    system: 'bg-gray-100 text-gray-600',
    error: 'bg-red-100 text-red-600',
    success: 'bg-green-100 text-green-600'
  }
  return classes[type] || classes.system
}

const formatTime = (timestamp) => {
  const now = new Date()
  const diff = now - timestamp
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return 'Vừa xong'
  if (minutes < 60) return `${minutes} phút trước`
  if (hours < 24) return `${hours} giờ trước`
  if (days < 7) return `${days} ngày trước`
  
  return timestamp.toLocaleDateString('vi-VN')
}

// Click outside to close
const handleClickOutside = (event) => {
  if (!event.target.closest('.relative')) {
    showNotifications.value = false
  }
}

// Lifecycle
onMounted(() => {
  // Add some sample notifications for demo
  addNotification({
    id: 1,
    type: 'system',
    title: 'Chào mừng!',
    message: 'Hệ thống thông báo đã sẵn sàng',
    timestamp: new Date(Date.now() - 1000 * 60 * 5), // 5 minutes ago
    read: false
  })
  
  addNotification({
    id: 2,
    type: 'wallet',
    title: 'Giao dịch thành công',
    message: 'Bạn đã nhận 0.1 ETH từ 0x1234...5678',
    timestamp: new Date(Date.now() - 1000 * 60 * 30), // 30 minutes ago
    read: true
  })
  
  // Connect to WebSocket
  if (authStore.isAuthenticated) {
    connectWebSocket()
  }
  
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  if (stompClient.value) {
    stompClient.value.disconnect()
  }
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
/* Custom animations */
@keyframes pulse-ring {
  0% {
    transform: scale(0.33);
  }
  40%, 50% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: scale(1.2);
  }
}

.animate-pulse-ring {
  animation: pulse-ring 1.25s cubic-bezier(0.215, 0.61, 0.355, 1) infinite;
}

/* Smooth transitions */
.transition-all {
  transition: all 0.2s ease-in-out;
}

/* Custom scrollbar for notifications */
.max-h-80::-webkit-scrollbar {
  width: 4px;
}

.max-h-80::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.max-h-80::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.max-h-80::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>