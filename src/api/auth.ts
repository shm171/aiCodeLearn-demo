// 认证与账号接口（以 Swagger 实际路径为准）
import request from '@/utils/request'
import type { LoginReq, LoginResp, Profile, RegisterReq } from './schema'
import { mockGetProfile, mockLogin, mockUpdateProfile } from './mock'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

/** POST /login 邮箱 + 密码登录，返回 JWT */
export function login(data: LoginReq): Promise<LoginResp> {
  if (USE_MOCK) return mockLogin(data)
  return request.post<unknown, LoginResp>('/login', data)
}

/** POST /user/register 注册（教师端一般不需要，保留供联调） */
export function register(data: RegisterReq): Promise<unknown> {
  if (USE_MOCK) return Promise.resolve({})
  return request.post<unknown, unknown>('/user/register', data)
}

/** 获取当前用户 Profile（含角色；角色不在 JWT 中，需从此接口查询） */
export function getProfile(): Promise<Profile> {
  if (USE_MOCK) return mockGetProfile()
  return request.get<unknown, Profile>('/user/profile')
}

/** 修改当前用户档案 */
export function updateProfile(data: Partial<Profile>): Promise<Profile> {
  if (USE_MOCK) return mockUpdateProfile(data)
  return request.put<unknown, Profile>('/user/profile', data)
}