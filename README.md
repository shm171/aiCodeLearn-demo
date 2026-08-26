# AI Learn 教师端前端（Vue 3）

面向 **AI Learn 智能学习平台** 的教师端 Web 前端，覆盖《README.md》中「教师数据看板」以及后续 AI 核心模块（题目匹配、静态检查、LLM 批改、错题归档、学生学习报告）对应的教师页面。本仓库对应《AI学习平台前端分工方案》中**前端工程师 B**（教师端 + 数据看板 + 管理端）的职责，页面模块与 A 的学员端相互独立，只复用公共 `api / components / utils`。

## 技术栈

| 类别 | 选型 |
| --- | --- |
| 语言 / 框架 | TypeScript（strict）+ Vue 3（Composition API，`<script setup>`） |
| 构建 | Vite |
| 路由 / 状态 | Vue Router 4 / Pinia |
| HTTP | Axios（JWT 拦截器：`Authorization: Bearer <token>`） |
| UI | Element Plus（el-table / el-form / el-dialog / el-pagination） |
| 图表 | ECharts（封装为通用图表组件，供教师看板与学员报告复用） |
| 样式 | SCSS + CSS 变量 |

## 目录结构

```text
src/
├─ api/                 # 业务接口封装
│  ├─ schema.ts         # 类型定义（后端 openapi-typescript 生成后可替换）
│  ├─ auth.ts           # 登录 / Profile
│  ├─ teacher.ts        # 题目作业、审阅、错题、学生报告
│  ├─ dashboard.ts      # 数据看板统计
│  └─ mock.ts           # 本地 Mock 数据（后端未就绪时演示）
├─ utils/request.ts     # Axios 实例 + JWT 拦截器 + 统一错误处理
├─ stores/user.ts       # Pinia：token 持久化、Profile、角色
├─ router/index.ts      # 路由 + 权限守卫（meta.roles）
├─ layouts/TeacherLayout.vue
├─ components/charts/   # BaseChart / LineChart / BarChart / PieChart / RadarChart
├─ views/
│  ├─ Login.vue
│  ├─ NotFound.vue
│  └─ teacher/          # 教师端页面
│     ├─ Dashboard.vue        # 教师数据看板
│     ├─ QuestionList.vue     # 题目与作业管理
│     ├─ ReviewList.vue       # 学生提交审阅列表
│     ├─ ReviewDetail.vue     # 审阅详情（代码 + 静态检查 + LLM 批改 + 人工复核）
│     ├─ WrongQuestions.vue   # 错题管理（筛选 / 导出）
│     ├─ Students.vue         # 学生列表
│     ├─ StudentReport.vue    # 学生学习报告（趋势 / 雷达 / 错题分布）
│     └─ Profile.vue          # 个人中心 / 档案修改
└─ styles/              # 全局样式
```

## 快速开始

```powershell
pnpm install
pnpm dev
```

浏览器打开 `http://localhost:5173`。

演示账号（Mock 模式）：

```text
邮箱：teacher@ailearn.com
密码：123456
```

> 若未安装 pnpm，可先用 npm：`npm install && npm run dev`。

## Mock 数据与真实接口切换

后端接口未就绪时，前端默认使用本地 Mock 数据（`src/api/mock.ts`），页面可完整演示。

- 开发环境：`.env.development` → `VITE_USE_MOCK=true`
- 生产/联调：`.env.production` → `VITE_USE_MOCK=false`

联调时把 `VITE_USE_MOCK` 改为 `false` 即可走真实请求。接口路径目前为示例（如 `/core/question/list`），**以当前功能分支实际运行的 Swagger 文档为准**，路径不一致时直接改 `src/api/teacher.ts`、`src/api/dashboard.ts`、`src/api/auth.ts` 即可。

## 与后端联调

1. 启动 MySQL 与后端：后端目录执行 `.\mvnw.cmd spring-boot:run`。
2. Swagger：`http://localhost:8080/swagger-ui.html`；OpenAPI JSON：`http://localhost:8080/v3/api-docs`。
3. 前端开发服务器已配置 Vite 代理：`/api` → `http://localhost:8080`（并去掉 `/api` 前缀，见 `vite.config.ts`）。
4. 登录：`POST /login`（邮箱 + 密码）→ 响应中的 JWT 存入 `localStorage`（key: `token`）；后续请求由 `src/utils/request.ts` 自动携带 `Authorization: Bearer <token>`。
5. 角色从 Profile 接口查询（README：角色不写入 JWT），存入 Pinia；路由守卫按 `meta.roles` 控制访问。

常见联调问题（摘自《前端工程师B工作流程详解》）：

- 一直 401：确认已登录且 localStorage 有 token；请求头是否带 `Authorization: Bearer <token>`；Token 是否过期（默认 24 小时）。
- 跨域：开发环境不要直连 8080，统一走 Vite 代理；生产由后端/网关配置 CORS 或同源部署。
- 图表空白：容器必须有高度后再 `echarts.init`；弹窗/抽屉中的图表需先可见再渲染（BaseChart 已处理 resize）。
- 分页字段：以 Swagger 为准（`page/size` 还是 `pageNum/pageSize`），不要硬编码。
- 导出乱码：请求加 `responseType: 'blob'`，用 `a.download` 指定文件名（`WrongQuestions.vue` 已有示例）。

## 构建与提交

```powershell
pnpm build      # vue-tsc 类型检查 + vite 打包，产物在 dist/
git status
git add .
git commit -m "feat: 教师端题目管理页面与数据看板"
git push -u origin feature/web-teacher
```

- 开发统一在功能分支（如 `feature/web-teacher`）进行，**不要直接向 main 提交**。
- 不要提交 `node_modules`、`dist`、`.env.*.local`、本地密码、Token、密钥（`.gitignore` 已配置）。
- 与前端工程师 A 的协作边界：请求层（`utils/request.ts`）与 `schema.d.ts` 类型由 A 主责，A 未就绪时本仓库先提供临时实现；图表组件由 B 主责，做成只传 `option` 的通用组件供学员报告复用。

## 后续可扩展

- 管理端页面：按 `meta.roles: ['ADMIN']` 增加 `/admin` 路由与页面（复用同一套 Layout 与请求层）。
- 接口类型：后端 `/v3/api-docs` 就绪后，用 `openapi-typescript` 生成 `src/api/schema.d.ts` 替换 `schema.ts`。
- 学生/教师/管理员细粒度授权：后端权限规则由项目负责人统一后接入。


## 教师端数据如何与学生端打通（Mock → 真实数据）

当前教师端所有数据来自本地 Mock（`src/api/mock.ts`），与学生端没有关联。真实联调时，**教师端和学生端共用同一个后端数据库**，前端只通过后端接口按 ID 取数，**不硬编码任何学生数据**。

### 1. 数据关联字段（与后端约定）
| 字段 | 含义 |
| --- | --- |
| `studentId` | 学生稳定 ID（不用姓名 / 邮箱关联，遵守 README 第七节规范） |
| `questionId` | 题目 / 作业 ID |
| `submissionId` | 学生提交记录 ID |
| `ownerUserId` | 数据归属账号 ID（core 模块只存这个） |

教师端页面全部通过 id 调用接口（如 `/teacher/submissions/{id}`、`/teacher/students/{id}/report`），学生端产生的提交记录、错题、报告都由后端按这些 ID 聚合后返回，前端无需知道“这个学生是谁的学生”。

### 2. 切换步骤
1. 后端启动后，从 Swagger（`http://localhost:8080/swagger-ui.html`）确认真实接口路径与字段（前端工程师 A、B 一起对）。
2. 把 `.env.development` 中 `VITE_USE_MOCK` 改为 `false`。
3. 修改 `src/api/auth.ts`、`src/api/teacher.ts`、`src/api/dashboard.ts` 中的接口路径（当前为示例路径）。
4. 按 Swagger 实际字段更新 `src/api/schema.ts`（或用 openapi-typescript 生成 `schema.d.ts` 替换）。
5. 页面代码**不需要大改**：它们只调用 api 函数，不直接读写数据。

### 3. 各页面数据来源对照
| 页面 | 当前 Mock（src/api/mock.ts） | 应替换的真实接口（示例） |
| --- | --- | --- |
| 数据看板 | `mockGetDashboardStats` | `GET /teacher/dashboard/stats` |
| 题目与作业 | `mockGetQuestionList / mockCreateQuestion / mockUpdateQuestion / mockUpdateQuestionStatus / mockDeleteQuestion` | `/core/question/list`、`/core/question`、`/core/question/{id}`、`/core/question/{id}/status` |
| 提交审阅列表 | `mockGetSubmissionList` | `GET /teacher/submissions` |
| 审阅详情 | `mockGetReviewDetail` | `GET /teacher/submissions/{id}` |
| 人工复核 | `mockSubmitReview` | `POST /teacher/submissions/{id}/review` |
| 错题管理 | `mockGetWrongQuestions / mockExportWrongQuestions` | `/teacher/wrong-questions`、`/teacher/wrong-questions/export` |
| 学生列表 | `mockGetStudentList` | `GET /teacher/students` |
| 学习报告 | `mockGetStudentReport` | `GET /teacher/students/{id}/report` |
| 登录 | `mockLogin` | `POST /login` |
| 个人档案 | `mockGetProfile / mockUpdateProfile` | `GET/PUT /user/profile` |

### 4. 联调后注意事项
- 学生端（A）产生的数据会通过后端出现在教师端，前提是后端已实现对应聚合接口；在此之前教师端先用 Mock 演示。
- 切到真实接口后若页面空白或报错，按顺序检查：后端是否启动 → Network 请求是否带 `Authorization: Bearer <token>` → 响应字段与 `schema.ts` 是否一致 → 401 是否自动跳登录。
- 接口字段一律以 Swagger 为准，不要在前端猜测字段。
