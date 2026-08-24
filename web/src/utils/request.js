// Axios请求封装：统一配置baseURL、token、错误处理
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
})

// 请求拦截器：发请求前自动加token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一处理返回数据和错误
request.interceptors.response.use(
  (response) => response.data, // 直接返回后端数据
  (error) => {
    // 优先显示后端返回的错误信息，没有就用默认文案
    ElMessage.error(error.response?.data?.message || '请求失败，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
