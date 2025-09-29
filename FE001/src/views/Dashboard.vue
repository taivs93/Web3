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

          <!-- Quản lý ví -->
          <div class="bg-white rounded-lg shadow-lg mb-6">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-semibold text-gray-900">Quản lý ví theo dõi</h2>
            </div>
            <div class="p-6">
              <!-- Thêm ví mới -->
              <div class="mb-6">
                <h3 class="text-md font-medium text-gray-900 mb-4">Thêm ví để theo dõi</h3>
                <div class="flex space-x-3">
                  <input
                    v-model="newWalletAddress"
                    type="text"
                    placeholder="Nhập địa chỉ ví (0x...)"
                    class="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                  <button
                    @click="addWallet"
                    :disabled="!newWalletAddress.trim() || isAddingWallet"
                    class="px-4 py-2 bg-indigo-600 text-white rounded-md text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {{ isAddingWallet ? 'Đang thêm...' : 'Thêm ví' }}
                  </button>
                </div>
              </div>

              <!-- Danh sách ví đang theo dõi -->
              <div>
                <div class="flex justify-between items-center mb-4">
                  <h3 class="text-md font-medium text-gray-900">Ví đang theo dõi</h3>
                  <button
                    @click="refreshWallets"
                    class="px-3 py-1 bg-gray-100 text-gray-700 rounded-md text-sm hover:bg-gray-200 transition-colors"
                  >
                    Làm mới
                  </button>
                </div>
                
                <div v-if="followedWallets.length === 0" class="text-center py-8 text-gray-500">
                  <svg class="w-12 h-12 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"></path>
                  </svg>
                  <p>Chưa có ví nào được theo dõi</p>
                </div>

                <div v-else class="space-y-3">
                  <div
                    v-for="(wallet, index) in followedWallets"
                    :key="wallet.id"
                    class="flex items-center justify-between p-4 bg-gray-50 rounded-lg"
                  >
                    <div class="flex items-center space-x-3">
                      <div class="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center">
                        <span class="text-sm font-medium text-indigo-600">{{ index + 1 }}</span>
                      </div>
                      <div>
                        <p class="text-sm font-mono text-gray-900">{{ wallet.address }}</p>
                        <p class="text-xs text-gray-500">{{ wallet.label || 'Ví cá nhân' }}</p>
                      </div>
                    </div>
                    <div class="flex items-center space-x-2">
                      <button
                        @click="copyWalletAddress(wallet.address)"
                        class="p-2 text-gray-400 hover:text-gray-600 transition-colors"
                        title="Sao chép địa chỉ"
                      >
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"></path>
                        </svg>
                      </button>
                      <button
                        @click="confirmUnfollowWallet(wallet)"
                        class="px-3 py-1 bg-red-100 text-red-700 rounded-md text-sm hover:bg-red-200 transition-colors"
                      >
                        Hủy theo dõi
                      </button>
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

// Wallet management
const newWalletAddress = ref('')
const followedWallets = ref([])
const isAddingWallet = ref(false)
const unfollowConfirmText = ref('')
const walletToUnfollow = ref(null)

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

// Wallet management functions
const addWallet = async () => {
  if (!newWalletAddress.value.trim()) return
  
  const address = newWalletAddress.value.trim()
  if (!address.startsWith('0x') || address.length !== 42) {
    addMessage('bot', 'Địa chỉ ví không hợp lệ! Vui lòng nhập địa chỉ ví Ethereum hợp lệ (bắt đầu bằng 0x và có 42 ký tự).')
    return
  }
  
  isAddingWallet.value = true
  
  try {
    // Gửi lệnh follow qua chat API
    await processCommand(`/follow ${address}`)
    newWalletAddress.value = ''
    await refreshWallets()
  } catch (error) {
    addMessage('bot', 'Có lỗi khi thêm ví. Vui lòng thử lại!')
  } finally {
    isAddingWallet.value = false
  }
}

const refreshWallets = async () => {
  try {
    // Gửi lệnh list để lấy danh sách ví
    await processCommand('/list')
  } catch (error) {
    console.error('Error refreshing wallets:', error)
  }
}

const copyWalletAddress = async (address) => {
  try {
    await navigator.clipboard.writeText(address)
    addMessage('bot', `Đã sao chép địa chỉ ví: ${address}`)
  } catch (error) {
    addMessage('bot', 'Không thể sao chép địa chỉ ví!')
  }
}

const confirmUnfollowWallet = (wallet) => {
  walletToUnfollow.value = wallet
  unfollowConfirmText.value = ''
  
  // Hiển thị modal xác nhận
  const modal = document.createElement('div')
  modal.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50'
  modal.innerHTML = `
    <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">Xác nhận hủy theo dõi</h3>
      <p class="text-sm text-gray-600 mb-4">
        Bạn có chắc chắn muốn hủy theo dõi ví này không?
      </p>
      <p class="text-xs font-mono text-gray-500 mb-4">{{ wallet.address }}</p>
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-2">
          Để xác nhận, vui lòng nhập "UnFollow":
        </label>
        <input
          type="text"
          placeholder="Nhập UnFollow"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-red-500"
          id="unfollowConfirmInput"
        />
      </div>
      <div class="flex space-x-3">
        <button
          id="cancelUnfollow"
          class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 transition-colors"
        >
          Hủy
        </button>
        <button
          id="confirmUnfollow"
          class="flex-1 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors disabled:opacity-50"
          disabled
        >
          Hủy theo dõi
        </button>
      </div>
    </div>
  `
  
  document.body.appendChild(modal)
  
  const input = modal.querySelector('#unfollowConfirmInput')
  const cancelBtn = modal.querySelector('#cancelUnfollow')
  const confirmBtn = modal.querySelector('#confirmUnfollow')
  
  input.addEventListener('input', (e) => {
    confirmBtn.disabled = e.target.value !== 'UnFollow'
  })
  
  cancelBtn.addEventListener('click', () => {
    document.body.removeChild(modal)
  })
  
  confirmBtn.addEventListener('click', async () => {
    if (input.value === 'UnFollow') {
      await unfollowWallet(wallet.address)
      document.body.removeChild(modal)
    }
  })
  
  // Focus vào input
  setTimeout(() => input.focus(), 100)
}

const unfollowWallet = async (address) => {
  try {
    await processCommand(`/unfollow ${address}`)
    await refreshWallets()
    addMessage('bot', `Đã hủy theo dõi ví: ${address}`)
  } catch (error) {
    addMessage('bot', 'Có lỗi khi hủy theo dõi ví. Vui lòng thử lại!')
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

onMounted(async () => {
  if (authStore.isAuthenticated) {
    addMessage('bot', `Xin chào! Tôi là Web3 Chat Bot.\n\nCác lệnh có sẵn:\n/start - Lời chào mừng\n/help - Xem danh sách lệnh\n/follow <địa_chỉ_ví> - Theo dõi ví\n/unfollow <địa_chỉ_ví> - Bỏ theo dõi ví\n/list - Xem danh sách ví đang theo dõi\n\nĐể liên kết Telegram:\n1. Click "Lấy Linking Code" để lấy code\n2. Gửi /link <code> trong Telegram bot\n3. Sau khi link thành công, chat ở đây sẽ đồng bộ với Telegram\n\nChat ở đây = Chat trực tiếp với bot Telegram!`)
    
    // Load danh sách ví khi đã đăng nhập
    await refreshWallets()
    
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