/**
 * WebSocket连接管理工具
 */
class WebSocketManager {
  constructor() {
    this.ws = null
    this.url = null
    this.reconnectTimer = null
    this.reconnectCount = 0
    this.maxReconnectCount = 5
    this.reconnectInterval = 3000
    this.messageHandlers = new Map()
    this.isManualClose = false
  }

  /**
   * 连接WebSocket
   * @param {string} url WebSocket地址
   * @param {string} token 用户token
   */
  connect(url, token) {
    this.url = url
    this.isManualClose = false
    
    // 构建WebSocket URL，包含token
    const wsUrl = `${url}?token=${token}`
    
    try {
      this.ws = new WebSocket(wsUrl)
      
      this.ws.onopen = () => {
        console.log('WebSocket连接成功')
        this.reconnectCount = 0
        this.onOpen()
      }
      
      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          this.handleMessage(data)
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      }
      
      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error)
        this.onError(error)
      }
      
      this.ws.onclose = () => {
        console.log('WebSocket连接关闭')
        this.onClose()
        
        // 如果不是手动关闭，则尝试重连
        if (!this.isManualClose) {
          this.reconnect()
        }
      }
    } catch (error) {
      console.error('WebSocket连接失败:', error)
      this.reconnect()
    }
  }

  /**
   * 重连
   */
  reconnect() {
    if (this.reconnectCount >= this.maxReconnectCount) {
      console.error('WebSocket重连次数已达上限')
      return
    }
    
    this.reconnectCount++
    console.log(`尝试重连WebSocket (${this.reconnectCount}/${this.maxReconnectCount})`)
    
    this.reconnectTimer = setTimeout(() => {
      if (this.url && !this.isManualClose) {
        // 重新获取token（从store或localStorage）
        const token = localStorage.getItem('token') || ''
        this.connect(this.url, token)
      }
    }, this.reconnectInterval)
  }

  /**
   * 关闭连接
   */
  close() {
    this.isManualClose = true
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  /**
   * 发送消息
   * @param {Object} message 消息对象
   */
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    } else {
      console.error('WebSocket未连接')
    }
  }

  /**
   * 注册消息处理器
   * @param {string} type 消息类型
   * @param {Function} handler 处理函数
   */
  onMessage(type, handler) {
    this.messageHandlers.set(type, handler)
  }

  /**
   * 移除消息处理器
   * @param {string} type 消息类型
   */
  offMessage(type) {
    this.messageHandlers.delete(type)
  }

  /**
   * 处理接收到的消息
   * @param {Object} data 消息数据
   */
  handleMessage(data) {
    const { type } = data
    const handler = this.messageHandlers.get(type)
    
    if (handler) {
      handler(data)
    } else {
      // 默认处理器
      console.log('收到WebSocket消息:', data)
    }
  }

  /**
   * 连接打开时的回调
   */
  onOpen() {
    // 可以在这里发送心跳包等
  }

  /**
   * 连接错误时的回调
   */
  onError() {
    // 错误处理
  }

  /**
   * 连接关闭时的回调
   */
  onClose() {
    // 关闭处理
  }

  /**
   * 获取连接状态
   */
  getState() {
    if (!this.ws) return WebSocket.CLOSED
    return this.ws.readyState
  }
}

// 创建单例
const wsManager = new WebSocketManager()

export default wsManager

