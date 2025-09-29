class SimpleWebSocketService {
  constructor() {
    this.ws = null
    this.connected = false
    this.userId = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 5000
  }

  connect(userId) {
    if (this.connected) {
      return
    }

    this.userId = userId

    try {
      this.ws = new WebSocket('ws://localhost:8080/api/ws')
      
      this.ws.onopen = () => {
        this.connected = true
        this.reconnectAttempts = 0
        
        this.send({
          type: 'register',
          userId: userId
        })
      }

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          this.handleMessage(data)
        } catch (e) {
          this.handleTextMessage(event.data)
        }
      }

      this.ws.onclose = () => {
        this.connected = false
        this.attemptReconnect()
      }

      this.ws.onerror = (error) => {
        this.connected = false
        this.attemptReconnect()
      }

    } catch (error) {
      this.attemptReconnect()
    }
  }

  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      
      setTimeout(() => {
        this.connect(this.userId)
      }, this.reconnectInterval)
    }
  }

  disconnect() {
    if (this.ws && this.connected) {
      this.ws.close()
      this.connected = false
    }
  }

  send(message) {
    if (this.ws && this.connected) {
      this.ws.send(JSON.stringify(message))
    }
  }

  handleMessage(data) {
    console.log('WebSocket message received:', data)
    
    if (data.type === 'notification') {
      window.dispatchEvent(new CustomEvent('websocket-notification', {
        detail: { type: 'notification', data: data.data }
      }))
    } else if (data.type === 'wallet-activity') {
      window.dispatchEvent(new CustomEvent('websocket-notification', {
        detail: { type: 'wallet-activity', data: data.data }
      }))
    } else if (data.type === 'price-alert') {
      window.dispatchEvent(new CustomEvent('websocket-notification', {
        detail: { type: 'price-alert', data: data.data }
      }))
    } else if (data.type === 'portfolio-update') {
      window.dispatchEvent(new CustomEvent('websocket-notification', {
        detail: { type: 'portfolio-update', data: data.data }
      }))
    } else if (data.type === 'gas-update') {
      window.dispatchEvent(new CustomEvent('websocket-notification', {
        detail: { type: 'gas-update', data: data.data }
      }))
    } else if (data.type === 'registered') {
      console.log('WebSocket registered successfully')
    } else if (data.type === 'welcome') {
      console.log('WebSocket welcome message:', data.message)
    } else {
      window.dispatchEvent(new CustomEvent('websocket-notification', {
        detail: { type: 'message', data: data }
      }))
    }
  }

  handleTextMessage(text) {
    window.dispatchEvent(new CustomEvent('websocket-notification', {
      detail: { type: 'text', data: text }
    }))
  }
}

const simpleWebSocketService = new SimpleWebSocketService()

export default simpleWebSocketService
