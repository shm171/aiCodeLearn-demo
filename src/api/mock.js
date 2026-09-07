function delay(data, ms = 300) {
  return new Promise((resolve) => setTimeout(() => resolve(structuredClone(data)), ms));
}
const MOCK_TEACHER = {
  id: 2,
  userId: 2,
  username: "\u738B\u8001\u5E08",
  email: "teacher@ailearn.com",
  phone: "13800000002",
  role: "TEACHER",
  bio: "\u8D1F\u8D23\u7B97\u6CD5\u4E0E\u6570\u636E\u7ED3\u6784\u8BFE\u7A0B\u6559\u5B66\uFF0C\u559C\u6B22\u7528 AI \u8F85\u52A9\u6279\u6539\u3002"
};
const DEMO_ACCOUNTS = {
  "teacher@ailearn.com": { password: "123456", profile: MOCK_TEACHER },
  "admin@ailearn.com": {
    password: "123456",
    profile: {
      id: 1,
      userId: 1,
      username: "\u7BA1\u7406\u5458",
      email: "admin@ailearn.com",
      role: "ADMIN"
    }
  }
};
function mockLogin(data) {
  const account = DEMO_ACCOUNTS[data.email];
  if (!account || account.password !== data.password) {
    return delay(
      Promise.reject({ message: "\u90AE\u7BB1\u6216\u5BC6\u7801\u9519\u8BEF" }),
      300
    ).then(
      () => {
        throw new Error("\u90AE\u7BB1\u6216\u5BC6\u7801\u9519\u8BEF");
      },
      (e) => {
        throw e;
      }
    );
  }
  return delay({ token: `mock-jwt-token-${Date.now()}`, tokenType: "Bearer", expiresIn: 86400 });
}
function mockGetProfile() {
  return delay(MOCK_TEACHER);
}
function mockUpdateProfile(data) {
  Object.assign(MOCK_TEACHER, data);
  return delay(MOCK_TEACHER);
}
const questions = [
  { id: 1, title: "\u4E24\u6570\u4E4B\u548C\uFF08\u6570\u7EC4\u54C8\u5E0C\uFF09", type: "PROGRAMMING", difficulty: "EASY", knowledgePoint: "\u6570\u7EC4 / \u54C8\u5E0C\u8868", description: "\u7ED9\u5B9A\u4E00\u4E2A\u6574\u6570\u6570\u7EC4\u548C\u4E00\u4E2A\u76EE\u6807\u503C\uFF0C\u627E\u51FA\u548C\u4E3A\u76EE\u6807\u503C\u7684\u4E24\u4E2A\u6570\u7684\u4E0B\u6807\u3002", status: 1, submitCount: 128, accuracy: 86.4, createdAt: "2026-07-01 10:20" },
  { id: 2, title: "\u53CD\u8F6C\u94FE\u8868", type: "PROGRAMMING", difficulty: "MEDIUM", knowledgePoint: "\u94FE\u8868", description: "\u5B9E\u73B0\u5355\u94FE\u8868\u53CD\u8F6C\uFF0C\u8981\u6C42 O(n) \u65F6\u95F4\u3001O(1) \u7A7A\u95F4\u3002", status: 1, submitCount: 96, accuracy: 72.1, createdAt: "2026-07-03 14:05" },
  { id: 3, title: "\u7B2C 1 \u5468\u7B97\u6CD5\u4F5C\u4E1A\uFF1A\u6392\u5E8F\u7EFC\u5408", type: "ASSIGNMENT", difficulty: "MEDIUM", knowledgePoint: "\u6392\u5E8F", description: "\u5B8C\u6210\u5FEB\u901F\u6392\u5E8F\u4E0E\u5F52\u5E76\u6392\u5E8F\u7684\u5B9E\u73B0\uFF0C\u5E76\u6BD4\u8F83\u4E0D\u540C\u6570\u636E\u89C4\u6A21\u4E0B\u7684\u6027\u80FD\u3002", status: 1, submitCount: 84, accuracy: 68.9, createdAt: "2026-07-06 09:00" },
  { id: 4, title: "\u6709\u6548\u7684\u62EC\u53F7", type: "PROGRAMMING", difficulty: "EASY", knowledgePoint: "\u6808", description: "\u7ED9\u5B9A\u53EA\u5305\u542B\u62EC\u53F7\u5B57\u7B26\u7684\u5B57\u7B26\u4E32\uFF0C\u5224\u65AD\u62EC\u53F7\u662F\u5426\u5339\u914D\u3002", status: 1, submitCount: 152, accuracy: 91.2, createdAt: "2026-07-10 16:30" },
  { id: 5, title: "\u4E8C\u53C9\u6811\u7684\u5C42\u5E8F\u904D\u5386", type: "PROGRAMMING", difficulty: "MEDIUM", knowledgePoint: "\u6811 / BFS", description: "\u6309\u5C42\u8F93\u51FA\u4E8C\u53C9\u6811\u8282\u70B9\uFF0C\u540C\u5C42\u8282\u70B9\u6309\u4ECE\u5DE6\u5230\u53F3\u6392\u5217\u3002", status: 1, submitCount: 61, accuracy: 64.7, createdAt: "2026-07-15 11:40" },
  { id: 6, title: "\u7B2C 2 \u5468\u4F5C\u4E1A\uFF1A\u52A8\u6001\u89C4\u5212\u5165\u95E8", type: "ASSIGNMENT", difficulty: "HARD", knowledgePoint: "\u52A8\u6001\u89C4\u5212", description: "\u5B8C\u6210\u722C\u697C\u68AF\u4E0E\u6700\u5927\u5B50\u6570\u7EC4\u548C\u4E24\u9053\u9898\uFF0C\u5E76\u5199\u51FA\u72B6\u6001\u8F6C\u79FB\u65B9\u7A0B\u8BF4\u660E\u3002", status: 0, submitCount: 42, accuracy: 55.3, createdAt: "2026-07-20 08:55" },
  { id: 7, title: "\u65E0\u91CD\u590D\u5B57\u7B26\u7684\u6700\u957F\u5B50\u4E32", type: "PROGRAMMING", difficulty: "HARD", knowledgePoint: "\u6ED1\u52A8\u7A97\u53E3", description: "\u7ED9\u5B9A\u5B57\u7B26\u4E32\uFF0C\u627E\u51FA\u5176\u4E2D\u4E0D\u542B\u6709\u91CD\u590D\u5B57\u7B26\u7684\u6700\u957F\u5B50\u4E32\u7684\u957F\u5EA6\u3002", status: 1, submitCount: 38, accuracy: 48.9, createdAt: "2026-07-25 15:15" },
  { id: 8, title: "\u7F16\u7A0B\u57FA\u7840\u9009\u62E9\u6D4B\u9A8C\uFF08\u7B2C\u4E00\u7AE0\uFF09", type: "CHOICE", difficulty: "EASY", knowledgePoint: "\u57FA\u7840\u8BED\u6CD5", description: "\u8986\u76D6\u53D8\u91CF\u3001\u6761\u4EF6\u3001\u5FAA\u73AF\u7684\u57FA\u7840\u9009\u62E9\u9898 20 \u9053\u3002", status: 1, submitCount: 175, accuracy: 93.5, createdAt: "2026-08-01 09:30" }
];
function mockGetQuestionList(query) {
  let list = [...questions];
  if (query.keyword) {
    const kw = query.keyword.toLowerCase();
    list = list.filter(
      (q) => q.title.toLowerCase().includes(kw) || q.knowledgePoint.toLowerCase().includes(kw)
    );
  }
  if (query.type) list = list.filter((q) => q.type === query.type);
  if (query.status !== "" && query.status !== void 0) list = list.filter((q) => q.status === query.status);
  const total = list.length;
  const start = (query.page - 1) * query.size;
  return delay({ list: list.slice(start, start + query.size), total, page: query.page, size: query.size });
}
function mockCreateQuestion(data) {
  const item = {
    id: Math.max(0, ...questions.map((q) => q.id)) + 1,
    ...data,
    status: 1,
    submitCount: 0,
    accuracy: 0,
    createdAt: (/* @__PURE__ */ new Date()).toISOString().slice(0, 16).replace("T", " ")
  };
  questions.unshift(item);
  return delay(item);
}
function mockUpdateQuestion(id, data) {
  const idx = questions.findIndex((q) => q.id === id);
  if (idx < 0) throw new Error("\u9898\u76EE\u4E0D\u5B58\u5728");
  questions[idx] = { ...questions[idx], ...data };
  return delay(questions[idx]);
}
function mockUpdateQuestionStatus(id, status) {
  const idx = questions.findIndex((q) => q.id === id);
  if (idx >= 0) questions[idx].status = status;
  return delay(void 0);
}
function mockDeleteQuestion(id) {
  const idx = questions.findIndex((q) => q.id === id);
  if (idx >= 0) questions.splice(idx, 1);
  return delay(void 0);
}
const submissions = [
  { id: 1, studentId: 11, studentName: "\u674E\u60F3", className: "\u8BA1\u7B97\u673A 2101", questionId: 1, questionTitle: "\u4E24\u6570\u4E4B\u548C\uFF08\u6570\u7EC4\u54C8\u5E0C\uFF09", submittedAt: "2026-08-25 09:12", status: "PENDING", staticIssueCount: 3, llmScore: null, durationSeconds: 842 },
  { id: 2, studentId: 12, studentName: "\u5F20\u96E8\u6850", className: "\u8BA1\u7B97\u673A 2101", questionId: 1, questionTitle: "\u4E24\u6570\u4E4B\u548C\uFF08\u6570\u7EC4\u54C8\u5E0C\uFF09", submittedAt: "2026-08-25 09:40", status: "GRADED", staticIssueCount: 0, llmScore: 92, durationSeconds: 610 },
  { id: 3, studentId: 13, studentName: "\u9648\u4E00\u9E23", className: "\u8BA1\u7B97\u673A 2102", questionId: 4, questionTitle: "\u6709\u6548\u7684\u62EC\u53F7", submittedAt: "2026-08-25 10:05", status: "PENDING", staticIssueCount: 1, llmScore: null, durationSeconds: 1205 },
  { id: 4, studentId: 14, studentName: "\u5218\u601D\u8FDC", className: "\u8BA1\u7B97\u673A 2102", questionId: 3, questionTitle: "\u7B2C 1 \u5468\u7B97\u6CD5\u4F5C\u4E1A\uFF1A\u6392\u5E8F\u7EFC\u5408", submittedAt: "2026-08-25 10:33", status: "GRADED", staticIssueCount: 2, llmScore: 78, durationSeconds: 2200 },
  { id: 5, studentId: 15, studentName: "\u738B\u6893\u6DB5", className: "\u8F6F\u4EF6 2101", questionId: 4, questionTitle: "\u6709\u6548\u7684\u62EC\u53F7", submittedAt: "2026-08-25 11:20", status: "REJECTED", staticIssueCount: 5, llmScore: 55, durationSeconds: 1500 },
  { id: 6, studentId: 11, studentName: "\u674E\u60F3", className: "\u8BA1\u7B97\u673A 2101", questionId: 4, questionTitle: "\u6709\u6548\u7684\u62EC\u53F7", submittedAt: "2026-08-25 14:02", status: "GRADED", staticIssueCount: 0, llmScore: 96, durationSeconds: 980 },
  { id: 7, studentId: 16, studentName: "\u8D75\u5929\u4F51", className: "\u8F6F\u4EF6 2101", questionId: 2, questionTitle: "\u53CD\u8F6C\u94FE\u8868", submittedAt: "2026-08-25 15:18", status: "PENDING", staticIssueCount: 4, llmScore: null, durationSeconds: 1890 },
  { id: 8, studentId: 17, studentName: "\u5B59\u60A6", className: "\u8BA1\u7B97\u673A 2103", questionId: 2, questionTitle: "\u53CD\u8F6C\u94FE\u8868", submittedAt: "2026-08-25 16:45", status: "GRADED", staticIssueCount: 1, llmScore: 84, durationSeconds: 1320 },
  { id: 9, studentId: 18, studentName: "\u5468\u5B50\u58A8", className: "\u8BA1\u7B97\u673A 2103", questionId: 7, questionTitle: "\u65E0\u91CD\u590D\u5B57\u7B26\u7684\u6700\u957F\u5B50\u4E32", submittedAt: "2026-08-26 08:30", status: "PENDING", staticIssueCount: 2, llmScore: null, durationSeconds: 1655 },
  { id: 10, studentId: 12, studentName: "\u5F20\u96E8\u6850", className: "\u8BA1\u7B97\u673A 2101", questionId: 3, questionTitle: "\u7B2C 1 \u5468\u7B97\u6CD5\u4F5C\u4E1A\uFF1A\u6392\u5E8F\u7EFC\u5408", submittedAt: "2026-08-26 09:05", status: "GRADED", staticIssueCount: 0, llmScore: 90, durationSeconds: 1480 }
];
function mockGetSubmissionList(query) {
  let list = [...submissions];
  if (query.keyword) {
    const kw = query.keyword.toLowerCase();
    list = list.filter(
      (s) => s.studentName.toLowerCase().includes(kw) || s.questionTitle.toLowerCase().includes(kw)
    );
  }
  if (query.questionId) list = list.filter((s) => s.questionId === query.questionId);
  if (query.status) list = list.filter((s) => s.status === query.status);
  const total = list.length;
  const start = (query.page - 1) * query.size;
  return delay({ list: list.slice(start, start + query.size), total, page: query.page, size: query.size });
}
const STATIC_ISSUES = [
  { id: 1, severity: "ERROR", rule: "no-unused-variable", message: "\u53D8\u91CF tmp \u5DF2\u5B9A\u4E49\u4F46\u4ECE\u672A\u4F7F\u7528", file: "main.py", line: 5, column: 9 },
  { id: 2, severity: "WARNING", rule: "complexity", message: "\u51FD\u6570\u590D\u6742\u5EA6\u4E3A 3\uFF0C\u5EFA\u8BAE\u62C6\u5206\u4EE5\u63D0\u5347\u53EF\u8BFB\u6027", file: "main.py", line: 8, column: 1 },
  { id: 3, severity: "INFO", rule: "naming-convention", message: "\u53D8\u91CF\u540D a/b \u8BED\u4E49\u4E0D\u660E\u786E\uFF0C\u5EFA\u8BAE\u4F7F\u7528\u6709\u610F\u4E49\u7684\u540D\u79F0", file: "main.py", line: 12, column: 13 }
];
function mockGetReviewDetail(id) {
  const sub = submissions.find((s) => s.id === id);
  if (!sub) throw new Error("\u63D0\u4EA4\u8BB0\u5F55\u4E0D\u5B58\u5728");
  const detail = {
    id: sub.id,
    studentName: sub.studentName,
    className: sub.className,
    questionTitle: sub.questionTitle,
    questionDescription: "\u7ED9\u5B9A\u4E00\u4E2A\u6574\u6570\u6570\u7EC4 nums \u548C\u4E00\u4E2A\u6574\u6570\u76EE\u6807\u503C target\uFF0C\u8BF7\u4F60\u5728\u8BE5\u6570\u7EC4\u4E2D\u627E\u51FA\u548C\u4E3A\u76EE\u6807\u503C target \u7684\u90A3\u4E24\u4E2A\u6574\u6570\uFF0C\u5E76\u8FD4\u56DE\u5B83\u4EEC\u7684\u6570\u7EC4\u4E0B\u6807\u3002\u4F60\u53EF\u4EE5\u5047\u8BBE\u6BCF\u79CD\u8F93\u5165\u53EA\u4F1A\u5BF9\u5E94\u4E00\u4E2A\u7B54\u6848\u3002",
    submittedAt: sub.submittedAt,
    language: "python",
    code: sub.id % 2 === 0 ? `class Solution:
    def twoSum(self, nums: List[int], target: int) -> List[int]:
        seen = {}
        for i, num in enumerate(nums):
            if target - num in seen:
                return [seen[target - num], i]
            seen[num] = i
        return []` : `class Solution:
    def twoSum(self, nums: List[int], target: int) -> List[int]:
        # \u66B4\u529B\u89E3\u6CD5
        for i in range(len(nums)):
            for j in range(i + 1, len(nums)):
                if nums[i] + nums[j] == target:
                    return [i, j]
        return []`,
    staticIssues: sub.staticIssueCount > 0 ? STATIC_ISSUES.slice(0, sub.staticIssueCount) : [],
    llmGrading: {
      totalScore: sub.llmScore ?? 0,
      maxScore: 100,
      summary: "\u6574\u4F53\u601D\u8DEF\u6B63\u786E\uFF0C\u80FD\u591F\u901A\u8FC7\u9898\u76EE\u8981\u6C42\u7684\u57FA\u672C\u7528\u4F8B\u3002\u5EFA\u8BAE\u8865\u5145\u8FB9\u754C\u6761\u4EF6\u6D4B\u8BD5\uFF08\u5982\u91CD\u590D\u5143\u7D20\u3001\u7A7A\u6570\u7EC4\uFF09\uFF0C\u5E76\u4F18\u5316\u53D8\u91CF\u547D\u540D\u3002",
      items: [
        { dimension: "\u6B63\u786E\u6027", score: 28, maxScore: 30, comment: "\u4E3B\u8981\u7528\u4F8B\u901A\u8FC7\uFF0C\u8FB9\u754C\u6761\u4EF6\u8986\u76D6\u4E0D\u8DB3\u3002" },
        { dimension: "\u7B97\u6CD5\u6548\u7387", score: 26, maxScore: 30, comment: "\u54C8\u5E0C\u8868\u65B9\u6848 O(n) \u4F18\u4E8E\u66B4\u529B O(n^2)\uFF0C\u5F88\u597D\u3002" },
        { dimension: "\u4EE3\u7801\u89C4\u8303", score: 16, maxScore: 20, comment: "\u7F29\u8FDB\u89C4\u8303\uFF0C\u4F46\u547D\u540D\u53EF\u518D\u63D0\u5347\u3002" },
        { dimension: "\u53EF\u8BFB\u6027", score: 12, maxScore: 20, comment: "\u903B\u8F91\u6E05\u6670\uFF0C\u7F3A\u5C11\u5FC5\u8981\u7684\u6CE8\u91CA\u3002" }
      ]
    },
    manualScore: null,
    manualComment: "",
    status: sub.status
  };
  return delay(detail);
}
async function mockSubmitReview(id, payload) {
  const sub = submissions.find((s) => s.id === id);
  if (sub) {
    sub.status = payload.status;
    sub.llmScore = payload.score;
  }
  const detail = await mockGetReviewDetail(id);
  detail.manualScore = payload.score;
  detail.manualComment = payload.comment;
  detail.status = payload.status;
  return delay(detail);
}
const wrongQuestions = [
  { id: 1, studentName: "\u674E\u60F3", className: "\u8BA1\u7B97\u673A 2101", questionTitle: "\u53CD\u8F6C\u94FE\u8868", knowledgePoint: "\u94FE\u8868", errorType: "\u7A7A\u6307\u9488", errorCount: 4, lastOccurredAt: "2026-08-24" },
  { id: 2, studentName: "\u9648\u4E00\u9E23", className: "\u8BA1\u7B97\u673A 2102", questionTitle: "\u65E0\u91CD\u590D\u5B57\u7B26\u7684\u6700\u957F\u5B50\u4E32", knowledgePoint: "\u6ED1\u52A8\u7A97\u53E3", errorType: "\u903B\u8F91\u9519\u8BEF", errorCount: 3, lastOccurredAt: "2026-08-25" },
  { id: 3, studentName: "\u738B\u6893\u6DB5", className: "\u8F6F\u4EF6 2101", questionTitle: "\u6709\u6548\u7684\u62EC\u53F7", knowledgePoint: "\u6808", errorType: "\u8FB9\u754C\u6761\u4EF6", errorCount: 5, lastOccurredAt: "2026-08-25" },
  { id: 4, studentName: "\u8D75\u5929\u4F51", className: "\u8F6F\u4EF6 2101", questionTitle: "\u4E8C\u53C9\u6811\u7684\u5C42\u5E8F\u904D\u5386", knowledgePoint: "\u6811 / BFS", errorType: "\u8D85\u65F6", errorCount: 2, lastOccurredAt: "2026-08-22" },
  { id: 5, studentName: "\u5468\u5B50\u58A8", className: "\u8BA1\u7B97\u673A 2103", questionTitle: "\u7B2C 2 \u5468\u4F5C\u4E1A\uFF1A\u52A8\u6001\u89C4\u5212\u5165\u95E8", knowledgePoint: "\u52A8\u6001\u89C4\u5212", errorType: "\u72B6\u6001\u8F6C\u79FB\u9519\u8BEF", errorCount: 6, lastOccurredAt: "2026-08-26" },
  { id: 6, studentName: "\u5B59\u60A6", className: "\u8BA1\u7B97\u673A 2103", questionTitle: "\u4E24\u6570\u4E4B\u548C\uFF08\u6570\u7EC4\u54C8\u5E0C\uFF09", knowledgePoint: "\u6570\u7EC4 / \u54C8\u5E0C\u8868", errorType: "\u601D\u8DEF\u504F\u5DEE", errorCount: 1, lastOccurredAt: "2026-08-20" },
  { id: 7, studentName: "\u5218\u601D\u8FDC", className: "\u8BA1\u7B97\u673A 2102", questionTitle: "\u53CD\u8F6C\u94FE\u8868", knowledgePoint: "\u94FE\u8868", errorType: "\u6307\u9488\u9519\u4E71", errorCount: 3, lastOccurredAt: "2026-08-23" },
  { id: 8, studentName: "\u5F20\u96E8\u6850", className: "\u8BA1\u7B97\u673A 2101", questionTitle: "\u65E0\u91CD\u590D\u5B57\u7B26\u7684\u6700\u957F\u5B50\u4E32", knowledgePoint: "\u6ED1\u52A8\u7A97\u53E3", errorType: "\u7406\u89E3\u504F\u5DEE", errorCount: 2, lastOccurredAt: "2026-08-24" }
];
function mockGetWrongQuestions(query) {
  let list = [...wrongQuestions];
  if (query.keyword) {
    const kw = query.keyword.toLowerCase();
    list = list.filter(
      (w) => w.studentName.toLowerCase().includes(kw) || w.questionTitle.toLowerCase().includes(kw)
    );
  }
  if (query.knowledgePoint) list = list.filter((w) => w.knowledgePoint === query.knowledgePoint);
  const total = list.length;
  const start = (query.page - 1) * query.size;
  return delay({ list: list.slice(start, start + query.size), total, page: query.page, size: query.size });
}
function mockExportWrongQuestions() {
  const rows = ["\u5B66\u751F,\u73ED\u7EA7,\u9898\u76EE,\u77E5\u8BC6\u70B9,\u9519\u8BEF\u7C7B\u578B,\u9519\u8BEF\u6B21\u6570,\u6700\u8FD1\u51FA\u9519"];
  wrongQuestions.forEach(
    (w) => rows.push(`${w.studentName},${w.className},${w.questionTitle},${w.knowledgePoint},${w.errorType},${w.errorCount},${w.lastOccurredAt}`)
  );
  const blob = new Blob(["\uFEFF" + rows.join("\n")], {
    type: "text/csv;charset=utf-8"
  });
  return delay(blob, 500);
}
const students = [
  { id: 11, name: "\u674E\u60F3", className: "\u8BA1\u7B97\u673A 2101", submitCount: 24, avgAccuracy: 88.5, wrongCount: 7, activeDays: 18, lastActiveAt: "2026-08-26 09:10" },
  { id: 12, name: "\u5F20\u96E8\u6850", className: "\u8BA1\u7B97\u673A 2101", submitCount: 22, avgAccuracy: 92.3, wrongCount: 4, activeDays: 20, lastActiveAt: "2026-08-26 09:05" },
  { id: 13, name: "\u9648\u4E00\u9E23", className: "\u8BA1\u7B97\u673A 2102", submitCount: 19, avgAccuracy: 76.1, wrongCount: 12, activeDays: 15, lastActiveAt: "2026-08-25 22:40" },
  { id: 14, name: "\u5218\u601D\u8FDC", className: "\u8BA1\u7B97\u673A 2102", submitCount: 20, avgAccuracy: 81.7, wrongCount: 9, activeDays: 16, lastActiveAt: "2026-08-25 20:33" },
  { id: 15, name: "\u738B\u6893\u6DB5", className: "\u8F6F\u4EF6 2101", submitCount: 16, avgAccuracy: 68.9, wrongCount: 15, activeDays: 12, lastActiveAt: "2026-08-25 18:20" },
  { id: 16, name: "\u8D75\u5929\u4F51", className: "\u8F6F\u4EF6 2101", submitCount: 21, avgAccuracy: 79.4, wrongCount: 10, activeDays: 17, lastActiveAt: "2026-08-26 08:48" },
  { id: 17, name: "\u5B59\u60A6", className: "\u8BA1\u7B97\u673A 2103", submitCount: 18, avgAccuracy: 85.2, wrongCount: 6, activeDays: 14, lastActiveAt: "2026-08-25 19:15" },
  { id: 18, name: "\u5468\u5B50\u58A8", className: "\u8BA1\u7B97\u673A 2103", submitCount: 15, avgAccuracy: 72.8, wrongCount: 11, activeDays: 11, lastActiveAt: "2026-08-26 08:30" }
];
function mockGetStudentList(query) {
  let list = [...students];
  if (query.keyword) list = list.filter((s) => s.name.includes(query.keyword));
  if (query.className) list = list.filter((s) => s.className === query.className);
  const total = list.length;
  const start = (query.page - 1) * query.size;
  return delay({ list: list.slice(start, start + query.size), total, page: query.page, size: query.size });
}
function mockGetStudentReport(id) {
  const student = students.find((s) => s.id === id) ?? students[0];
  const trend = Array.from({ length: 14 }, (_, i) => {
    const d = new Date(2026, 7, 13 + i);
    const date = `${d.getMonth() + 1}/${d.getDate()}`;
    return {
      date,
      submitCount: Math.round(1 + Math.random() * 3),
      accuracy: Math.round(70 + Math.random() * 25)
    };
  });
  return delay({
    studentId: student.id,
    name: student.name,
    className: student.className,
    trend,
    knowledge: [
      { name: "\u6570\u7EC4", value: 88 },
      { name: "\u94FE\u8868", value: 62 },
      { name: "\u6808", value: 90 },
      { name: "\u6811", value: 58 },
      { name: "\u52A8\u6001\u89C4\u5212", value: 41 },
      { name: "\u6ED1\u52A8\u7A97\u53E3", value: 66 }
    ],
    wrongDistribution: [
      { knowledgePoint: "\u94FE\u8868", wrongCount: 5 },
      { knowledgePoint: "\u52A8\u6001\u89C4\u5212", wrongCount: 4 },
      { knowledgePoint: "\u6811 / BFS", wrongCount: 3 },
      { knowledgePoint: "\u6ED1\u52A8\u7A97\u53E3", wrongCount: 2 }
    ],
    recentSubmissions: submissions.filter((s) => s.studentId === student.id).slice(0, 5)
  });
}
function mockGetDashboardStats() {
  const perStudentStats = [
    { ownerUserId: 11, errorCount: 15, submissionCount: 24, lastActiveAt: "2026-08-26 09:10", attention: true },
    { ownerUserId: 12, errorCount: 4, submissionCount: 22, lastActiveAt: "2026-08-26 09:05", attention: false },
    { ownerUserId: 13, errorCount: 12, submissionCount: 19, lastActiveAt: "2026-08-25 22:40", attention: true },
    { ownerUserId: 14, errorCount: 9, submissionCount: 20, lastActiveAt: "2026-08-25 20:33", attention: false },
    { ownerUserId: 15, errorCount: 15, submissionCount: 16, lastActiveAt: "2026-08-25 18:20", attention: true },
    { ownerUserId: 16, errorCount: 10, submissionCount: 21, lastActiveAt: "2026-08-26 08:48", attention: false },
    { ownerUserId: 17, errorCount: 6, submissionCount: 18, lastActiveAt: "2026-08-25 19:15", attention: false },
    { ownerUserId: 18, errorCount: 11, submissionCount: 15, lastActiveAt: "2026-08-26 08:30", attention: true }
  ];
  return delay({
    totalSubmissions: 432,
    totalErrors: 127,
    classTopicRanking: [
      { rank: 1, topic: "\u52A8\u6001\u89C4\u5212", errorCount: 42 },
      { rank: 2, topic: "\u94FE\u8868", errorCount: 31 },
      { rank: 3, topic: "\u6ED1\u52A8\u7A97\u53E3", errorCount: 24 },
      { rank: 4, topic: "\u6811 / BFS", errorCount: 18 },
      { rank: 5, topic: "\u6570\u7EC4 / \u54C8\u5E0C\u8868", errorCount: 12 }
    ],
    perStudentStats,
    classCategoryDistribution: [
      { label: "\u547D\u540D\u89C4\u8303", value: 38 },
      { label: "WARNING", value: 29 },
      { label: "\u903B\u8F91\u9519\u8BEF", value: 22 },
      { label: "\u6570\u7EC4\u8D8A\u754C", value: 15 },
      { label: "\u5185\u5B58\u6CC4\u6F0F", value: 23 }
    ],
    attentionStudentIds: [11, 15, 18]
  });
}
function mockGradeSubmission(submissionId, enableLLM = false) {
  return delay({
    submissionId,
    score: enableLLM ? 86 : 74,
    issues: [
      { source: "RULE", severity: "WARNING", category: "\u547D\u540D\u89C4\u8303", lineNumber: 5, message: "\u53D8\u91CF\u540D a/b \u8BED\u4E49\u4E0D\u660E\u786E", suggestion: "\u4F7F\u7528\u6709\u610F\u4E49\u7684\u540D\u79F0\uFF0C\u5982 index / seen" },
      { source: "RULE", severity: "INFO", category: "\u7F29\u8FDB", lineNumber: 12, message: "\u7F29\u8FDB\u4E0D\u4E00\u81F4\uFF0C\u5F71\u54CD\u53EF\u8BFB\u6027", suggestion: "\u7EDF\u4E00\u4E3A 4 \u7A7A\u683C\u7F29\u8FDB" },
      ...enableLLM ? [{ source: "LLM", severity: "ERROR", category: "\u7B97\u6CD5\u601D\u8DEF", lineNumber: 8, message: "\u66B4\u529B\u89E3\u6CD5\u65F6\u95F4\u590D\u6742\u5EA6 O(n^2)\uFF0C\u6570\u636E\u91CF\u5927\u65F6\u4F1A\u8D85\u65F6", suggestion: "\u4F7F\u7528\u54C8\u5E0C\u8868\u8BB0\u5F55\u5DF2\u8BBF\u95EE\u5143\u7D20\uFF0C\u4F18\u5316\u5230 O(n)" }] : []
    ],
    overallFeedback: enableLLM ? "\u6574\u4F53\u601D\u8DEF\u6B63\u786E\uFF0C\u5EFA\u8BAE\u8865\u5145\u8FB9\u754C\u6761\u4EF6\u6D4B\u8BD5\uFF08\u91CD\u590D\u5143\u7D20\u3001\u7A7A\u6570\u7EC4\uFF09\uFF0C\u5E76\u4F18\u5316\u53D8\u91CF\u547D\u540D\u3002" : "\u4EC5\u8FD0\u884C\u89C4\u5219\u6821\u9A8C\uFF0C\u672A\u542F\u7528 LLM \u6DF1\u5EA6\u6279\u6539\u3002"
  });
}
export {
  MOCK_TEACHER,
  delay,
  mockCreateQuestion,
  mockDeleteQuestion,
  mockExportWrongQuestions,
  mockGetDashboardStats,
  mockGetProfile,
  mockGetQuestionList,
  mockGetReviewDetail,
  mockGetStudentList,
  mockGetStudentReport,
  mockGetSubmissionList,
  mockGetWrongQuestions,
  mockGradeSubmission,
  mockLogin,
  mockSubmitReview,
  mockUpdateProfile,
  mockUpdateQuestion,
  mockUpdateQuestionStatus
};
