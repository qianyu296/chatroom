import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

/**
 * WebSocket连接管理工具（使用STOMP协议）
 */
class WebSocketManager {
  constructor() {
    this.stompClient = null
    this.url = null
    this.token = null
    this.reconnectTimer = null
    this.reconnectCount = 0
    this.maxReconnectCount = 10
    this.reconnectInterval = 5000
    this.messageHandlers = new Map()
    this.isManualClose = false
    this.subscriptions = new Map() // 存储订阅
    this.openListeners = [] // 连接成功回调列表
  }

  /**
   * 连接WebSocket（使用STOMP协议）
   * @param {string} url WebSocket地址（例如：ws://localhost:8080/ws 或 http://localhost:8080/ws）
   * @param {string} token 用户token
   */
  connect(url, token) {
    // 如果已经有连接，先关闭
    if (this.stompClient) {
      this.close()
    }

    this.token = token
    this.isManualClose = false
    
    // 根据当前页面的协议自动选择ws://或wss://
    // 如果页面是HTTPS（如通过ngrok访问），则使用wss://；如果是HTTP，则使用ws://
    let finalUrl = url
    const isHttps = window.location.protocol === 'https:'
    const currentHost = window.location.host
    
    // 检查是否通过ngrok访问（ngrok域名通常包含ngrok.io等）
    const isNgrok = currentHost.includes('ngrok') || 
                    currentHost.includes('ngrok-free.app') || 
                    currentHost.includes('ngrok.app')
    
    // 如果URL是ws://或wss://开头，根据页面协议调整
    if (url.startsWith('ws://') || url.startsWith('wss://')) {
      if (isHttps && url.startsWith('ws://')) {
        // HTTPS页面必须使用wss://
        finalUrl = url.replace(/^ws:/, 'wss:')
        
        // 如果通过ngrok访问，且URL包含localhost，需要替换为当前host
        if (isNgrok && finalUrl.includes('localhost')) {
          // 将localhost替换为当前ngrok的host
          finalUrl = finalUrl.replace(/localhost:\d+/, currentHost)
        }
      }
    } else if (url.startsWith('http://') || url.startsWith('https://')) {
      // 如果传入的是http://或https://，根据页面协议调整
      if (isHttps && url.startsWith('http://')) {
        finalUrl = url.replace(/^http:/, 'https:')
        
        // 如果通过ngrok访问，且URL包含localhost，需要替换为当前host
        if (isNgrok && finalUrl.includes('localhost')) {
          finalUrl = finalUrl.replace(/localhost:\d+/, currentHost)
        }
      }
    }
    
    this.url = finalUrl
    
    // 将 ws:// 或 wss:// 转换为 http:// 或 https://（SockJS需要）
    const httpUrl = finalUrl.replace(/^ws:/, 'http:').replace(/^wss:/, 'https:')
    
    try {
      // 创建SockJS连接
      const socket = new SockJS(httpUrl)
      
      // 创建STOMP客户端
      this.stompClient = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        debug: () => {
          // 可以在这里控制日志输出
          // console.log('STOMP:', str)
        },
        onConnect: () => {
          this.reconnectCount = 0
          this.onOpen()
        },
        onStompError: (frame) => {
          console.error('STOMP错误:', frame)
          // 通知用户连接错误
          if (this.onError) {
            this.onError(frame)
          }
          // 提示用户认证错误
          if (frame.headers && frame.headers.message) {
            console.error('连接错误:', frame.headers.message)
          }
        },
        onWebSocketClose: () => {
          this.onClose()

          // STOMP客户端会通过reconnectDelay自动重连，这里不需要手动处理
          // 但如果isManualClose为true（用户主动关闭），则不重连
          if (this.isManualClose) {
            // 用户主动关闭，不自动重连
          }
        },
        onDisconnect: () => {
          // 连接断开
        }
      })
      
      // 设置连接头（包含JWT token）
      this.stompClient.configure({
        connectHeaders: {
          Authorization: `Bearer ${token}`
        }
      })

      // 激活连接
      this.stompClient.activate()
    } catch (error) {
      console.error('WebSocket连接失败:', error)
      this.onError && this.onError({ message: 'WebSocket连接失败，请刷新页面' })
    }
  }

  /**
   * 重连
   */
  reconnect() {
    if (this.reconnectCount >= this.maxReconnectCount) {
      console.error('WebSocket重连次数已达上限')
      // 通知用户连接失败
      if (this.onError) {
        this.onError({ message: '聊天连接失败，请刷新页面或重新登录' })
      }
      return
    }

    this.reconnectCount++

    this.reconnectTimer = setTimeout(() => {
      if (this.url && !this.isManualClose) {
        // 重新获取token（从store或localStorage）
        const token = localStorage.getItem('token') || this.token || ''
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
    
    // 取消所有订阅
    this.subscriptions.forEach((subscription) => {
      subscription.unsubscribe()
    })
    this.subscriptions.clear()
    
    // 断开STOMP连接
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.deactivate()
    }
    this.stompClient = null
  }

  /**
   * 发送消息
   * @param {Object} message 消息对象，格式：{ type: 'PRIVATE_MESSAGE' | 'GROUP_MESSAGE', data: {...} }
   */
  send(message) {
    if (this.stompClient && this.stompClient.connected) {
      const { type, data } = message

      if (type === 'PRIVATE_MESSAGE') {
        // 发送私聊消息到 /app/chat/private
        this.stompClient.publish({
          destination: '/app/chat/private',
          body: JSON.stringify(data)
        })
      } else if (type === 'GROUP_MESSAGE') {
        // 发送群聊消息到 /app/chat/group
        this.stompClient.publish({
          destination: '/app/chat/group',
          body: JSON.stringify(data)
        })
      } else {
        console.warn('未知的消息类型:', type)
      }
    } else {
      console.error('WebSocket未连接，无法发送消息')
      // 通知用户消息发送失败
      if (this.onError) {
        this.onError({ message: '消息发送失败，聊天连接已断开' })
      }
    }
  }

  /**
   * 订阅消息
   * @param {string} destination 订阅目标（例如：/topic/private/{userId} 或 /topic/group/{groupId}）
   * @param {Function} callback 回调函数
   */
  subscribe(destination, callback) {
    if (this.stompClient && this.stompClient.connected) {
      const subscription = this.stompClient.subscribe(destination, (message) => {
        try {
          const data = JSON.parse(message.body)
          callback(data)
        } catch (error) {
          // 忽略解析错误
        }
      })

      this.subscriptions.set(destination, subscription)
      return subscription
    } else {
      return null
    }
  }

  /**
   * 取消订阅
   * @param {string} destination 订阅目标
   */
  unsubscribe(destination) {
    const subscription = this.subscriptions.get(destination)
    if (subscription) {
      subscription.unsubscribe()
      this.subscriptions.delete(destination)
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
    }
  }

  /**
   * 注册连接成功回调（支持多个监听器）
   */
  addOpenListener(listener) {
    this.openListeners.push(listener)
  }

  /**
   * 移除连接成功回调
   */
  removeOpenListener(listener) {
    this.openListeners = this.openListeners.filter(l => l !== listener)
  }

  /**
   * 连接打开时的回调
   */
  onOpen() {
    this.openListeners.forEach(listener => listener())
  }

  /**
   * 连接错误时的回调
   */
  onError(error) {
    console.error('WebSocket错误:', error)
  }

  /**
   * 连接关闭时的回调
   */
  onClose() {
    // 清除所有订阅（STOMP subscription 对象已失效）
    this.subscriptions.clear()
  }

  /**
   * 获取连接状态
   */
  getState() {
    if (!this.stompClient) return 'CLOSED'
    if (this.stompClient.connected) return 'OPEN'
    return 'CONNECTING'
  }

  /**
   * 检查是否已连接
   */
  isConnected() {
    return this.stompClient && this.stompClient.connected
  }
}

// 创建单例
const wsManager = new WebSocketManager()

export default wsManager

