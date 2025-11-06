import { getPrivateMessages, getGroupMessages } from '@/api/message'
import wsManager from '@/utils/websocket'

const state = {
  // 私聊消息: { [friendId]: [...] }
  privateMessages: {},
  // 群聊消息: { [groupId]: [...] }
  groupMessages: {},
  // 未读消息数: { [chatId]: count }
  unreadCounts: {}
}

const mutations = {
  // 添加私聊消息
  ADD_PRIVATE_MESSAGE(state, { friendId, message }) {
    if (!state.privateMessages[friendId]) {
      state.privateMessages[friendId] = []
    }
    state.privateMessages[friendId].push(message)
  },
  
  // 设置私聊消息列表
  SET_PRIVATE_MESSAGES(state, { friendId, messages }) {
    state.privateMessages[friendId] = messages
  },
  
  // 添加群聊消息
  ADD_GROUP_MESSAGE(state, { groupId, message }) {
    if (!state.groupMessages[groupId]) {
      state.groupMessages[groupId] = []
    }
    state.groupMessages[groupId].push(message)
  },
  
  // 设置群聊消息列表
  SET_GROUP_MESSAGES(state, { groupId, messages }) {
    state.groupMessages[groupId] = messages
  },
  
  // 更新未读消息数
  UPDATE_UNREAD_COUNT(state, { chatId, count }) {
    state.unreadCounts[chatId] = count
  },
  
  // 清除未读消息数
  CLEAR_UNREAD_COUNT(state, chatId) {
    state.unreadCounts[chatId] = 0
  },
  
  // 清空所有消息
  CLEAR_ALL_MESSAGES(state) {
    state.privateMessages = {}
    state.groupMessages = {}
    state.unreadCounts = {}
  }
}

const actions = {
  // 获取私聊消息历史
  async getPrivateMessages({ commit, rootState }, { friendId, page = 1, pageSize = 50 }) {
    const res = await getPrivateMessages(friendId, page, pageSize)
    if (res.data) {
      // 标记消息是否为自己发送的
      const messages = res.data.map(msg => ({
        ...msg,
        isOwn: msg.senderId === rootState.user?.userInfo?.id
      }))
      commit('SET_PRIVATE_MESSAGES', { friendId, messages })
    }
    return res
  },
  
  // 获取群聊消息历史
  async getGroupMessages({ commit, rootState }, { groupId, page = 1, pageSize = 50 }) {
    const res = await getGroupMessages(groupId, page, pageSize)
    if (res.data) {
      // 标记消息是否为自己发送的
      const messages = res.data.map(msg => ({
        ...msg,
        isOwn: msg.senderId === rootState.user?.userInfo?.id
      }))
      commit('SET_GROUP_MESSAGES', { groupId, messages })
    }
    return res
  },
  
  // 发送私聊消息
  sendPrivateMessage({ commit, rootState }, { friendId, content }) {
    const message = {
      type: 'PRIVATE_MESSAGE',
      data: {
        receiverId: friendId,
        content,
        timestamp: Date.now()
      }
    }
    wsManager.send(message)
    
    // 立即添加到本地消息列表（优化用户体验）
    const localMessage = {
      id: `temp_${Date.now()}`,
      senderId: rootState.user.userInfo.id,
      receiverId: friendId,
      content,
      timestamp: Date.now(),
      isOwn: true
    }
    commit('ADD_PRIVATE_MESSAGE', { friendId, message: localMessage })
  },
  
  // 发送群聊消息
  sendGroupMessage({ commit, rootState }, { groupId, content }) {
    const message = {
      type: 'GROUP_MESSAGE',
      data: {
        groupId,
        content,
        timestamp: Date.now()
      }
    }
    wsManager.send(message)
    
    // 立即添加到本地消息列表（优化用户体验）
    const localMessage = {
      id: `temp_${Date.now()}`,
      senderId: rootState.user.userInfo.id,
      groupId,
      content,
      timestamp: Date.now(),
      isOwn: true,
      senderName: rootState.user.userInfo.nickname
    }
    commit('ADD_GROUP_MESSAGE', { groupId, message: localMessage })
  },
  
  // 接收WebSocket消息
  receiveMessage({ commit, rootState, state }, data) {
    const { type, data: messageData } = data
    
    if (type === 'PRIVATE_MESSAGE') {
      const friendId = messageData.senderId === rootState.user.userInfo.id 
        ? messageData.receiverId 
        : messageData.senderId
      
      const message = {
        ...messageData,
        isOwn: messageData.senderId === rootState.user.userInfo.id
      }
      
      commit('ADD_PRIVATE_MESSAGE', {
        friendId,
        message
      })
      
      // 如果不是自己发送的消息，更新未读消息数
      if (!message.isOwn) {
        const currentCount = state.unreadCounts[friendId] || 0
        commit('UPDATE_UNREAD_COUNT', {
          chatId: friendId,
          count: currentCount + 1
        })
      }
    } else if (type === 'GROUP_MESSAGE') {
      const message = {
        ...messageData,
        isOwn: messageData.senderId === rootState.user.userInfo.id
      }
      
      commit('ADD_GROUP_MESSAGE', {
        groupId: messageData.groupId,
        message
      })
      
      // 如果不是自己发送的消息，更新未读消息数
      if (!message.isOwn) {
        const currentCount = state.unreadCounts[messageData.groupId] || 0
        commit('UPDATE_UNREAD_COUNT', {
          chatId: messageData.groupId,
          count: currentCount + 1
        })
      }
    }
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
