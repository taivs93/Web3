<template>
  <div v-if="show" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg p-8 max-w-md mx-4 relative">
      <!-- Close button -->
      <button 
        @click="closePopup"
        class="absolute top-4 right-4 text-gray-400 hover:text-gray-600"
      >
        ✕
      </button>
      
      <!-- Content -->
      <div class="text-center mb-8">
        <h2 class="text-2xl font-bold text-gray-900 mb-4">🤔 Xác nhận quan trọng</h2>
        <p class="text-gray-600 leading-relaxed">
          Bạn có chắc chắn muốn tiếp tục không? Hành động này có thể ảnh hưởng đến 
          <span class="font-semibold text-red-500">tương lai của bạn</span> và 
          <span class="font-semibold text-blue-500">thế giới crypto</span>! 
          <br><br>
          <span class="text-sm text-gray-500">
            *Lưu ý: Đây chỉ là một popup vô nghĩa để test UI 😄
          </span>
        </p>
      </div>
      
      <!-- Buttons Container -->
      <div class="flex justify-center relative">
        <!-- Đồng ý Button -->
        <button
          ref="agreeButton"
          @click="handleAgree"
          @mouseenter="onButtonHover('agree')"
          class="px-6 py-3 bg-green-500 text-white rounded-lg font-semibold hover:bg-green-600 transition-colors duration-200"
          :class="{ 'mr-12': !isSwapped, 'ml-12': isSwapped }"
        >
          ✅ Đồng ý
        </button>
        
        <!-- Từ chối Button -->
        <button
          ref="rejectButton"
          @click="handleReject"
          @mouseenter="onButtonHover('reject')"
          class="px-6 py-3 bg-red-500 text-white rounded-lg font-semibold hover:bg-red-600 transition-colors duration-200"
          :class="{ 'ml-12': !isSwapped, 'mr-12': isSwapped }"
        >
          ❌ Từ chối
        </button>
      </div>
      
      <!-- Fun message -->
      <div v-if="clickCount > 0" class="mt-4 text-center">
        <p class="text-sm text-gray-500">
          Bạn đã click {{ clickCount }} lần rồi! 😂
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'agree', 'reject'])

const agreeButton = ref(null)
const rejectButton = ref(null)
const clickCount = ref(0)
const isSwapped = ref(false)

// Track mouse movement
let mouseTracker = null

const onButtonHover = (buttonType) => {
  if (buttonType === 'reject') {
    // Khi hover vào "Từ chối", đổi chỗ các nút
    swapButtons()
  }
}

const swapButtons = () => {
  if (!agreeButton.value || !rejectButton.value) return
  
  // Đổi chỗ bằng cách thay đổi order trong CSS
  if (!isSwapped.value) {
    agreeButton.value.style.order = '2'
    rejectButton.value.style.order = '1'
    isSwapped.value = true
  } else {
    agreeButton.value.style.order = '1'
    rejectButton.value.style.order = '2'
    isSwapped.value = false
  }
}

const handleAgree = () => {
  clickCount.value++
  console.log('User clicked Đồng ý! 😄')
  emit('agree')
  
  // Hiển thị message vui
  setTimeout(() => {
    alert('Cảm ơn bạn đã đồng ý! Bạn thật dễ thương! 😊')
  }, 100)
}

const handleReject = () => {
  clickCount.value++
  console.log('User clicked Từ chối! 😂')
  emit('reject')
  
  // Hiển thị message vui
  setTimeout(() => {
    alert('Haha! Bạn vẫn click được "Từ chối"! Tài năng đấy! 🎉')
  }, 100)
}

const closePopup = () => {
  emit('close')
}

// Reset khi popup đóng
const resetPopup = () => {
  isSwapped.value = false
  clickCount.value = 0
  if (agreeButton.value) agreeButton.value.style.order = ''
  if (rejectButton.value) rejectButton.value.style.order = ''
}

// Watch show prop để reset khi đóng
import { watch } from 'vue'
watch(() => props.show, (newValue) => {
  if (!newValue) {
    resetPopup()
  }
})

onMounted(() => {
  // Có thể thêm hiệu ứng vui khi mount
  console.log('Trick Popup mounted! Ready to prank! 😈')
})

onUnmounted(() => {
  resetPopup()
})
</script>

<style scoped>
/* Không cần transition cho việc đổi chỗ */
</style>
