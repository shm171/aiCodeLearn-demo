// ============================================================
// 本地 Mock 数据层（后端管理端接口未实现前，先用于页面开发与演示）
// ============================================================
import type {
  AdminStats,
  AdminUser,
  LoginReq,
  LoginResp,
  PageResult,
  PlatformConfig,
  Profile,
  RegisterReq,
  RoleOption,
  UserQuery,
} from './schema'

export function delay<T>(data: T, ms = 300): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(structuredClone(data)), ms))
}

const MOCK_ADMIN: Profile = {
  id: 1,
  userId: 1,
  username: '管理员',
  email: 'admin@ailearn.com',
  role: 'ADMIN',
}

const DEMO_ACCOUNTS: Record<string, { password: string; profile: Profile }> = {
  'admin@ailearn.com': { password: '123456', profile: MOCK_ADMIN },
  'teacher@ailearn.com': {
    password: '123456',
    profile: { id: 2, userId: 2, username: '王老师', email: 'teacher@ailearn.com', role: 'TEACHER' },
  },
  'student@ailearn.com': {
    password: '123456',
    profile: { id: 3, userId: 3, username: '李想', email: 'student@ailearn.com', role: 'STUDENT' },
  },
}

let currentEmail = ''

export function mockLogin(data: LoginReq): Promise<LoginResp> {
  const account = DEMO_ACCOUNTS[data.email]
  if (!account || account.password !== data.password) {
    return Promise.reject(new Error('邮箱或密码错误'))
  }
  currentEmail = data.email
  return delay({ token: `mock-jwt-token-${Date.now()}`, tokenType: 'Bearer', expiresIn: 86400 })
}

export function mockGetProfile(): Promise<Profile> {
  const account = DEMO_ACCOUNTS[currentEmail] || DEMO_ACCOUNTS['admin@ailearn.com']
  return delay(account.profile)
}

export function mockUpdateProfile(data: Partial<Profile>): Promise<Profile> {
  const account = DEMO_ACCOUNTS[currentEmail] || DEMO_ACCOUNTS['admin@ailearn.com']
  Object.assign(account.profile, data)
  return delay(account.profile)
}

export function mockRegister(data: RegisterReq): Promise<{ id: number; email: string }> {
  return delay({ id: 100, email: data.email })
}

// -------------------- 用户管理 --------------------

const users: AdminUser[] = [
  { id: 1, email: 'admin@ailearn.com', username: '管理员', role: 'ADMIN', status: 1, createdAt: '2026-07-01 09:00' },
  { id: 2, email: 'teacher@ailearn.com', username: '王老师', role: 'TEACHER', status: 1, createdAt: '2026-07-02 10:20' },
  { id: 3, email: 'teacher2@ailearn.com', username: '李老师', role: 'TEACHER', status: 1, createdAt: '2026-07-05 14:10' },
  { id: 4, email: 'student@ailearn.com', username: '李想', role: 'STUDENT', status: 1, createdAt: '2026-07-08 08:30' },
  { id: 5, email: 'student2@ailearn.com', username: '张雨桐', role: 'STUDENT', status: 1, createdAt: '2026-07-09 11:45' },
  { id: 6, email: 'student3@ailearn.com', username: '陈一鸣', role: 'STUDENT', status: 0, createdAt: '2026-07-12 16:00' },
  { id: 7, email: 'teacher3@ailearn.com', username: '赵老师', role: 'TEACHER', status: 1, createdAt: '2026-07-15 09:25' },
  { id: 8, email: 'student4@ailearn.com', username: '刘思远', role: 'STUDENT', status: 1, createdAt: '2026-07-18 13:50' },
]

export function mockGetUserList(query: UserQuery): Promise<PageResult<AdminUser>> {
  let list = [...users]
  if (query.keyword) {
    const kw = query.keyword.toLowerCase()
    list = list.filter(
      (u) => u.username.toLowerCase().includes(kw) || u.email.toLowerCase().includes(kw)
    )
  }
  if (query.role) list = list.filter((u) => u.role === query.role)
  const total = list.length
  const start = (query.page - 1) * query.size
  return delay({ list: list.slice(start, start + query.size), total, page: query.page, size: query.size })
}

export function mockCreateUser(data: { email: string; password: string; username: string; role: string }): Promise<AdminUser> {
  const item: AdminUser = {
    id: Math.max(0, ...users.map((u) => u.id)) + 1,
    email: data.email,
    username: data.username,
    role: data.role as AdminUser['role'],
    status: 1,
    createdAt: new Date().toISOString().slice(0, 16).replace('T', ' '),
  }
  users.unshift(item)
  return delay(item)
}

export function mockUpdateUserRole(id: number, role: string): Promise<AdminUser> {
  const u = users.find((x) => x.id === id)
  if (!u) throw new Error('用户不存在')
  u.role = role as AdminUser['role']
  return delay(u)
}

export function mockDeleteUser(id: number): Promise<void> {
  const idx = users.findIndex((u) => u.id === id)
  if (idx >= 0) users.splice(idx, 1)
  return delay(undefined)
}

export function mockGetAdminStats(): Promise<AdminStats> {
  return delay({
    totalUsers: users.length,
    teacherCount: users.filter((u) => u.role === 'TEACHER').length,
    studentCount: users.filter((u) => u.role === 'STUDENT').length,
    adminCount: users.filter((u) => u.role === 'ADMIN').length,
    recentUsers: users.slice(0, 5),
    roleDistribution: [
      { name: '学生', value: users.filter((u) => u.role === 'STUDENT').length },
      { name: '教师', value: users.filter((u) => u.role === 'TEACHER').length },
      { name: '管理员', value: users.filter((u) => u.role === 'ADMIN').length },
    ],
  })
}

export function mockGetRoles(): Promise<RoleOption[]> {
  return delay([
    { role: 'ADMIN', name: '管理员', description: '平台级管理：用户、角色与配置管理', userCount: users.filter((u) => u.role === 'ADMIN').length, permissions: ['用户管理', '角色管理', '基础配置', '数据看板'] },
    { role: 'TEACHER', name: '教师', description: '教学管理：看板、审阅、错题、学习报告', userCount: users.filter((u) => u.role === 'TEACHER').length, permissions: ['教师看板', '题目/作业管理', '提交审阅', '错题管理', '学生报告'] },
    { role: 'STUDENT', name: '学生', description: '学习端：上传、作答、错题本、学习报告', userCount: users.filter((u) => u.role === 'STUDENT').length, permissions: ['文件上传', '题目作答', '静态检查', 'LLM 批改', '错题本', '学习报告'] },
  ])
}

let config: PlatformConfig = {
  platformName: 'AI Learn 智能学习平台',
  allowRegister: true,
  defaultRole: 'STUDENT',
  classes: ['计算机 2101', '计算机 2102', '计算机 2103', '软件 2101'],
}

export function mockGetConfig(): Promise<PlatformConfig> {
  return delay(config)
}

export function mockUpdateConfig(data: PlatformConfig): Promise<PlatformConfig> {
  config = { ...data }
  return delay(config)
}