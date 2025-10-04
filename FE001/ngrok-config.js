// Ngrok Tunnel Configuration
// Thay thế URL này bằng URL ngrok thực tế của bạn

export const NGROK_CONFIG = {
  // URL ngrok của bạn (đã cập nhật với URL thực tế)
  FRONTEND_URL: 'https://6aeed624f07c.ngrok-free.app',
  API_BASE_URL: 'https://6aeed624f07c.ngrok-free.app/api',
  WS_URL: 'wss://8aa396bdad4a.ngrok-free.app/api/ws',
  BACKEND_URL: 'https://6aeed624f07c.ngrok-free.app'
}

// Hàm để lấy cấu hình dựa trên môi trường
export const getConfig = () => {
  const isTunnel = window.location.hostname.includes('ngrok.io') || 
                   window.location.hostname.includes('ngrok-free.app')
  
  if (isTunnel) {
    return {
      API_BASE_URL: NGROK_CONFIG.API_BASE_URL,
      WS_URL: NGROK_CONFIG.WS_URL,
      BACKEND_URL: NGROK_CONFIG.BACKEND_URL
    }
  }
  
  // Cấu hình local
  return {
    API_BASE_URL: '/api',
    WS_URL: `ws://${window.location.hostname}:8080/api/ws`,
    BACKEND_URL: `http://${window.location.hostname}:8080`
  }
}
