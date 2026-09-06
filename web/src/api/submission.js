// 作业提交相关接口
import request from '../utils/request'

// 上传源码文件：POST /core/submissions，multipart/form-data
// 参数：file（.cpp 或 .java 文件），需要登录
export function uploadSubmissionApi(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/core/submissions', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 触发AI批改：POST /core/submissions/{submissionId}/grade
// 返回：得分、错误列表、整体评语
export function gradeSubmissionApi(submissionId) {
  return request.post(`/core/submissions/${submissionId}/grade`)
}
