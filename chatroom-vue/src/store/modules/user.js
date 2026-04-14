import { login, logout, register, getCurrentUser } from '@/api/user'
import wsManager from '@/utils/websocket'

const state = {
  token: localStorage.getItem('token') || '',
  userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
  isLoggedIn: !!localStorage.getItem('token')
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
    localStorage.setItem('token', token)
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  },
  SET_LOGGED_IN(state, status) {
    state.isLoggedIn = status
  },
  CLEAR_USER(state) {
    state.token = ''
    state.userInfo = null
    state.isLoggedIn = false
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
}

const actions = {
  // 注册
  async register(context, userInfo) {
    const res = await register(userInfo)
    return res
  },

  // 登录
  async login({ commit }, loginForm) {
    const res = await login(loginForm)
    if (res.data && res.data.token) {
      commit('SET_TOKEN', res.data.token)
      // 后端返回的字段是 user，不是 userInfo
      commit('SET_USER_INFO', res.data.user || res.data.userInfo)
      commit('SET_LOGGED_IN', true)

      // 连接WebSocket
      const wsUrl = process.env.VUE_APP_WS_URL || 'ws://localhost:8080/ws'
      wsManager.connect(wsUrl, res.data.token)
    }
    return res
  },

  // 登出
  async logout({ commit }) {
    try {
      await logout()
    } catch (error) {
      // 即使后端登出失败，也清除本地状态
    } finally {
      // 关闭WebSocket连接
      wsManager.close()
      commit('CLEAR_USER')
    }
  },

  // 获取当前用户信息
  async getCurrentUser({ commit }) {
    const res = await getCurrentUser()
    if (res.data) {
      commit('SET_USER_INFO', res.data)
      commit('SET_LOGGED_IN', true)
    }
    return res
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

