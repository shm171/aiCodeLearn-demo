// 管理端业务接口（后端管理端接口未实现，当前路径为示例，以 Swagger 为准）
import request from '@/utils/request'
import type {
  AdminStats,
  AdminUser,
  PageResult,
  PlatformConfig,
  RoleOption,
  UserQuery,
} from './schema'
import {
  mockCreateUser,
  mockDeleteUser,
  mockGetAdminStats,
  mockGetConfig,
  mockGetRoles,
  mockGetUserList,
  mockUpdateConfig,
  mockUpdateUserRole,
} from './mock'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

export function getAdminStats(): Promise<AdminStats> {
  if (USE_MOCK) return mockGetAdminStats()
  return request.get<unknown, AdminStats>('/admin/stats')
}

export function getUserList(params: UserQuery): Promise<PageResult<AdminUser>> {
  if (USE_MOCK) return mockGetUserList(params)
  return request.get<unknown, PageResult<AdminUser>>('/admin/users', { params })
}

export function createUser(data: { email: string; password: string; username: string; role: string }): Promise<AdminUser> {
  if (USE_MOCK) return mockCreateUser(data)
  return request.post<unknown, AdminUser>('/admin/users', data)
}

export function updateUserRole(id: number, role: string): Promise<AdminUser> {
  if (USE_MOCK) return mockUpdateUserRole(id, role)
  return request.put<unknown, AdminUser>(`/admin/users/${id}/role`, { role })
}

export function deleteUser(id: number): Promise<void> {
  if (USE_MOCK) return mockDeleteUser(id)
  return request.delete<unknown, void>(`/admin/users/${id}`)
}

export function getRoles(): Promise<RoleOption[]> {
  if (USE_MOCK) return mockGetRoles()
  return request.get<unknown, RoleOption[]>('/admin/roles')
}

export function getConfig(): Promise<PlatformConfig> {
  if (USE_MOCK) return mockGetConfig()
  return request.get<unknown, PlatformConfig>('/admin/config')
}

export function updateConfig(data: PlatformConfig): Promise<PlatformConfig> {
  if (USE_MOCK) return mockUpdateConfig(data)
  return request.put<unknown, PlatformConfig>('/admin/config', data)
}