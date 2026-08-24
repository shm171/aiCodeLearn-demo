// ============================================================
// 网络请求封装（Axios）
// 作用：把 axios 统一配置好，以后所有页面都用这个 request 发请求。
// 好处：不用在每个页面重复写服务器地址、token、错误提示等。
//
// 使用方法（在任意页面里）：
//   import request from '../utils/request'
//   const data = await request.get('/courses')        // GET 请求
//   const data = await request.post('/login', {...})  // POST 请求
// ============================================================

import axios from 'axios'
import { ElMessage } from 'element-plus' // 用 Element Plus 的消息弹窗来提示错误

// 创建一个"配置好"的 axios 实例
const request = axios.create({
  // baseURL：所有请求的地址都会自动加上这个前缀
  // 直接写后端地址，开发阶段前后端分开运行
  baseURL: 'http://localhost:8080',
  timeout: 10000, // 超时时间（毫秒）：10 秒没收到响应就算失败
})

// ========== 请求拦截器 ==========
// 每次"发送请求之前"都会先执行这里
request.interceptors.request.use((config) => {
  // 从 localStorage 取出 token（登录时保存的）
  const token = localStorage.getItem('token')
  if (token) {
    // 把 token 放进请求头里，后端靠它识别"你是谁"
    // Bearer 是一种常见的 token 写法，等后端接口定下来后按后端要求改
    config.headers.Authorization = `Bearer ${token}`
  }
  return config // 必须返回 config，请求才会继续发出去
})

// ========== 响应拦截器 ==========
// 每次"收到响应之后"都会先执行这里
request.interceptors.response.use(
  // ----- 请求成功的情况 -----
  // 直接返回 response.data（后端真正返回的数据）
  // 所以调用时 const data = await request.get(...)，data 就是后端的数据
  (response) => response.data,

  // ----- 请求失败的情况 -----
  (error) => {
    // 弹出错误提示：优先显示后端给的错误信息，没有就用默认文案
    // error.response 是后端返回的错误响应，?. 表示"有才取，没有就是 undefined"
    ElMessage.error(error.response?.data?.message || '请求失败，请稍后重试')
    return Promise.reject(error) // 继续把错误抛给调用方（调用方可以 try...catch 处理）
  }
)

// 导出配置好的实例，给其他页面使用
export default request
