// ============================================================
// 请求层（Axios 实例 + JWT 拦截器）
// 说明：按前端分工，请求层由前端工程师 A 主责维护，本文件为
// B 在 A 未就绪时提供的临时实现，后续并入 A 的版本即可。
// - 请求拦截器：从 localStorage 读取 token，写入 Authorization: Bearer <token>
// - 响应拦截器：401 时清空 token 并跳转登录页
// ============================================================
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  // 后端直接返回业务数据（如 PageResult、Profile、{token}）
  // 若后端统一包装为 { code, message, data }，请在此处返回 response.data.data
  (response) => response.data,
  (error) => {
    const status = error.response?.status
    const msg =
      error.response?.data?.message || error.response?.data?.error || error.message || '请求失败'
    if (status === 401) {
      localStorage.removeItem('token')
      if (router.currentRoute.value.path !== '/login') {
        ElMessage.error('登录已过期，请重新登录')
        router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
      }
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default request