// Axios请求封装：统一配置baseURL、token、错误处理
import axios from 'axios'

const request = axios.create({
  // '/api' 开头走 vite.config.js 里配的代理，转发到 http://localhost:8080（解决跨域）
  baseURL: '/api',
  timeout: 10000, // 超时时间：10秒
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
  (error) => Promise.reject(error)
)

export default request
