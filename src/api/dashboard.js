// 教师数据看板统计接口（适配后端 GET /core/teacher/dashboard?classId=）
import request from '@/utils/request'
import { mockGetDashboardStats } from './mock'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

export function getDashboardStats(classId) {
  if (USE_MOCK) return mockGetDashboardStats()
  return request.get('/core/teacher/dashboard', {
    params: classId ? { classId } : {},
  })
}