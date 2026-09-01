// 教师数据看板统计接口（适配 teacher 分支：GET /core/teacher/dashboard?classId=）
import request from '@/utils/request'
import type { DashboardStats } from './schema'
import { mockGetDashboardStats } from './mock'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

export function getDashboardStats(classId?: string): Promise<DashboardStats> {
  if (USE_MOCK) return mockGetDashboardStats()
  return request.get<unknown, DashboardStats>('/core/teacher/dashboard', {
    params: classId ? { classId } : {},
  })
}