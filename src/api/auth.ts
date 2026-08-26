// 认证与账号接口（以 Swagger 实际路径为准）
import request from '@/utils/request'
import type { LoginReq, LoginResp, Profile, RegisterReq } from './schema'
import { mockGetProfile, mockLogin, mockUpdateProfile } from './mock'

// 认证接口是否使用本地 Mock：
// - 默认跟随 VITE_USE_MOCK（全局开关）
// - 想单独把“登录/档案”先切到真实后端，就在 .env.development 里设 VITE_USE_MOCK_AUTH=false
const USE_MOCK_AUTH = (import.meta.env.VITE_USE_MOCK_AUTH ?? import.meta.env.VITE_USE_MOCK) === 'true'

/**
 * POST /login 邮箱 + 密码登录，返回 JWT（真实后端）
 * 1. 路径：README 约定为 POST /login；若你的后端不同，改下面的 '/login'。
 * 2. 返回：默认按 { token: '...' } 处理（stores/user.ts 读 resp.token）。
 *    若后端返回 accessToken，或包装成 { code, message, data }，
 *    在 utils/request.ts 的响应拦截器里统一解包。
 */
export function login(data: LoginReq): Promise<LoginResp> {
  if (USE_MOCK_AUTH) return mockLogin(data)
  return request.post<unknown, LoginResp>('/login', data)
}

/** POST /user/register 注册（教师端一般不需要，保留供联调） */
export function register(data: RegisterReq): Promise<unknown> {
  if (USE_MOCK_AUTH) return Promise.resolve({})
  return request.post<unknown, unknown>('/user/register', data)
}

/** 获取当前用户 Profile（含角色；角色不在 JWT 中，需从此接口查询） */
export function getProfile(): Promise<Profile> {
  if (USE_MOCK_AUTH) return mockGetProfile()
  return request.get<unknown, Profile>('/user/profile')
}

/** 修改当前用户档案 */
export function updateProfile(data: Partial<Profile>): Promise<Profile> {
  if (USE_MOCK_AUTH) return mockUpdateProfile(data)
  return request.put<unknown, Profile>('/user/profile', data)
}