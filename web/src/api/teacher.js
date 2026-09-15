import request from '../utils/request'

// 教师端班级整体错题数据
export function getTeacherDashboardApi() {
  return request.get('/core/teacher/dashboard')
}