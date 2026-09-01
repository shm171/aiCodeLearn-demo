// 认证与账号接口（路径已按 F:\软件大赛\1 后端对齐，联调时以 Swagger 为准）
import request from '@/utils/request'
import type { LoginReq, LoginResp, Profile, RegisterReq } from './schema'
import { mockGetProfile, mockLogin, mockUpdateProfile } from './mock'

// 认证接口是否使用本地 Mock：
// - 默认跟随 VITE_USE_MOCK（全局开关）
// - 想单独把“登录/档案”先切到真实后端，就在 .env.development 里设 VITE_USE_MOCK_AUTH=false
const USE_MOCK_AUTH = (import.meta.env.VITE_USE_MOCK_AUTH ?? import.meta.env.VITE_USE_MOCK) === 'true'

/** POST /login 邮箱 + 密码登录，返回 { token } */
export function login(data: LoginReq): Promise<LoginResp> {
  if (USE_MOCK_AUTH) return mockLogin(data)
  return request.post<unknown, LoginResp>('/login', data)
}

/**
 * POST /user/register 注册
 * 后端要求：role 必填（STUDENT/TEACHER），密码 8~16 位。
 * 教师端注册固定传 TEACHER。
 */
export function register(data: RegisterReq): Promise<unknown> {
  if (USE_MOCK_AUTH) return Promise.resolve({})
  return request.post<unknown, unknown>('/user/register', { ...data, role: 'TEACHER' })
}

/**
 * 获取当前登录用户（含角色）
 * ⚠️ 后端目前没有这个接口：现有的是 GET /user/{id}/profile（需要先知道自己的 id），
 * 而 JWT 里只存了邮箱、没有用户 id。需要后端新增 GET /user/me，
 * 带 JWT 返回 { id, email, username, role }。后端加上后，这里即可联调。
 */
export function getProfile(): Promise<Profile> {
  if (USE_MOCK_AUTH) return mockGetProfile()
  return request.get<unknown, Profile>('/user/me')
}

/**
 * 修改档案
 * 后端：PUT /user/{id}/profile，body 只支持 { username }（手机号/简介后端未支持）。
 */
export function updateProfile(userId: number, data: Partial<Profile>): Promise<Profile> {
  if (USE_MOCK_AUTH) return mockUpdateProfile(data)
  return request.put<unknown, Profile>(`/user/${userId}/profile`, { username: data.username })
}