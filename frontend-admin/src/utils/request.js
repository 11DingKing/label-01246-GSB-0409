import axios from 'axios'
import { Message } from '@arco-design/web-vue'
import router from '@/router'
import { sanitizeObject, containsXss } from '@/utils/xss'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest'  // CSRF 防护：标识 AJAX 请求
  }
})

// CSRF Token 缓存
let csrfToken = ''

/**
 * 获取 CSRF Token
 */
async function fetchCsrfToken() {
  try {
    const res = await axios.get('/api/auth/csrf-token', {
      headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    csrfToken = res.data?.data || res.headers['x-csrf-token'] || ''
  } catch (e) {
    // CSRF token 获取失败不阻塞业务
    console.warn('Failed to fetch CSRF token:', e.message)
  }
}

// 初始化时获取 CSRF Token
fetchCsrfToken()

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加 JWT Token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 对写操作添加 CSRF Token
    const method = config.method?.toUpperCase()
    if (method === 'POST' || method === 'PUT' || method === 'DELETE' || method === 'PATCH') {
      if (csrfToken) {
        config.headers['X-CSRF-Token'] = csrfToken
      }

      // XSS 防护：对请求数据进行检测和清理
      if (config.data && typeof config.data === 'object') {
        config.data = sanitizeObject(config.data)
      }
    }

    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 如果响应头中有新的 CSRF Token，更新缓存
    const newCsrfToken = response.headers['x-csrf-token']
    if (newCsrfToken) {
      csrfToken = newCsrfToken
    }

    const res = response.data
    if (res.code !== 200) {
      Message.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      // CSRF 校验失败时刷新 Token
      if (res.code === 403 && res.message?.includes('CSRF')) {
        fetchCsrfToken()
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
      Message.error('登录已过期，请重新登录')
    } else if (error.response?.status === 403) {
      Message.error('请求被拒绝，请刷新页面重试')
      fetchCsrfToken()
    } else {
      Message.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
