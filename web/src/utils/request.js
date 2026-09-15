// Axios请求封装：统一配置baseURL、token、错误处理
import axios from 'axios'

const request = axios.create({
  // '/api' 开头走 vite.config.js 里配的代理，转发到 http://localhost:8080（解决跨域）
  baseURL: '/api',
  timeout: 60000, // 超时时间：60秒（批改涉及AI调用，比普通请求慢）
})

// 请求拦截器：发请求前自动加token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：成功直接返回后端数据；失败把错误抛给调用页
// 错误提示由页面自己弹（每个页面的提示文案不一样，统一弹会重复）
request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    // 401/403：token过期或无效（后端未认证统一返回403，登录密码错误返回401），
    // 清除登录信息并跳转登录页；登录接口本身的401由登录页自己提示
    const status = error.response?.status
    const isLoginRequest = error.config?.url?.includes('/login')
    if ((status === 401 || status === 403) && !isLoginRequest) {
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
