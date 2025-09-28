<template>
  <div class="fixed bottom-4 right-4 z-50">
    <!-- Chat Toggle Button -->
    <button
      @click="toggleChat"
      class="bg-blue-600 hover:bg-blue-700 text-white rounded-full p-4 shadow-lg transition-all duration-300 transform hover:scale-105"
      :class="{ 'animate-pulse': hasNewMessages }"
    >
      <svg v-if="!isOpen" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
      </svg>
      <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
      </svg>
    </button>

    <!-- Chat Window -->
    <div 
      v-if="isOpen" 
      class="absolute bottom-16 right-0 w-80 h-96 bg-white rounded-lg shadow-xl border border-gray-200 flex flex-col"
    >
      <!-- Chat Header -->
      <div class="bg-blue-600 text-white p-4 rounded-t-lg flex items-center justify-between">
        <div class="flex items-center space-x-2">
          <div class="w-3 h-3 bg-green-400 rounded-full"></div>
          <span class="font-semibold">Web3 Chat Bot</span>
            </div>
        <button @click="toggleChat" class="text-white hover:text-gray-200">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Messages Area -->
      <div class="flex-1 overflow-y-auto p-4 space-y-3" ref="messagesContainer">
        <div v-if="messages.length === 0" class="text-center text-gray-500 py-8">
          <svg class="w-12 h-12 mx-auto mb-2 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
          <p>Chào mừng đến với Web3 Chat Bot!</p>
          <p class="text-sm">Gửi lệnh để bắt đầu...</p>
        </div>
        
        <div
          v-for="message in messages"
          :key="message.id"
          class="flex"
          :class="message.sender === 'user' ? 'justify-end' : 'justify-start'"
        >
          <div
            class="max-w-xs px-4 py-2 rounded-lg"
            :class="message.sender === 'user' 
              ? 'bg-blue-600 text-white' 
              : 'bg-gray-100 text-gray-800'"
          >
            <p class="text-sm">{{ message.text }}</p>
            <p class="text-xs mt-1 opacity-70">{{ formatTime(message.timestamp) }}</p>
          </div>
        </div>
        
        <div v-if="isLoading" class="flex justify-start">
          <div class="bg-gray-100 text-gray-800 px-4 py-2 rounded-lg">
            <div class="flex items-center space-x-2">
              <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-600"></div>
              <span class="text-sm">Đang xử lý...</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="border-t border-gray-200 p-4">
        <div class="flex space-x-2">
          <input
            v-model="currentMessage"
            @keyup.enter="sendMessage"
            placeholder="Nhập lệnh (ví dụ: /help, /search BTC, /gas bsc)..."
            class="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            :disabled="isLoading"
          />
          <button
            @click="sendMessage"
            :disabled="!currentMessage.trim() || isLoading"
            class="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
          >
            Gửi
          </button>
        </div>
        
        <!-- Quick Commands -->
        <div class="mt-2 flex flex-wrap gap-1">
          <button
            v-for="command in quickCommands"
            :key="command"
            @click="sendQuickCommand(command)"
            class="text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 px-2 py-1 rounded transition-colors"
            :disabled="isLoading"
          >
            {{ command }}
          </button>
        </div>
        
        <!-- Profile Link -->
        <div class="mt-2 text-center">
          <button
            @click="openProfile"
            class="text-xs text-blue-600 hover:text-blue-800 underline"
          >
            Mở trang Profile để liên kết Telegram
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { chatAPI } from '@/services/api'

const authStore = useAuthStore()

// Reactive data
const isOpen = ref(false)
const currentMessage = ref('')
const messages = ref([])
const isLoading = ref(false)
const hasNewMessages = ref(false)

// Quick commands
const quickCommands = ref([
  '/start',
  '/help',
  '/follow',
  '/unfollow',
  '/list',
  '/search',
  '/gas',
  '/link'
])

// Computed
const messagesContainer = ref(null)

// Methods
const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    hasNewMessages.value = false
    nextTick(() => {
  scrollToBottom()
    })
  }
}

const sendMessage = async () => {
  if (!currentMessage.value.trim() || isLoading.value) return
  
  const messageText = currentMessage.value.trim()
  currentMessage.value = ''
  
  // Add user message
  addMessage('user', messageText)
  
  // Send to backend
  await processCommand(messageText)
}

const sendQuickCommand = (command) => {
  currentMessage.value = command
  sendMessage()
}

const openProfile = () => {
  // Close chat widget
  isOpen.value = false
  
  // Navigate to profile page
  window.location.href = '/profile'
}

const addMessage = (sender, text, data = null) => {
  const message = {
    id: Date.now() + Math.random(),
    sender,
    text,
    timestamp: new Date(),
    data
  }
  
  messages.value.push(message)
  
  if (sender === 'bot' && !isOpen.value) {
    hasNewMessages.value = true
  }
  
  nextTick(() => {
    scrollToBottom()
  })
}

const processCommand = async (command) => {
  if (!authStore.isAuthenticated) {
    addMessage('bot', 'Vui lòng đăng nhập để sử dụng chat bot!')
    return
  }
  
  isLoading.value = true
  
  try {
    // Parse command and argument
    const parts = command.trim().split(' ')
    const cmd = parts[0]
    const argument = parts.slice(1).join(' ')
    
    let response
    
    // Xử lý các command đặc biệt
    switch (cmd) {
      case '/list':
        response = await chatAPI.getFollowedAddresses(authStore.walletAddress)
        if (response.data && response.data.data) {
          const addresses = response.data.data
          if (addresses.length === 0) {
            addMessage('bot', 'Bạn chưa theo dõi địa chỉ ví nào.\n\nSử dụng /follow <địa_chỉ_ví> để bắt đầu theo dõi.')
          } else {
            addMessage('bot', 'Danh sách địa chỉ đang theo dõi:\n\n' + addresses.join('\n'))
          }
        }
        break
        
      case '/search':
        if (!argument.trim()) {
          addMessage('bot', 'Vui lòng nhập từ khóa tìm kiếm. Ví dụ: /search BTC hoặc /search Bitcoin')
          break
        }
        response = await chatAPI.searchCoins(argument, authStore.walletAddress)
        if (response.data && response.data.data) {
          addMessage('bot', response.data.data)
        }
        break
        
      case '/gas':
        response = await chatAPI.getGasEstimate(argument || 'bsc', authStore.walletAddress)
        if (response.data && response.data.data) {
          addMessage('bot', response.data.data)
        }
        break
        
      default:
        // Các command khác gửi qua sendMessage
        response = await chatAPI.sendMessage(
          cmd,
          authStore.walletAddress,
          argument,
          'bsc'
        )
        
        if (response.data && response.data.data) {
          const botResponse = response.data.data
          
          // Check if user needs to link Telegram first
          if (botResponse.message && botResponse.message.includes('not linked to Telegram')) {
            addMessage('bot', 'Bạn cần liên kết tài khoản Telegram trước!\n\nHãy click nút "Liên kết Telegram" trong trang Profile để lấy linking code.')
          } else {
            addMessage('bot', botResponse.message || 'Lệnh đã được xử lý thành công!')
          }
        } else {
          addMessage('bot', 'Không thể xử lý lệnh. Vui lòng thử lại!')
        }
        break
    }
  } catch (error) {
    console.error('Chat error:', error)
    
    // Handle specific error cases
    if (error.response?.data?.message?.includes('not linked to Telegram')) {
      addMessage('bot', 'Bạn cần liên kết tài khoản Telegram trước!\n\nHãy click nút "Liên kết Telegram" trong trang Profile để lấy linking code.')
    } else if (error.response?.data?.message?.includes('User not found')) {
      addMessage('bot', 'Không tìm thấy user. Vui lòng đăng nhập lại!')
    } else {
      addMessage('bot', 'Có lỗi xảy ra khi xử lý lệnh. Vui lòng thử lại!')
    }
  } finally {
    isLoading.value = false
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('vi-VN', {
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

onMounted(() => {
  if (authStore.isAuthenticated) {
    addMessage('bot', `Xin chào! Tôi là Web3 Chat Bot.\n\nCác lệnh có sẵn:\n/start - Lời chào mừng\n/help - Xem danh sách lệnh\n/follow <địa_chỉ_ví> - Theo dõi ví\n/unfollow <địa_chỉ_ví> - Bỏ theo dõi ví\n/list - Xem danh sách ví đang theo dõi\n/search <từ_khóa> - Tìm kiếm coin\n/gas [network] - Xem phí gas\n/link - Liên kết tài khoản Telegram\n\nLưu ý: Bạn cần liên kết Telegram trước khi sử dụng các lệnh khác!`)
  }
})
</script>