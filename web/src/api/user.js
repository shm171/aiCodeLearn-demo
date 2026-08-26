// 用户相关接口：页面里调用这里的函数发请求
import request from '../utils/request'

// 登录：POST /login，参数 email、password，成功返回 { token: "..." }
export function loginApi(email, password) {
  return request.post('/login', { email, password })
}

// 注册：POST /user/register，role 填 "STUDENT"（学员）或 "TEACHER"（老师）
export function registerApi(email, password, username, role) {
  return request.post('/user/register', { email, password, username, role })
}
