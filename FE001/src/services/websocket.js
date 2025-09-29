

class WebSocketService {
  constructor() {
    this.stompClient = null
    this.connected = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 5000
  }

  connect(userId) {
    if (this.connected) return

    try {
      
      const socket = new SockJS('http://localhost:8080/api/ws')
      
      
      this.stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        debug: (str) => console.log('STOMP: ' + str),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      })

      
      this.stompClient.onConnect = (frame) => {
        console.log('Connected: ' + frame)
        this.connected = true
        this.reconnectAttempts = 0
        
        
        this.stompClient.subscribe(`/user/queue/notifications`, (message) => {
          this.handleNotification(JSON.parse(message.body))
        })

        
        this.stompClient.subscribe(`/user/queue/wallet-activity`, (message) => {
          this.handleWalletActivity(JSON.parse(message.body))
        })

        
        this.stompClient.subscribe(`/user/queue/price-alert`, (message) => {
          this.handlePriceAlert(JSON.parse(message.body))
        })

        
        this.stompClient.publish({
          destination: '/app/register',
          body: JSON.stringify({ userId: userId })
        })
        
        console.log('WebSocket connected and registered for user:', userId)
      }

      this.stompClient.onStompError = (frame) => {
        console.error('STOMP error: ' + frame.headers['message'])
        console.error('Details: ' + frame.body)
        this.connected = false
        this.attemptReconnect(userId)
      }

      this.stompClient.onWebSocketError = (error) => {
        console.error('WebSocket error: ' + error)
        this.connected = false
        this.attemptReconnect(userId)
      }

      
      this.stompClient.activate()
    } catch (error) {
      console.error('WebSocket connection error:', error)
      this.attemptReconnect(userId)
    }
  }

  attemptReconnect(userId) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      
      setTimeout(() => {
        this.connect(userId)
      }, this.reconnectInterval)
    } else {
      console.error('Max reconnection attempts reached')
    }
  }

  disconnect() {
    if (this.stompClient && this.connected) {
      this.stompClient.deactivate()
      this.connected = false
      console.log('WebSocket disconnected')
    }
  }

  handleNotification(notification) {
    console.log('Received notification:', notification)
    
    window.dispatchEvent(new CustomEvent('websocket-notification', {
      detail: { type: 'notification', data: notification }
    }))
  }

  handleWalletActivity(activity) {
    console.log('Received wallet activity:', activity)
    
    window.dispatchEvent(new CustomEvent('websocket-notification', {
      detail: { type: 'wallet-activity', data: activity }
    }))
  }

  handlePriceAlert(alert) {
    console.log('Received price alert:', alert)
    
    window.dispatchEvent(new CustomEvent('websocket-notification', {
      detail: { type: 'price-alert', data: alert }
    }))
  }

  sendMessage(destination, message) {
    if (this.stompClient && this.connected) {
      this.stompClient.publish({
        destination: destination,
        body: JSON.stringify(message)
      })
    } else {
      console.error('WebSocket not connected')
    }
  }
}


const webSocketService = new WebSocketService()

export default webSocketService
