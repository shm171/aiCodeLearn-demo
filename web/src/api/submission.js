// 作业提交相关接口
import request from '../utils/request'

// 上传源码并完成批改：POST /core/submissions，multipart/form-data
// 参数：file（.cpp 或 .java 文件），需要登录
// 一步完成上传+批改，返回 { sourceFile: {...}, gradingResult: {...} }：
// gradingResult = { errors, score, feedback, status, stages }
//   - errors：问题清单，每项 { category, errorType, errorCode, message, fixSuggestion, source, line: [行号] }
//   - score：总分 0~100
//   - status：COMPLETED（完整，成绩可信）/ PARTIAL / UNAVAILABLE
//   - stages：各检查阶段状态，全部 COMPLETED 时成绩才可采纳
export function uploadSubmissionApi(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/core/submissions', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 分页查询我的提交历史：GET /core/submissions，参数 page、size
// 返回 Spring 分页结构：{ content: [提交], totalElements, totalPages, ... }
// 提交字段：{ id, filename, language, chapter, content, submittedAt }
export function getSubmissionListApi(params = {}) {
  return request.get('/core/submissions', { params })
}

// 查询单条提交详情（含源码原文）：GET /core/submissions/{submissionId}
export function getSubmissionDetailApi(submissionId) {
  return request.get(`/core/submissions/${submissionId}`)
}
