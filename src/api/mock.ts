// ============================================================
// 本地 Mock 数据层
// 用途：后端接口未就绪时，前端先用 Mock 数据开发页面
//（与《前端工程师B工作流程详解》约定一致）。
// 联调时把 .env 中 VITE_USE_MOCK 改为 false 即可切换真实接口。
// ============================================================
import type {
  DashboardStats,
  LoginReq,
  LoginResp,
  PageResult,
  Profile,
  Question,
  QuestionPayload,
  QuestionQuery,
  ReviewDetail,
  StaticIssue,
  ReviewPayload,
  Student,
  StudentQuery,
  StudentReport,
  Submission,
  SubmissionQuery,
  WrongQuestion,
  WrongQuery,
} from './schema'

/** 模拟网络延迟 */
export function delay<T>(data: T, ms = 300): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(structuredClone(data)), ms))
}

export const MOCK_TEACHER: Profile = {
  id: 2,
  userId: 2,
  username: '王老师',
  email: 'teacher@ailearn.com',
  phone: '13800000002',
  role: 'TEACHER',
  bio: '负责算法与数据结构课程教学，喜欢用 AI 辅助批改。',
}

const DEMO_ACCOUNTS: Record<string, { password: string; profile: Profile }> = {
  'teacher@ailearn.com': { password: '123456', profile: MOCK_TEACHER },
  'admin@ailearn.com': {
    password: '123456',
    profile: {
      id: 1,
      userId: 1,
      username: '管理员',
      email: 'admin@ailearn.com',
      role: 'ADMIN',
    },
  },
}

export function mockLogin(data: LoginReq): Promise<LoginResp> {
  const account = DEMO_ACCOUNTS[data.email]
  if (!account || account.password !== data.password) {
    return delay(
      Promise.reject({ message: '邮箱或密码错误' }) as never,
      300
    ).then(
      () => {
        throw new Error('邮箱或密码错误')
      },
      (e) => {
        throw e
      }
    )
  }
  return delay({ token: `mock-jwt-token-${Date.now()}`, tokenType: 'Bearer', expiresIn: 86400 })
}

export function mockGetProfile(): Promise<Profile> {
  return delay(MOCK_TEACHER)
}

export function mockUpdateProfile(data: Partial<Profile>): Promise<Profile> {
  Object.assign(MOCK_TEACHER, data)
  return delay(MOCK_TEACHER)
}

// -------------------- 题目与作业管理 --------------------

const questions: Question[] = [
  { id: 1, title: '两数之和（数组哈希）', type: 'PROGRAMMING', difficulty: 'EASY', knowledgePoint: '数组 / 哈希表', description: '给定一个整数数组和一个目标值，找出和为目标值的两个数的下标。', status: 1, submitCount: 128, accuracy: 86.4, createdAt: '2026-07-01 10:20' },
  { id: 2, title: '反转链表', type: 'PROGRAMMING', difficulty: 'MEDIUM', knowledgePoint: '链表', description: '实现单链表反转，要求 O(n) 时间、O(1) 空间。', status: 1, submitCount: 96, accuracy: 72.1, createdAt: '2026-07-03 14:05' },
  { id: 3, title: '第 1 周算法作业：排序综合', type: 'ASSIGNMENT', difficulty: 'MEDIUM', knowledgePoint: '排序', description: '完成快速排序与归并排序的实现，并比较不同数据规模下的性能。', status: 1, submitCount: 84, accuracy: 68.9, createdAt: '2026-07-06 09:00' },
  { id: 4, title: '有效的括号', type: 'PROGRAMMING', difficulty: 'EASY', knowledgePoint: '栈', description: '给定只包含括号字符的字符串，判断括号是否匹配。', status: 1, submitCount: 152, accuracy: 91.2, createdAt: '2026-07-10 16:30' },
  { id: 5, title: '二叉树的层序遍历', type: 'PROGRAMMING', difficulty: 'MEDIUM', knowledgePoint: '树 / BFS', description: '按层输出二叉树节点，同层节点按从左到右排列。', status: 1, submitCount: 61, accuracy: 64.7, createdAt: '2026-07-15 11:40' },
  { id: 6, title: '第 2 周作业：动态规划入门', type: 'ASSIGNMENT', difficulty: 'HARD', knowledgePoint: '动态规划', description: '完成爬楼梯与最大子数组和两道题，并写出状态转移方程说明。', status: 0, submitCount: 42, accuracy: 55.3, createdAt: '2026-07-20 08:55' },
  { id: 7, title: '无重复字符的最长子串', type: 'PROGRAMMING', difficulty: 'HARD', knowledgePoint: '滑动窗口', description: '给定字符串，找出其中不含有重复字符的最长子串的长度。', status: 1, submitCount: 38, accuracy: 48.9, createdAt: '2026-07-25 15:15' },
  { id: 8, title: '编程基础选择测验（第一章）', type: 'CHOICE', difficulty: 'EASY', knowledgePoint: '基础语法', description: '覆盖变量、条件、循环的基础选择题 20 道。', status: 1, submitCount: 175, accuracy: 93.5, createdAt: '2026-08-01 09:30' },
]

export function mockGetQuestionList(query: QuestionQuery): Promise<PageResult<Question>> {
  let list = [...questions]
  if (query.keyword) {
    const kw = query.keyword.toLowerCase()
    list = list.filter(
      (q) => q.title.toLowerCase().includes(kw) || q.knowledgePoint.toLowerCase().includes(kw)
    )
  }
  if (query.type) list = list.filter((q) => q.type === query.type)
  if (query.status !== '' && query.status !== undefined) list = list.filter((q) => q.status === query.status)
  const total = list.length
  const start = (query.page - 1) * query.size
  return delay({ list: list.slice(start, start + query.size), total, page: query.page, size: query.size })
}

export function mockCreateQuestion(data: QuestionPayload): Promise<Question> {
  const item: Question = {
    id: Math.max(0, ...questions.map((q) => q.id)) + 1,
    ...data,
    status: 1,
    submitCount: 0,
    accuracy: 0,
    createdAt: new Date().toISOString().slice(0, 16).replace('T', ' '),
  }
  questions.unshift(item)
  return delay(item)
}

export function mockUpdateQuestion(id: number, data: QuestionPayload): Promise<Question> {
  const idx = questions.findIndex((q) => q.id === id)
  if (idx < 0) throw new Error('题目不存在')
  questions[idx] = { ...questions[idx], ...data }
  return delay(questions[idx])
}

export function mockUpdateQuestionStatus(id: number, status: 0 | 1): Promise<void> {
  const idx = questions.findIndex((q) => q.id === id)
  if (idx >= 0) questions[idx].status = status
  return delay(undefined)
}

export function mockDeleteQuestion(id: number): Promise<void> {
  const idx = questions.findIndex((q) => q.id === id)
  if (idx >= 0) questions.splice(idx, 1)
  return delay(undefined)
}

// -------------------- 学生提交审阅 --------------------

const submissions: Submission[] = [
  { id: 1, studentId: 11, studentName: '李想', className: '计算机 2101', questionId: 1, questionTitle: '两数之和（数组哈希）', submittedAt: '2026-08-25 09:12', status: 'PENDING', staticIssueCount: 3, llmScore: null, durationSeconds: 842 },
  { id: 2, studentId: 12, studentName: '张雨桐', className: '计算机 2101', questionId: 1, questionTitle: '两数之和（数组哈希）', submittedAt: '2026-08-25 09:40', status: 'GRADED', staticIssueCount: 0, llmScore: 92, durationSeconds: 610 },
  { id: 3, studentId: 13, studentName: '陈一鸣', className: '计算机 2102', questionId: 4, questionTitle: '有效的括号', submittedAt: '2026-08-25 10:05', status: 'PENDING', staticIssueCount: 1, llmScore: null, durationSeconds: 1205 },
  { id: 4, studentId: 14, studentName: '刘思远', className: '计算机 2102', questionId: 3, questionTitle: '第 1 周算法作业：排序综合', submittedAt: '2026-08-25 10:33', status: 'GRADED', staticIssueCount: 2, llmScore: 78, durationSeconds: 2200 },
  { id: 5, studentId: 15, studentName: '王梓涵', className: '软件 2101', questionId: 4, questionTitle: '有效的括号', submittedAt: '2026-08-25 11:20', status: 'REJECTED', staticIssueCount: 5, llmScore: 55, durationSeconds: 1500 },
  { id: 6, studentId: 11, studentName: '李想', className: '计算机 2101', questionId: 4, questionTitle: '有效的括号', submittedAt: '2026-08-25 14:02', status: 'GRADED', staticIssueCount: 0, llmScore: 96, durationSeconds: 980 },
  { id: 7, studentId: 16, studentName: '赵天佑', className: '软件 2101', questionId: 2, questionTitle: '反转链表', submittedAt: '2026-08-25 15:18', status: 'PENDING', staticIssueCount: 4, llmScore: null, durationSeconds: 1890 },
  { id: 8, studentId: 17, studentName: '孙悦', className: '计算机 2103', questionId: 2, questionTitle: '反转链表', submittedAt: '2026-08-25 16:45', status: 'GRADED', staticIssueCount: 1, llmScore: 84, durationSeconds: 1320 },
  { id: 9, studentId: 18, studentName: '周子墨', className: '计算机 2103', questionId: 7, questionTitle: '无重复字符的最长子串', submittedAt: '2026-08-26 08:30', status: 'PENDING', staticIssueCount: 2, llmScore: null, durationSeconds: 1655 },
  { id: 10, studentId: 12, studentName: '张雨桐', className: '计算机 2101', questionId: 3, questionTitle: '第 1 周算法作业：排序综合', submittedAt: '2026-08-26 09:05', status: 'GRADED', staticIssueCount: 0, llmScore: 90, durationSeconds: 1480 },
]

export function mockGetSubmissionList(query: SubmissionQuery): Promise<PageResult<Submission>> {
  let list = [...submissions]
  if (query.keyword) {
    const kw = query.keyword.toLowerCase()
    list = list.filter(
      (s) => s.studentName.toLowerCase().includes(kw) || s.questionTitle.toLowerCase().includes(kw)
    )
  }
  if (query.questionId) list = list.filter((s) => s.questionId === query.questionId)
  if (query.status) list = list.filter((s) => s.status === query.status)
  const total = list.length
  const start = (query.page - 1) * query.size
  return delay({ list: list.slice(start, start + query.size), total, page: query.page, size: query.size })
}

const STATIC_ISSUES: StaticIssue[] = [
  { id: 1, severity: 'ERROR', rule: 'no-unused-variable', message: '变量 tmp 已定义但从未使用', file: 'main.py', line: 5, column: 9 },
  { id: 2, severity: 'WARNING', rule: 'complexity', message: '函数复杂度为 3，建议拆分以提升可读性', file: 'main.py', line: 8, column: 1 },
  { id: 3, severity: 'INFO', rule: 'naming-convention', message: '变量名 a/b 语义不明确，建议使用有意义的名称', file: 'main.py', line: 12, column: 13 },
]
export function mockGetReviewDetail(id: number): Promise<ReviewDetail> {
  const sub = submissions.find((s) => s.id === id)
  if (!sub) throw new Error('提交记录不存在')
  const detail: ReviewDetail = {
    id: sub.id,
    studentName: sub.studentName,
    className: sub.className,
    questionTitle: sub.questionTitle,
    questionDescription: '给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出和为目标值 target 的那两个整数，并返回它们的数组下标。你可以假设每种输入只会对应一个答案。',
    submittedAt: sub.submittedAt,
    language: 'python',
    code: sub.id % 2 === 0
      ? `class Solution:
    def twoSum(self, nums: List[int], target: int) -> List[int]:
        seen = {}
        for i, num in enumerate(nums):
            if target - num in seen:
                return [seen[target - num], i]
            seen[num] = i
        return []`
      : `class Solution:
    def twoSum(self, nums: List[int], target: int) -> List[int]:
        # 暴力解法
        for i in range(len(nums)):
            for j in range(i + 1, len(nums)):
                if nums[i] + nums[j] == target:
                    return [i, j]
        return []`,
    staticIssues: sub.staticIssueCount > 0 ? STATIC_ISSUES.slice(0, sub.staticIssueCount) : [],
    llmGrading: {
      totalScore: sub.llmScore ?? 0,
      maxScore: 100,
      summary: '整体思路正确，能够通过题目要求的基本用例。建议补充边界条件测试（如重复元素、空数组），并优化变量命名。',
      items: [
        { dimension: '正确性', score: 28, maxScore: 30, comment: '主要用例通过，边界条件覆盖不足。' },
        { dimension: '算法效率', score: 26, maxScore: 30, comment: '哈希表方案 O(n) 优于暴力 O(n^2)，很好。' },
        { dimension: '代码规范', score: 16, maxScore: 20, comment: '缩进规范，但命名可再提升。' },
        { dimension: '可读性', score: 12, maxScore: 20, comment: '逻辑清晰，缺少必要的注释。' },
      ],
    },
    manualScore: null,
    manualComment: '',
    status: sub.status,
  }
  return delay(detail)
}

export async function mockSubmitReview(id: number, payload: ReviewPayload): Promise<ReviewDetail> {
  const sub = submissions.find((s) => s.id === id)
  if (sub) {
    sub.status = payload.status
    sub.llmScore = payload.score
  }
  const detail = await mockGetReviewDetail(id)
  detail.manualScore = payload.score
  detail.manualComment = payload.comment
  detail.status = payload.status
  return delay(detail)
}

// -------------------- 错题管理 --------------------

const wrongQuestions: WrongQuestion[] = [
  { id: 1, studentName: '李想', className: '计算机 2101', questionTitle: '反转链表', knowledgePoint: '链表', errorType: '空指针', errorCount: 4, lastOccurredAt: '2026-08-24' },
  { id: 2, studentName: '陈一鸣', className: '计算机 2102', questionTitle: '无重复字符的最长子串', knowledgePoint: '滑动窗口', errorType: '逻辑错误', errorCount: 3, lastOccurredAt: '2026-08-25' },
  { id: 3, studentName: '王梓涵', className: '软件 2101', questionTitle: '有效的括号', knowledgePoint: '栈', errorType: '边界条件', errorCount: 5, lastOccurredAt: '2026-08-25' },
  { id: 4, studentName: '赵天佑', className: '软件 2101', questionTitle: '二叉树的层序遍历', knowledgePoint: '树 / BFS', errorType: '超时', errorCount: 2, lastOccurredAt: '2026-08-22' },
  { id: 5, studentName: '周子墨', className: '计算机 2103', questionTitle: '第 2 周作业：动态规划入门', knowledgePoint: '动态规划', errorType: '状态转移错误', errorCount: 6, lastOccurredAt: '2026-08-26' },
  { id: 6, studentName: '孙悦', className: '计算机 2103', questionTitle: '两数之和（数组哈希）', knowledgePoint: '数组 / 哈希表', errorType: '思路偏差', errorCount: 1, lastOccurredAt: '2026-08-20' },
  { id: 7, studentName: '刘思远', className: '计算机 2102', questionTitle: '反转链表', knowledgePoint: '链表', errorType: '指针错乱', errorCount: 3, lastOccurredAt: '2026-08-23' },
  { id: 8, studentName: '张雨桐', className: '计算机 2101', questionTitle: '无重复字符的最长子串', knowledgePoint: '滑动窗口', errorType: '理解偏差', errorCount: 2, lastOccurredAt: '2026-08-24' },
]

export function mockGetWrongQuestions(query: WrongQuery): Promise<PageResult<WrongQuestion>> {
  let list = [...wrongQuestions]
  if (query.keyword) {
    const kw = query.keyword.toLowerCase()
    list = list.filter(
      (w) => w.studentName.toLowerCase().includes(kw) || w.questionTitle.toLowerCase().includes(kw)
    )
  }
  if (query.knowledgePoint) list = list.filter((w) => w.knowledgePoint === query.knowledgePoint)
  const total = list.length
  const start = (query.page - 1) * query.size
  return delay({ list: list.slice(start, start + query.size), total, page: query.page, size: query.size })
}

export function mockExportWrongQuestions(): Promise<Blob> {
  const rows = ['学生,班级,题目,知识点,错误类型,错误次数,最近出错']
  wrongQuestions.forEach((w) =>
    rows.push(`${w.studentName},${w.className},${w.questionTitle},${w.knowledgePoint},${w.errorType},${w.errorCount},${w.lastOccurredAt}`)
  )
  const blob = new Blob(['\uFEFF' + rows.join('\n')], {
    type: 'text/csv;charset=utf-8',
  })
  return delay(blob, 500)
}

// -------------------- 学生与学习报告 --------------------

const students: Student[] = [
  { id: 11, name: '李想', className: '计算机 2101', submitCount: 24, avgAccuracy: 88.5, wrongCount: 7, activeDays: 18, lastActiveAt: '2026-08-26 09:10' },
  { id: 12, name: '张雨桐', className: '计算机 2101', submitCount: 22, avgAccuracy: 92.3, wrongCount: 4, activeDays: 20, lastActiveAt: '2026-08-26 09:05' },
  { id: 13, name: '陈一鸣', className: '计算机 2102', submitCount: 19, avgAccuracy: 76.1, wrongCount: 12, activeDays: 15, lastActiveAt: '2026-08-25 22:40' },
  { id: 14, name: '刘思远', className: '计算机 2102', submitCount: 20, avgAccuracy: 81.7, wrongCount: 9, activeDays: 16, lastActiveAt: '2026-08-25 20:33' },
  { id: 15, name: '王梓涵', className: '软件 2101', submitCount: 16, avgAccuracy: 68.9, wrongCount: 15, activeDays: 12, lastActiveAt: '2026-08-25 18:20' },
  { id: 16, name: '赵天佑', className: '软件 2101', submitCount: 21, avgAccuracy: 79.4, wrongCount: 10, activeDays: 17, lastActiveAt: '2026-08-26 08:48' },
  { id: 17, name: '孙悦', className: '计算机 2103', submitCount: 18, avgAccuracy: 85.2, wrongCount: 6, activeDays: 14, lastActiveAt: '2026-08-25 19:15' },
  { id: 18, name: '周子墨', className: '计算机 2103', submitCount: 15, avgAccuracy: 72.8, wrongCount: 11, activeDays: 11, lastActiveAt: '2026-08-26 08:30' },
]

export function mockGetStudentList(query: StudentQuery): Promise<PageResult<Student>> {
  let list = [...students]
  if (query.keyword) list = list.filter((s) => s.name.includes(query.keyword!))
  if (query.className) list = list.filter((s) => s.className === query.className)
  const total = list.length
  const start = (query.page - 1) * query.size
  return delay({ list: list.slice(start, start + query.size), total, page: query.page, size: query.size })
}

export function mockGetStudentReport(id: number): Promise<StudentReport> {
  const student = students.find((s) => s.id === id) ?? students[0]
  const trend = Array.from({ length: 14 }, (_, i) => {
    const d = new Date(2026, 7, 13 + i)
    const date = `${d.getMonth() + 1}/${d.getDate()}`
    return {
      date,
      submitCount: Math.round(1 + Math.random() * 3),
      accuracy: Math.round(70 + Math.random() * 25),
    }
  })
  return delay({
    studentId: student.id,
    name: student.name,
    className: student.className,
    trend,
    knowledge: [
      { name: '数组', value: 88 },
      { name: '链表', value: 62 },
      { name: '栈', value: 90 },
      { name: '树', value: 58 },
      { name: '动态规划', value: 41 },
      { name: '滑动窗口', value: 66 },
    ],
    wrongDistribution: [
      { knowledgePoint: '链表', wrongCount: 5 },
      { knowledgePoint: '动态规划', wrongCount: 4 },
      { knowledgePoint: '树 / BFS', wrongCount: 3 },
      { knowledgePoint: '滑动窗口', wrongCount: 2 },
    ],
    recentSubmissions: submissions.filter((s) => s.studentId === student.id).slice(0, 5),
  })
}

// -------------------- 数据看板 --------------------

export function mockGetDashboardStats(): Promise<DashboardStats> {
  const dates: string[] = []
  const submit: number[] = []
  const accuracy: number[] = []
  for (let i = 13; i <= 26; i++) {
    dates.push(`8/${i}`)
    submit.push(Math.round(20 + Math.sin(i) * 8 + Math.random() * 10))
    accuracy.push(Math.round(70 + Math.random() * 20))
  }
  return delay({
    studentCount: 86,
    todaySubmit: 37,
    pendingReview: 12,
    avgAccuracy: 81.6,
    activeStudents: 64,
    submitTrend: dates.map((date, i) => ({ date, submitCount: submit[i], accuracy: accuracy[i] })),
    wrongDistribution: [
      { knowledgePoint: '动态规划', wrongCount: 42 },
      { knowledgePoint: '链表', wrongCount: 31 },
      { knowledgePoint: '滑动窗口', wrongCount: 24 },
      { knowledgePoint: '树 / BFS', wrongCount: 18 },
      { knowledgePoint: '数组 / 哈希表', wrongCount: 12 },
    ],
    classActivity: [
      { className: '计算机 2101', submitCount: 128, activeStudents: 28 },
      { className: '计算机 2102', submitCount: 96, activeStudents: 24 },
      { className: '计算机 2103', submitCount: 87, activeStudents: 21 },
      { className: '软件 2101', submitCount: 74, activeStudents: 19 },
    ],
    recentSubmissions: submissions.slice(0, 6),
  })
}