// ============================================================
// 类型定义（占位）
// 说明：后端 /v3/api-docs 就绪后，可用 openapi-typescript 生成
// schema.d.ts 并替换本文件。当前字段以 README 与 Swagger 约定为准，
// 联调时若字段不一致，以实际 Swagger 返回为准修改。
// ============================================================

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

/** 用户角色（Profile 中的角色，不在 JWT 中） */
export type Role = 'STUDENT' | 'TEACHER' | 'ADMIN'

/** 用户档案 */
export interface Profile {
  id: number
  userId: number
  username: string
  email: string
  phone?: string
  avatar?: string
  role: Role
  bio?: string
}

/** 登录请求：POST /login */
export interface LoginReq {
  email: string
  password: string
}

/** 登录响应（JWT Token） */
export interface LoginResp {
  token: string
  tokenType?: string
  expiresIn?: number
}

/** 注册请求：POST /user/register */
export interface RegisterReq {
  email: string
  password: string
  username: string
  /** 后端必填：STUDENT 或 TEACHER */
  role: 'STUDENT' | 'TEACHER'
}

export type QuestionType = 'PROGRAMMING' | 'ASSIGNMENT' | 'CHOICE'
export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD'

/** 题目 / 作业 */
export interface Question {
  id: number
  title: string
  type: QuestionType
  difficulty: Difficulty
  knowledgePoint: string
  description: string
  /** 0=下架 1=上架 */
  status: 0 | 1
  submitCount: number
  /** 平均正确率 0-100 */
  accuracy: number
  createdAt: string
}

export interface QuestionQuery {
  page: number
  size: number
  keyword?: string
  type?: QuestionType | ''
  status?: 0 | 1 | ''
}

export interface QuestionPayload {
  title: string
  type: QuestionType
  difficulty: Difficulty
  knowledgePoint: string
  description: string
}

export type ReviewStatus = 'PENDING' | 'GRADED' | 'REJECTED'

/** 学生提交记录 */
export interface Submission {
  id: number
  studentId: number
  studentName: string
  className: string
  questionId: number
  questionTitle: string
  submittedAt: string
  status: ReviewStatus
  staticIssueCount: number
  llmScore: number | null
  durationSeconds: number
}

export interface SubmissionQuery {
  page: number
  size: number
  keyword?: string
  questionId?: number | ''
  status?: ReviewStatus | ''
}

/** 静态代码检查问题 */
export interface StaticIssue {
  id: number
  severity: 'ERROR' | 'WARNING' | 'INFO'
  rule: string
  message: string
  file: string
  line: number
  column: number
}

/** LLM 批改单项 */
export interface GradingItem {
  dimension: string
  score: number
  maxScore: number
  comment: string
}

/** 审阅详情（代码 + 静态检查 + LLM 批改 + 人工复核） */
export interface ReviewDetail {
  id: number
  studentName: string
  className: string
  questionTitle: string
  questionDescription: string
  submittedAt: string
  code: string
  language: string
  staticIssues: StaticIssue[]
  llmGrading: {
    totalScore: number
    maxScore: number
    summary: string
    items: GradingItem[]
  } | null
  manualScore: number | null
  manualComment: string
  status: ReviewStatus
}

/** 人工复核提交 */
export interface ReviewPayload {
  score: number
  comment: string
  status: ReviewStatus
}

/** 错题记录 */
export interface WrongQuestion {
  id: number
  studentName: string
  className: string
  questionTitle: string
  knowledgePoint: string
  errorType: string
  errorCount: number
  lastOccurredAt: string
}

export interface WrongQuery {
  page: number
  size: number
  keyword?: string
  knowledgePoint?: string
}

/** 学生（教师视角） */
export interface Student {
  id: number
  name: string
  className: string
  submitCount: number
  avgAccuracy: number
  wrongCount: number
  activeDays: number
  lastActiveAt: string
}

export interface StudentQuery {
  page: number
  size: number
  keyword?: string
  className?: string
}

/** 趋势点 */
export interface TrendPoint {
  date: string
  submitCount: number
  accuracy: number
}

export interface KnowledgePointStat {
  knowledgePoint: string
  wrongCount: number
}

export interface ClassActivity {
  className: string
  submitCount: number
  activeStudents: number
}

/** 教师数据看板统计 */
export interface DashboardStats {
  studentCount: number
  todaySubmit: number
  pendingReview: number
  avgAccuracy: number
  activeStudents: number
  submitTrend: TrendPoint[]
  wrongDistribution: KnowledgePointStat[]
  classActivity: ClassActivity[]
  recentSubmissions: Submission[]
}

/** 学生学习报告 */
export interface StudentReport {
  studentId: number
  name: string
  className: string
  trend: TrendPoint[]
  /** 知识点掌握度（雷达图，0-100） */
  knowledge: Array<{ name: string; value: number }>
  wrongDistribution: KnowledgePointStat[]
  recentSubmissions: Submission[]
}