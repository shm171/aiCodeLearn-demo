// 错题本/学习报告相关接口
import request from '../utils/request'

// 获取错题列表（分页）：GET /core/review/errors
// 参数：category（FORMAT_ERROR / SYNTAX_ERROR / LOGIC_ERROR，可选）、page、size
// 返回 Spring 分页结构：{ content: [错题], totalElements, totalPages, ... }
// 错题字段：{ id, chapter, category, errorType, errorCode, fixSuggestion, line: [行号], createdAt, mastered }
export function getErrorListApi(params = {}) {
  return request.get('/core/review/errors', { params })
}

// 获取错题详情：GET /core/review/errors/{errorId}
export function getErrorDetailApi(errorId) {
  return request.get(`/core/review/errors/${errorId}`)
}

// 标记已掌握：POST /core/review/errors/{errorId}/mastered，返回更新后的错题记录
export function markMasteredApi(errorId) {
  return request.post(`/core/review/errors/${errorId}/mastered`)
}

// 学习报告（复习包）：GET /core/review/package
// 返回：{ distribution, topWeakPoints, practiceList, studentCurve,
//         totalErrors, totalSubmissions, accuracyRate, summary }
// accuracyRate 是 0.0~1.0 的小数，页面显示百分比时要乘以 100
export function getReportApi() {
  return request.get('/core/review/package')
}

// 获取薄弱知识点：GET /core/review/weak-topics
// 返回数组，每项 { chapter, category, errorType, count, weight, mastery, lastSeenAt }
// mastery 是 0.0~1.0 的小数，显示百分比时乘以 100
export function getWeakTopicsApi() {
  return request.get('/core/review/weak-topics')
}
