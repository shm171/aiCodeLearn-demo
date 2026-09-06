// 错题本/学习报告相关接口
import request from '../utils/request'

// 获取错题列表：GET /core/review/errors
// 支持筛选参数：language, category, severity, mastered
export function getErrorListApi(params = {}) {
  return request.get('/core/review/errors', { params })
}

// 获取错题详情：GET /core/review/errors/{errorId}
export function getErrorDetailApi(errorId) {
  return request.get(`/core/review/errors/${errorId}`)
}

// 获取复习包：GET /core/review/package
export function getReviewPackageApi() {
  return request.get('/core/review/package')
}

// 获取薄弱知识点：GET /core/review/weak-topics
export function getWeakTopicsApi() {
  return request.get('/core/review/weak-topics')
}
