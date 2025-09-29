import { onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'

export function useAuthInit() {
  const authStore = useAuthStore()

  onMounted(async () => {
    try {
      // Khôi phục trạng thái đăng nhập từ localStorage
      const restored = authStore.restoreFromLocalStorage()
      
      if (restored && authStore.walletAddress) {
        // Kiểm tra xem ví có còn kết nối không
        if (window.ethereum) {
          const accounts = await window.ethereum.request({ method: 'eth_accounts' })
          
          if (accounts.length > 0 && accounts[0].toLowerCase() === authStore.walletAddress.toLowerCase()) {
            // Ví vẫn còn kết nối, khôi phục user profile
            try {
              await authStore.getUserProfile()
              console.log('Đã khôi phục trạng thái đăng nhập thành công!')
            } catch (profileError) {
              console.warn('Không thể tải thông tin profile, nhưng vẫn giữ trạng thái đăng nhập')
            }
          } else {
            // Ví đã ngắt kết nối, đăng xuất
            console.log('Ví đã ngắt kết nối, đăng xuất...')
            authStore.logout()
          }
        } else {
          // MetaMask không có, đăng xuất
          console.log('MetaMask không có, đăng xuất...')
          authStore.logout()
        }
      }
    } catch (error) {
      console.error('Error during auth initialization:', error)
      // Có lỗi, đăng xuất để đảm bảo an toàn
      authStore.logout()
    }

    // Lắng nghe sự kiện thay đổi tài khoản MetaMask
    if (window.ethereum) {
      const handleAccountsChanged = (accounts) => {
        if (accounts.length === 0) {
          // Người dùng đã ngắt kết nối ví
          authStore.logout()
        } else if (accounts[0].toLowerCase() !== authStore.walletAddress?.toLowerCase()) {
          // Người dùng đã chuyển sang tài khoản khác
          authStore.logout()
        }
      }

      const handleChainChanged = () => {
        // Có thể thêm logic xử lý khi thay đổi network
        console.log('Network changed')
      }

      // Đăng ký listeners
      window.ethereum.on('accountsChanged', handleAccountsChanged)
      window.ethereum.on('chainChanged', handleChainChanged)

      // Cleanup function
      return () => {
        if (window.ethereum) {
          window.ethereum.removeListener('accountsChanged', handleAccountsChanged)
          window.ethereum.removeListener('chainChanged', handleChainChanged)
        }
      }
    }
  })

  return {
    authStore
  }
}
