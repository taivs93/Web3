<template>
  <div class="bg-gradient-to-br from-blue-50 to-indigo-100">
      <!-- Hero Section -->
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div class="text-center">
          <h1 class="text-4xl font-bold text-gray-900 mb-6">
            Chào mừng đến với Web3 Authentication
          </h1>
          <p class="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
            Đăng nhập an toàn bằng ví Ethereum của bạn. Sử dụng MetaMask để xác thực danh tính và quản lý hồ sơ cá nhân.
          </p>
          
          <div class="flex justify-center space-x-4">
            <router-link
              to="/login"
              class="bg-indigo-600 text-white px-8 py-3 rounded-lg text-lg font-medium hover:bg-indigo-700 transition-colors"
            >
              Bắt đầu ngay
            </router-link>
          </div>
        </div>
      </div>

      <!-- Features -->
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="mt-16 grid grid-cols-1 md:grid-cols-3 gap-8">
          <div class="bg-white p-6 rounded-lg shadow-md">
            <div class="w-12 h-12 bg-indigo-100 rounded-lg flex items-center justify-center mb-4">
              <svg class="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">Bảo mật cao</h3>
            <p class="text-gray-600">Xác thực bằng chữ ký số từ ví Ethereum, đảm bảo an toàn tuyệt đối.</p>
          </div>

          <div class="bg-white p-6 rounded-lg shadow-md">
            <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center mb-4">
              <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">Nhanh chóng</h3>
            <p class="text-gray-600">Đăng nhập chỉ với một cú nhấp chuột, không cần nhớ mật khẩu.</p>
          </div>

          <div class="bg-white p-6 rounded-lg shadow-md">
            <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center mb-4">
              <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">Quản lý hồ sơ</h3>
            <p class="text-gray-600">Cập nhật và quản lý thông tin cá nhân một cách dễ dàng.</p>
          </div>
        </div>

        <!-- How it works -->
        <div class="mt-16">
          <h2 class="text-3xl font-bold text-gray-900 text-center mb-8">Cách thức hoạt động</h2>
          <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
            <div class="text-center">
              <div class="w-16 h-16 bg-indigo-600 text-white rounded-full flex items-center justify-center mx-auto mb-4 text-xl font-bold">
                1
              </div>
              <h3 class="font-semibold text-gray-900 mb-2">Kết nối MetaMask</h3>
              <p class="text-gray-600">Kết nối ví Ethereum của bạn</p>
            </div>
            <div class="text-center">
              <div class="w-16 h-16 bg-indigo-600 text-white rounded-full flex items-center justify-center mx-auto mb-4 text-xl font-bold">
                2
              </div>
              <h3 class="font-semibold text-gray-900 mb-2">Ký thông điệp</h3>
              <p class="text-gray-600">Xác nhận danh tính bằng chữ ký số</p>
            </div>
            <div class="text-center">
              <div class="w-16 h-16 bg-indigo-600 text-white rounded-full flex items-center justify-center mx-auto mb-4 text-xl font-bold">
                3
              </div>
              <h3 class="font-semibold text-gray-900 mb-2">Xác thực</h3>
              <p class="text-gray-600">Hệ thống xác minh chữ ký</p>
            </div>
            <div class="text-center">
              <div class="w-16 h-16 bg-indigo-600 text-white rounded-full flex items-center justify-center mx-auto mb-4 text-xl font-bold">
                4
              </div>
              <h3 class="font-semibold text-gray-900 mb-2">Truy cập</h3>
              <p class="text-gray-600">Đăng nhập thành công</p>
            </div>
          </div>
        </div>

        <!-- API Information -->
        <div class="mt-16">
          <h2 class="text-3xl font-bold text-gray-900 text-center mb-8">API Endpoints</h2>
          <div class="bg-white p-6 rounded-lg shadow-lg">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="space-y-4">
                <div class="p-4 bg-green-50 rounded-lg">
                  <div class="flex items-center mb-2">
                    <span class="bg-green-500 text-white px-2 py-1 rounded text-xs font-mono">POST</span>
                    <span class="ml-2 font-mono text-sm">/api/login</span>
                  </div>
                  <p class="text-sm text-gray-600">Đăng nhập bằng chữ ký wallet</p>
                </div>

                <div class="p-4 bg-blue-50 rounded-lg">
                  <div class="flex items-center mb-2">
                    <span class="bg-blue-500 text-white px-2 py-1 rounded text-xs font-mono">GET</span>
                    <span class="ml-2 font-mono text-sm">/api/profile</span>
                  </div>
                  <p class="text-sm text-gray-600">Lấy thông tin profile user</p>
                </div>
              </div>

              <div class="space-y-4">
                <div class="p-4 bg-yellow-50 rounded-lg">
                  <div class="flex items-center mb-2">
                    <span class="bg-yellow-500 text-white px-2 py-1 rounded text-xs font-mono">PUT</span>
                    <span class="ml-2 font-mono text-sm">/api/profile</span>
                  </div>
                  <p class="text-sm text-gray-600">Cập nhật thông tin profile</p>
                </div>

                <div class="p-4 bg-purple-50 rounded-lg">
                  <div class="flex items-center mb-2">
                    <span class="bg-purple-500 text-white px-2 py-1 rounded text-xs font-mono">GET</span>
                    <span class="ml-2 font-mono text-sm">/api/health</span>
                  </div>
                  <p class="text-sm text-gray-600">Kiểm tra trạng thái server</p>
                </div>

                <div class="p-4 bg-orange-50 rounded-lg">
                  <div class="flex items-center mb-2">
                    <span class="bg-orange-500 text-white px-2 py-1 rounded text-xs font-mono">GET</span>
                    <span class="ml-2 font-mono text-sm">/api/gas/estimate/{network}</span>
                  </div>
                  <p class="text-sm text-gray-600">Lấy ước tính phí gas</p>
                </div>

                <div class="p-4 bg-teal-50 rounded-lg">
                  <div class="flex items-center mb-2">
                    <span class="bg-teal-500 text-white px-2 py-1 rounded text-xs font-mono">POST</span>
                    <span class="ml-2 font-mono text-sm">/api/search/general</span>
                  </div>
                  <p class="text-sm text-gray-600">Tìm kiếm blockchain data</p>
                </div>
              </div>
            </div>
            
            <div class="mt-6 p-4 bg-gray-50 rounded-lg">
              <h4 class="font-semibold text-gray-900 mb-2">Thông tin kỹ thuật:</h4>
              <ul class="text-sm text-gray-600 space-y-1">
                <li>• <strong>Backend:</strong> Spring Boot + MySQL</li>
                <li>• <strong>Web3 Integration:</strong> Ethereum signature verification</li>
                <li>• <strong>Frontend:</strong> Vue.js 3 + TailwindCSS</li>
                <li>• <strong>API Response:</strong> Chuẩn REST với ResponseDTO wrapper</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const logout = () => {
  authStore.logout()
  router.push('/')
}

onMounted(() => {
  console.log('Home.vue mounted')
  console.log('Auth state:', {
    isAuthenticated: authStore.isAuthenticated,
    user: authStore.user,
    walletAddress: authStore.walletAddress
  })
})
</script>
