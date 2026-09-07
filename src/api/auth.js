// 认证与账号接口（路径已按后端对齐，联调以 Swagger 为准）
import request from '@/utils/request'
import { mockGetProfile, mockLogin, mockUpdateProfile } from './mock'

const USE_MOCK_AUTH = (import.meta.env.VITE_USE_MOCK_AUTH ?? import.meta.env.VITE_USE_MOCK) === 'true'

// POST /login 邮箱 + 密码登录，返回 { token }
export function login(data) {
  if (USE_MOCK_AUTH) return mockLogin(data)
  return request.post('/login', data)
}

// POST /user/register 注册（后端要求 role 必填，密码 8~16 位）
export function register(data) {
  if (USE_MOCK_AUTH) return Promise.resolve({})
  return request.post('/user/register', { ...data, role: 'TEACHER' })
}

// 获取当前登录用户（含角色）
// ⚠️ 后端目前没有 /user/me；暂用 POST /login/validate 校验 token，
// 校验通过后返回演示教师身份（临时方案，等后端提供用户信息接口后替换）。
export async function getProfile() {
  if (USE_MOCK_AUTH) return mockGetProfile()
  const res = await request.post('/login/validate')
  if (res === true) {
    return { id: 0, userId: 0, username: '演示教师', email: '', role: 'TEACHER' }
  }
  throw new Error('Token 校验失败')
}

// 修改档案（后端 PUT /user/{id}/profile，仅支持 username）
export function updateProfile(userId, data) {
  if (USE_MOCK_AUTH) return mockUpdateProfile(data)
  return request.put(`/user/${userId}/profile`, { username: data.username })
}