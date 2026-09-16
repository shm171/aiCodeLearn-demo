// 用户相关接口：页面里调用这里的函数发请求
import request from '../utils/request'

// 登录：POST /login，参数 email、password，成功返回 { token: "..." }
export function loginApi(email, password) {
  return request.post('/login', { email, password })
}

// 注册：POST /user/register，平台只开放学员注册，role 固定传 "STUDENT"
// 成功返回 { id, email }，id 需要保存下来，之后查档案要用
export function registerApi(email, password, username, role) {
  return request.post('/user/register', { email, password, username, role })
}

// 校验token是否有效：POST /login/validate，返回 true / false
export function validateTokenApi() {
  return request.post('/login/validate')
}

// 获取用户信息：GET /user/{id}，返回 { id, email }
export function getUserInfoApi(userId) {
  return request.get(`/user/${userId}`)
}

// 修改用户信息（邮箱/密码）：PUT /user/{id}，参数 { email, password }
export function updateUserInfoApi(userId, data) {
  return request.put(`/user/${userId}`, data)
}

// 获取用户档案：GET /user/{id}/profile，返回 { userId, username, role, createdAt }
export function getUserProfileApi(userId) {
  return request.get(`/user/${userId}/profile`)
}

// 修改用户档案（用户名）：PUT /user/{id}/profile，参数 { username }
export function updateUserProfileApi(userId, username) {
  return request.put(`/user/${userId}/profile`, { username })
}
