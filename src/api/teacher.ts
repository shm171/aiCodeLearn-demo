// 教师端业务接口（题目/作业、审阅、错题、学生报告）
// 说明：接口路径为示例，联调时以 Swagger 实际路径为准（/core 前缀对应 README 后续 AI 核心模块）。
import request from '@/utils/request'
import type {
  PageResult,
  Question,
  GradingResult,
  QuestionPayload,
  QuestionQuery,
  ReviewDetail,
  Student,
  StudentQuery,
  StudentReport,
  Submission,
  SubmissionQuery,
  WrongQuestion,
  WrongQuery,
} from './schema'
import {
  mockCreateQuestion,
  mockDeleteQuestion,
  mockExportWrongQuestions,
  mockGetQuestionList,
  mockGetReviewDetail,
  mockGetStudentList,
  mockGetStudentReport,
  mockGetSubmissionList,
  mockGetWrongQuestions,
  mockGradeSubmission,
  mockUpdateQuestion,
  mockUpdateQuestionStatus,
} from './mock'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// ---------- 题目与作业管理 ----------
export function getQuestionList(params: QuestionQuery): Promise<PageResult<Question>> {
  if (USE_MOCK) return mockGetQuestionList(params)
  return request.get<unknown, PageResult<Question>>('/core/question/list', { params })
}

export function createQuestion(data: QuestionPayload): Promise<Question> {
  if (USE_MOCK) return mockCreateQuestion(data)
  return request.post<unknown, Question>('/core/question', data)
}

export function updateQuestion(id: number, data: QuestionPayload): Promise<Question> {
  if (USE_MOCK) return mockUpdateQuestion(id, data)
  return request.put<unknown, Question>(`/core/question/${id}`, data)
}

export function updateQuestionStatus(id: number, status: 0 | 1): Promise<void> {
  if (USE_MOCK) return mockUpdateQuestionStatus(id, status)
  return request.put<unknown, void>(`/core/question/${id}/status`, { status })
}

export function deleteQuestion(id: number): Promise<void> {
  if (USE_MOCK) return mockDeleteQuestion(id)
  return request.delete<unknown, void>(`/core/question/${id}`)
}

// ---------- 学生提交审阅 ----------
export function getSubmissionList(params: SubmissionQuery): Promise<PageResult<Submission>> {
  if (USE_MOCK) return mockGetSubmissionList(params)
  return request.get<unknown, PageResult<Submission>>('/teacher/submissions', { params })
}

export function getReviewDetail(id: number): Promise<ReviewDetail> {
  if (USE_MOCK) return mockGetReviewDetail(id)
  return request.get<unknown, ReviewDetail>(`/teacher/submissions/${id}`)
}

/** 双层批改（规则校验 + 可选 LLM）——适配 granding 分支 POST /core/submissions/{submissionId}/grade */
export function gradeSubmission(submissionId: number, enableLLM = false): Promise<GradingResult> {
  if (USE_MOCK) return mockGradeSubmission(submissionId, enableLLM)
  return request.post<unknown, GradingResult>(`/core/submissions/${submissionId}/grade`, { enableLLM })
}

// ---------- 错题管理 ----------
export function getWrongQuestions(params: WrongQuery): Promise<PageResult<WrongQuestion>> {
  if (USE_MOCK) return mockGetWrongQuestions(params)
  return request.get<unknown, PageResult<WrongQuestion>>('/teacher/wrong-questions', { params })
}

/** 导出错题统计（文件流，防止中文文件名乱码需 responseType: blob） */
export function exportWrongQuestions(): Promise<Blob> {
  if (USE_MOCK) return mockExportWrongQuestions()
  return request.get<unknown, Blob>('/teacher/wrong-questions/export', { responseType: 'blob' })
}

// ---------- 学生与学习报告 ----------
export function getStudentList(params: StudentQuery): Promise<PageResult<Student>> {
  if (USE_MOCK) return mockGetStudentList(params)
  return request.get<unknown, PageResult<Student>>('/teacher/students', { params })
}

export function getStudentReport(id: number): Promise<StudentReport> {
  if (USE_MOCK) return mockGetStudentReport(id)
  return request.get<unknown, StudentReport>(`/teacher/students/${id}/report`)
}