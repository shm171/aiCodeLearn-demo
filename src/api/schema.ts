// ============================================================
// 类型定义（占位）。后端 /v3/api-docs 就绪后可用 openapi-typescript 生成替换。
// ============================================================

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

export type Role = 'STUDENT' | 'TEACHER' | 'ADMIN'

export interface Profile {
  id: number
  userId: number
  username: string
  email: string
  role: Role
  createdAt?: string
}

export interface LoginReq {
  email: string
  password: string
}

export interface LoginResp {
  token: string
  tokenType?: string
  expiresIn?: number
}

export interface RegisterReq {
  email: string
  password: string
  username: string
  role: 'STUDENT' | 'TEACHER'
}

/** 管理端用户（对齐后端 UserDto/ProfileDto） */
export interface AdminUser {
  id: number
  email: string
  username: string
  role: Role
  status: 0 | 1
  createdAt: string
}

export interface UserQuery {
  page: number
  size: number
  keyword?: string
  role?: Role | ''
}

export interface RoleOption {
  role: Role
  name: string
  description: string
  userCount: number
  permissions: string[]
}

/** 管理端概览统计 */
export interface AdminStats {
  totalUsers: number
  teacherCount: number
  studentCount: number
  adminCount: number
  recentUsers: AdminUser[]
  roleDistribution: Array<{ name: string; value: number }>
}

export interface PlatformConfig {
  platformName: string
  allowRegister: boolean
  defaultRole: 'STUDENT' | 'TEACHER'
  classes: string[]
}