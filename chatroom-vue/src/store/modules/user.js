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
    try {
      const res = await register(userInfo)
      return res
    } catch (error) {
      // 如果后端不可用，提示可以使用测试账户
      if (error.message && error.message.includes('Network Error')) {
        throw new Error('无法连接到服务器，请直接使用测试账户登录（用户名：testuser，密码：123456）')
      }
      throw error
    }
  },

  // 使用测试账户登录
  loginWithTestAccount({ commit }) {
    const testAccount = {
      username: 'testuser',
      password: '123456',
      token: 'test_token_' + Date.now(),
      userInfo: {
        id: 1,
        username: 'testuser',
        nickname: '测试用户',
        avatar: ''
      }
    }
    
    commit('SET_TOKEN', testAccount.token)
    commit('SET_USER_INFO', testAccount.userInfo)
    commit('SET_LOGGED_IN', true)
    
    // 尝试连接WebSocket（如果失败也不影响）
    const wsUrl = process.env.VUE_APP_WS_URL || 'ws://localhost:8080/ws'
    try {
      wsManager.connect(wsUrl, testAccount.token)
    } catch (error) {
      console.warn('WebSocket连接失败，将在后端可用时自动重连')
    }
    
    return {
      code: 200,
      message: '登录成功（测试账户）',
      data: {
        token: testAccount.token,
        userInfo: testAccount.userInfo
      }
    }
  },

  // 登录
  async login({ commit, dispatch }, loginForm) {
    // 如果是测试账户，直接使用测试登录
    if (loginForm.username === 'testuser' && loginForm.password === '123456') {
      return dispatch('loginWithTestAccount')
    }
    
    try {
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
    } catch (error) {
      // 如果后端不可用，提示可以使用测试账户
      if (error.message && error.message.includes('Network Error')) {
        throw new Error('无法连接到服务器，请使用测试账户登录（用户名：testuser，密码：123456）')
      }
      throw error
    }
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

