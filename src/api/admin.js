// 管理端业务接口（后端管理端接口未实现，当前路径为示例，以 Swagger 为准）
import request from '@/utils/request'
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

export function getAdminStats() {
  if (USE_MOCK) return mockGetAdminStats()
  return request.get('/admin/stats')
}

export function getUserList(params) {
  if (USE_MOCK) return mockGetUserList(params)
  return request.get('/admin/users', { params })
}

export function createUser(data) {
  if (USE_MOCK) return mockCreateUser(data)
  return request.post('/admin/users', data)
}

export function updateUserRole(id, role) {
  if (USE_MOCK) return mockUpdateUserRole(id, role)
  return request.put(`/admin/users/${id}/role`, { role })
}

export function deleteUser(id) {
  if (USE_MOCK) return mockDeleteUser(id)
  return request.delete(`/admin/users/${id}`)
}

export function getRoles() {
  if (USE_MOCK) return mockGetRoles()
  return request.get('/admin/roles')
}

export function getConfig() {
  if (USE_MOCK) return mockGetConfig()
  return request.get('/admin/config')
}

export function updateConfig(data) {
  if (USE_MOCK) return mockUpdateConfig(data)
  return request.put('/admin/config', data)
}