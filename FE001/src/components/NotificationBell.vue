<template>
  <div class="relative">
    <button
      @click="toggleNotifications"
      class="relative px-3 py-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-md transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500"
    >
      <span class="text-sm font-medium">
        Thông báo
        <span 
          v-if="unreadCount > 0" 
          class="ml-1 bg-red-500 text-white text-xs rounded-full px-2 py-1"
        >
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </span>
    </button>

    <div 
      v-if="showNotifications" 
      class="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-xl border border-gray-200 z-50 max-h-96 overflow-hidden"
    >
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

      <div class="max-h-80 overflow-y-auto">
        <div v-if="notifications.length === 0" class="p-4 text-center text-gray-500">
          <p class="text-sm">Chưa có thông báo nào</p>
        </div>
        
        <div v-else>
          <div 
            v-for="notification in notifications" 
            :key="notification.id"
            class="border-b border-gray-100 transition-all duration-200"
            :class="{ 'bg-blue-50': !notification.read }"
          >
            <div 
              @click="toggleNotificationExpansion(notification.id)"
              class="px-4 py-3 hover:bg-gray-50 cursor-pointer transition-colors"
            >
              <div class="flex items-start space-x-3">
                <!-- Notification Content -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between">
                    <p class="text-sm font-medium text-gray-900 truncate">
                      {{ notification.title }}
                    </p>
                    <div class="flex items-center space-x-2">
                      <div v-if="!notification.read" class="w-2 h-2 bg-blue-500 rounded-full"></div>
                    </div>
                  </div>
                  <p class="text-sm text-gray-600 mt-1">
                    {{ notification.message }}
                  </p>
                  <p class="text-xs text-gray-500 mt-2">
                    {{ formatTime(notification.timestamp) }}
                  </p>
                  
                         <div v-if="expandedNotifications.includes(notification.id) && notification.fullData" class="mt-3 p-3 bg-gray-50 rounded-lg">
                           <div class="text-xs text-gray-700 space-y-1">
                             <div><strong>Network:</strong> {{ notification.fullData.networkAsString || notification.fullData.network || 'Unknown' }}</div>
                             <div><strong>From:</strong> {{ notification.fullData.fromAddress || 'Unknown' }}</div>
                             <div><strong>To:</strong> {{ notification.fullData.toAddress || 'Unknown' }}</div>
                             <div><strong>Value:</strong> {{ notification.fullData.valueFormatted || (notification.fullData.value ? (parseFloat(notification.fullData.value) / Math.pow(10, 18)).toFixed(4) : '0') }} {{ notification.fullData.currency || 'ETH' }}</div>
                             <div><strong>Hash:</strong> {{ notification.fullData.transactionHash || 'Unknown' }}</div>
                             <div><strong>Block:</strong> {{ notification.fullData.blockNumber || 'Unknown' }}</div>
                             <div><strong>Gas Price:</strong> {{ notification.fullData.gasPrice || 'Unknown' }}</div>
                           </div>
                         </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'


const showNotifications = ref(false)
const notifications = ref([])
const hasNewNotifications = ref(false)
const expandedNotifications = ref([])


const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.read).length
})


const addNotification = (notification) => {
  notifications.value.unshift(notification)
  hasNewNotifications.value = true
  
  
  setTimeout(() => {
    hasNewNotifications.value = false
  }, 3000)
  
  
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

const markAllAsRead = () => {
  notifications.value.forEach(notification => {
    notification.read = true
  })
}

const clearAllNotifications = () => {
  notifications.value = []
}

const toggleNotificationExpansion = (notificationId) => {
  const index = expandedNotifications.value.indexOf(notificationId)
  if (index > -1) {
    expandedNotifications.value.splice(index, 1)
  } else {
    expandedNotifications.value.push(notificationId)
  }
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


const handleClickOutside = (event) => {
  if (!event.target.closest('.relative')) {
    showNotifications.value = false
  }
}

const handleWebSocketNotification = (event) => {
  const { type, data } = event.detail
  
  let notification = null
  
  switch (type) {
    case 'notification':
      notification = {
        id: Date.now() + Math.random(),
        type: 'system',
        title: 'Thông báo hệ thống',
        message: data,
        timestamp: new Date(),
        read: false
      }
      break
    case 'wallet-activity':
      const networkName = data.networkAsString || data.network || 'Unknown'
      const fromAddress = data.fromAddress || 'Unknown'
      const toAddress = data.toAddress || 'Unknown'
      const value = data.valueFormatted || (data.value ? (parseFloat(data.value) / Math.pow(10, 18)).toFixed(4) : '0')
      const currency = data.currency || 'ETH'
      
      notification = {
        id: Date.now() + Math.random(),
        type: 'wallet',
        title: `Hoạt động ví ${networkName}`,
        message: `${fromAddress.slice(0, 6)}...${fromAddress.slice(-4)} → ${toAddress.slice(0, 6)}...${toAddress.slice(-4)} (${value} ${currency})`,
        timestamp: new Date(),
        read: false,
        fullData: data
      }
      break
    case 'price-alert':
      notification = {
        id: Date.now() + Math.random(),
        type: 'price',
        title: 'Cảnh báo giá',
        message: `${data.symbol} = $${data.price}`,
        timestamp: new Date(),
        read: false
      }
      break
    case 'portfolio-update':
      notification = {
        id: Date.now() + Math.random(),
        type: 'portfolio',
        title: 'Cập nhật Portfolio',
        message: `Portfolio "${data.portfolioName}" đã được cập nhật`,
        timestamp: new Date(),
        read: false
      }
      break
    case 'gas-update':
      notification = {
        id: Date.now() + Math.random(),
        type: 'gas',
        title: 'Cập nhật Gas Fee',
        message: `Gas fee ${data.network} đã được cập nhật`,
        timestamp: new Date(),
        read: false
      }
      break
    case 'text':
      notification = {
        id: Date.now() + Math.random(),
        type: 'text',
        title: 'Tin nhắn mới',
        message: data,
        timestamp: new Date(),
        read: false
      }
      break
  }
  
  if (notification) {
    addNotification(notification)
  }
}

onMounted(() => {
  window.addEventListener('websocket-notification', handleWebSocketNotification)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  window.removeEventListener('websocket-notification', handleWebSocketNotification)
  document.removeEventListener('click', handleClickOutside)
})
</script>
