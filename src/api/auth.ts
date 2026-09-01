// 认证与账号接口（路径按后端 base 模块对齐，联调以 Swagger 为准）
import request from '@/utils/request'
import type { LoginReq, LoginResp, Profile, RegisterReq } from './schema'
import { mockGetProfile, mockLogin, mockUpdateProfile } from './mock'

const USE_MOCK_AUTH = (import.meta.env.VITE_USE_MOCK_AUTH ?? import.meta.env.VITE_USE_MOCK) === 'true'

/** POST /login 邮箱 + 密码登录，返回 { token } */
export function login(data: LoginReq): Promise<LoginResp> {
  if (USE_MOCK_AUTH) return mockLogin(data)
  return request.post<unknown, LoginResp>('/login', data)
}

/** POST /user/register 注册（后端要求 role 必填，密码 8~16 位） */
export function register(data: RegisterReq): Promise<unknown> {
  if (USE_MOCK_AUTH) return Promise.resolve({})
  return request.post<unknown, unknown>('/user/register', data)
}

/**
 * 获取当前登录用户（含角色）
 * ⚠️ 后端目前没有 /user/me；暂用 POST /login/validate 校验 token，
 * 校验通过后返回演示管理员身份（临时方案，等后端提供用户信息接口后替换）。
 */
export async function getProfile(): Promise<Profile> {
  if (USE_MOCK_AUTH) return mockGetProfile()
  const res: unknown = await request.post('/login/validate')
  if (res === true) {
    return { id: 1, userId: 1, username: '管理员', email: '', role: 'ADMIN' }
  }
  throw new Error('Token 校验失败')
}

/** 修改档案（后端 PUT /user/{id}/profile，仅支持 username） */
export function updateProfile(userId: number, data: Partial<Profile>): Promise<Profile> {
  if (USE_MOCK_AUTH) return mockUpdateProfile(data)
  return request.put<unknown, Profile>(`/user/${userId}/profile`, { username: data.username })
}