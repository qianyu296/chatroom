import Vue from 'vue'
import { getPrivateMessages, getGroupMessages } from '@/api/message'
import wsManager from '@/utils/websocket'

// 统一转换为字符串，避免类型不一致导致比较失败
const toStr = (id) => String(id)

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
    const key = toStr(friendId)
    if (!state.privateMessages[key]) {
      Vue.set(state.privateMessages, key, [])
    }
    state.privateMessages[key].push(message)
  },

  // 设置私聊消息列表
  SET_PRIVATE_MESSAGES(state, { friendId, messages }) {
    Vue.set(state.privateMessages, toStr(friendId), messages)
  },

  // 添加群聊消息
  ADD_GROUP_MESSAGE(state, { groupId, message }) {
    const key = toStr(groupId)
    if (!state.groupMessages[key]) {
      Vue.set(state.groupMessages, key, [])
    }
    state.groupMessages[key].push(message)
  },

  // 设置群聊消息列表
  SET_GROUP_MESSAGES(state, { groupId, messages }) {
    Vue.set(state.groupMessages, toStr(groupId), messages)
  },

  // 更新未读消息数
  UPDATE_UNREAD_COUNT(state, { chatId, count }) {
    state.unreadCounts[toStr(chatId)] = count
  },

  // 清除未读消息数
  CLEAR_UNREAD_COUNT(state, chatId) {
    state.unreadCounts[toStr(chatId)] = 0
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
      const currentUserId = toStr(rootState.user?.userInfo?.id)
      // 标记消息是否为自己发送的
      const messages = res.data.map(msg => ({
        ...msg,
        isOwn: toStr(msg.senderId) === currentUserId
      }))
      commit('SET_PRIVATE_MESSAGES', { friendId, messages })
    }
    return res
  },

  // 获取群聊消息历史
  async getGroupMessages({ commit, rootState }, { groupId, page = 1, pageSize = 50 }) {
    const res = await getGroupMessages(groupId, page, pageSize)
    if (res.data) {
      const currentUserId = toStr(rootState.user?.userInfo?.id)
      // 标记消息是否为自己发送的
      const messages = res.data.map(msg => ({
        ...msg,
        isOwn: toStr(msg.senderId) === currentUserId
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
    const currentUserId = toStr(rootState.user?.userInfo?.id)

    if (type === 'PRIVATE_MESSAGE') {
      const senderId = toStr(messageData.senderId)
      const receiverId = toStr(messageData.receiverId)

      // 跳过自己发送的消息回传（发送时已本地添加）
      if (senderId === currentUserId) return

      const friendId = senderId
      const message = {
        ...messageData,
        senderId,
        receiverId,
        isOwn: false
      }

      commit('ADD_PRIVATE_MESSAGE', { friendId, message })

      const currentCount = state.unreadCounts[friendId] || 0
      commit('UPDATE_UNREAD_COUNT', {
        chatId: friendId,
        count: currentCount + 1
      })
    } else if (type === 'GROUP_MESSAGE') {
      const senderId = toStr(messageData.senderId)
      const groupId = toStr(messageData.groupId)

      // 跳过自己发送的消息回传
      if (senderId === currentUserId) return

      const message = {
        ...messageData,
        senderId,
        groupId,
        isOwn: false
      }

      commit('ADD_GROUP_MESSAGE', { groupId, message })

      const currentCount = state.unreadCounts[groupId] || 0
      commit('UPDATE_UNREAD_COUNT', {
        chatId: groupId,
        count: currentCount + 1
      })
    }
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
