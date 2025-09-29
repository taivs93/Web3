<template>
  <div class="min-h-screen bg-red-100 flex items-center justify-center">
    <div class="text-center">
      <h1 class="text-4xl font-bold text-red-900 mb-4">TEST PAGE - NẾU THẤY TRANG NÀY THÌ VUE HOẠT ĐỘNG!</h1>
      <p class="text-xl text-red-600">Vue Router đang hoạt động bình thường!</p>
      <div class="mt-8">
        <router-link 
          to="/" 
          class="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors"
        >
          Về trang chủ
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'

onMounted(() => {
  console.log('TEST PAGE MOUNTED - Vue is working!')
})
</script>
