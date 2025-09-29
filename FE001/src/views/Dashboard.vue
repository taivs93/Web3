<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-2">
          <div class="bg-white rounded-lg shadow-lg mb-6">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-semibold text-gray-900">Thông tin tài khoản</h2>
            </div>
            <div class="p-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Địa chỉ ví</label>
                    <p class="text-sm text-gray-900 font-mono">{{ authStore.walletAddress || 'Chưa kết nối' }}</p>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Trạng thái Telegram</label>
                    <span v-if="!isTelegramLinked" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                      Chưa liên kết
                    </span>
                    <span v-else class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                      Đã liên kết
                    </span>
                  </div>
                </div>
                <div class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Linking Code</label>
                    <button
                      @click="getLinkingCode"
                      class="bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700"
                    >
                      Lấy Linking Code
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="lg:col-span-1">
          <div class="bg-white rounded-lg shadow-lg sticky top-4">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-semibold text-gray-900">Chat Bot</h2>
            </div>
            <div class="p-6">
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Quick Commands</label>
                  <div class="space-y-2">
                    <button
                      v-for="command in quickCommands"
                      :key="command"
                      @click="sendQuickCommand(command)"
                      class="w-full text-left px-3 py-2 text-sm bg-gray-100 hover:bg-gray-200 rounded-md transition-colors"
                    >
                      {{ command }}
                    </button>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Chat</label>
                  <div class="border rounded-lg p-4 h-64 overflow-y-auto bg-gray-50" ref="messagesContainer">
                    <div
                      v-for="message in messages"
                      :key="message.id"
                      class="mb-3"
                    >
                      <div class="flex items-start space-x-2">
                        <div class="flex-1">
                          <div class="flex items-center space-x-2 mb-1">
                            <span class="text-xs font-medium text-gray-600">
                              {{ message.sender === 'user' ? 'Bạn' : 'Bot' }}
                            </span>
                            <span class="text-xs text-gray-500">
                              {{ formatTime(message.timestamp) }}
                            </span>
                          </div>
                          <div class="text-sm text-gray-900 whitespace-pre-wrap">
                            {{ message.text }}
                          </div>
                        </div>
                      </div>
                    </div>
                    <div v-if="isLoading" class="text-sm text-gray-500">
                      Bot đang trả lời...
                    </div>
                  </div>
                </div>
                <div class="flex space-x-2">
                  <input
                    v-model="currentMessage"
                    @keyup.enter="sendMessage"
                    placeholder="Nhập tin nhắn..."
                    class="flex-1 px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    :disabled="isLoading"
                  />
                  <button
                    @click="sendMessage"
                    :disabled="!currentMessage.trim() || isLoading"
                    class="px-4 py-2 bg-blue-600 text-white rounded-md text-sm font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Gửi
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { chatAPI } from '../services/api'
import simpleWebSocketService from '../services/simple-websocket'

const router = useRouter()
const authStore = useAuthStore()

const currentMessage = ref('')
const messages = ref([])
const isLoading = ref(false)
const isTelegramLinked = ref(false)
const messagesContainer = ref(null)
const showChat = ref(false)
const notifications = ref([])

const quickCommands = ref([
  '/start',
  '/help',
  '/follow',
  '/unfollow',
  '/list',
  '/clear'
])

const sendMessage = async () => {
  if (!currentMessage.value.trim() || isLoading.value) return
  
  const messageText = currentMessage.value.trim()
  currentMessage.value = ''
  
  addMessage('user', messageText)
  await processCommand(messageText)
}

const sendQuickCommand = (command) => {
  currentMessage.value = command
  sendMessage()
}

const addMessage = (sender, text) => {
  const message = {
    id: Date.now() + Math.random(),
    sender,
    text,
    timestamp: new Date()
  }
  
  messages.value.push(message)
  
  nextTick(() => {
    scrollToBottom()
  })
}

const processCommand = async (command) => {
  if (!authStore.isAuthenticated) {
    addMessage('bot', 'Vui lòng đăng nhập để sử dụng chat bot!')
    return
  }
  
  // Xử lý lệnh /clear
  if (command.trim() === '/clear') {
    messages.value = []
    addMessage('bot', 'Đã xóa tất cả tin nhắn!')
    return
  }
  
  isLoading.value = true
  
  try {
    const parts = command.trim().split(' ')
    const cmd = parts[0]
    const argument = parts.slice(1).join(' ')
    
    const response = await chatAPI.sendMessage(
      cmd,
      authStore.walletAddress,
      argument
    )
    
    if (response.data && response.data.data) {
      const botResponse = response.data.data
      
      if (botResponse.message && botResponse.message.includes('not linked to Telegram')) {
        addMessage('bot', 'Bạn cần liên kết tài khoản Telegram trước!\n\nHãy click nút "Lấy Linking Code" để lấy linking code.')
        isTelegramLinked.value = false
      } else {
        addMessage('bot', botResponse.message || 'Lệnh đã được xử lý thành công!')
        isTelegramLinked.value = true
      }
    } else {
      addMessage('bot', 'Không thể xử lý lệnh. Vui lòng thử lại!')
    }
  } catch (error) {
    if (error.response?.data?.message?.includes('not linked to Telegram')) {
      addMessage('bot', 'Bạn cần liên kết tài khoản Telegram trước!\n\nHãy click nút "Lấy Linking Code" để lấy linking code.')
      isTelegramLinked.value = false
    } else {
      addMessage('bot', 'Có lỗi xảy ra khi xử lý lệnh. Vui lòng thử lại!')
    }
  } finally {
    isLoading.value = false
  }
}

const getLinkingCode = async () => {
  try {
    const response = await chatAPI.getLinkAccount(authStore.walletAddress)
    const linkingCode = response.data.data
    
    addMessage('bot', `Linking Code: ${linkingCode}\n\nGửi lệnh này trong Telegram bot:\n/link ${linkingCode}\n\nSau khi link thành công, bạn có thể sử dụng các lệnh khác ở đây.`)
    
    const botUsername = 'taiteasicale_bot'
    const telegramUrl = `https://t.me/${botUsername}`
    window.open(telegramUrl, '_blank')
    
  } catch (error) {
    addMessage('bot', 'Không thể lấy linking code. Vui lòng thử lại!')
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

const logout = () => {
  authStore.logout()
  router.push('/')
}

const addNotification = (message) => {
  const notification = {
    id: Date.now() + Math.random(),
    message,
    timestamp: new Date()
  }
  
  notifications.value.unshift(notification)
  
  if (notifications.value.length > 10) {
    notifications.value = notifications.value.slice(0, 10)
  }
}

const handleWebSocketNotification = (event) => {
  const { type, data } = event.detail
  
  switch (type) {
    case 'notification':
      addMessage('bot', `Thông báo: ${data}`)
      break
    case 'wallet-activity':
      const networkName = data.networkAsString || data.network || 'Unknown'
      const fromAddress = data.fromAddress || 'Unknown'
      const toAddress = data.toAddress || 'Unknown'
      const value = data.valueFormatted || (data.value ? (parseFloat(data.value) / Math.pow(10, 18)).toFixed(4) : '0')
      const currency = data.currency || 'ETH'
      
      const walletMessage = `Hoạt động ví\nNetwork: ${networkName}\nFrom: ${fromAddress}\nTo: ${toAddress}\nValue: ${value} ${currency}\nHash: ${data.transactionHash || 'Unknown'}`
      addMessage('bot', walletMessage)
      break
    case 'price-alert':
      const priceMessage = `Cảnh báo giá: ${data.symbol} = $${data.price}`
      addMessage('bot', priceMessage)
      break
  }
}

onMounted(() => {
  if (authStore.isAuthenticated) {
    addMessage('bot', `Xin chào! Tôi là Web3 Chat Bot.\n\nCác lệnh có sẵn:\n/start - Lời chào mừng\n/help - Xem danh sách lệnh\n/follow <địa_chỉ_ví> - Theo dõi ví\n/unfollow <địa_chỉ_ví> - Bỏ theo dõi ví\n/list - Xem danh sách ví đang theo dõi\n\nĐể liên kết Telegram:\n1. Click "Lấy Linking Code" để lấy code\n2. Gửi /link <code> trong Telegram bot\n3. Sau khi link thành công, chat ở đây sẽ đồng bộ với Telegram\n\nChat ở đây = Chat trực tiếp với bot Telegram!`)
    
    if (authStore.user && authStore.user.id) {
      simpleWebSocketService.connect(authStore.user.id)
    } else {
      simpleWebSocketService.connect(1)
    }
    
    window.addEventListener('websocket-notification', handleWebSocketNotification)
  }
})

onUnmounted(() => {
  simpleWebSocketService.disconnect()
  window.removeEventListener('websocket-notification', handleWebSocketNotification)
})
</script>