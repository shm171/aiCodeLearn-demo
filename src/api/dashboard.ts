// 教师数据看板统计接口
import request from '@/utils/request'
import type { DashboardStats } from './schema'
import { mockGetDashboardStats } from './mock'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

export function getDashboardStats(): Promise<DashboardStats> {
  if (USE_MOCK) return mockGetDashboardStats()
  return request.get<unknown, DashboardStats>('/teacher/dashboard/stats')
}