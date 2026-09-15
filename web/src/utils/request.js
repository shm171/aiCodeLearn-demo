// Axios请求封装：统一配置baseURL、token、错误处理
import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 60000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error.response?.status
    const isLoginRequest = error.config?.url?.includes('/login')

    // 401才是凭证失效；403可能只是无权限访问某个资源
    if (status === 401 && !isLoginRequest) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('email')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }

    return Promise.reject(error)
  }
)

export default request