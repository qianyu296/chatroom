import axios from 'axios'
import { Message } from 'element-ui'
import store from '../store'
import router from '../router'

// 创建axios实例
const service = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从store中获取token
    const token = store.state.user.token
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果后端返回的状态码不是200（支持字符串"200"和数字200），则视为错误
    const code = res.code
    const isSuccess = code === 200 || code === '200' || code === undefined
    
    if (!isSuccess) {
      Message.error(res.message || '请求失败')
      
      // 401: 未授权，跳转到登录页
      if (code === 401 || code === '401') {
        store.dispatch('user/logout')
        router.push('/login')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    Message.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service

