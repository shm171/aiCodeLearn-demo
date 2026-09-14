# AI Learn 后端整合说明

AI Learn 是面向程序设计课程的学习辅助后端。学生上传 Java 或 C++ 源码后，系统完成文件归档、章节匹配、规则检查、LLM 批改和错题归档，并提供学生复习报告、教师统计看板和流式学习助手。

本轮工作只整理后端，分支为 `feature/backend-integration`。前端代码没有修改，也没有并入该分支。AI 批改与助手的核心实现保持原状。

## 当前结论

| 模块 | 状态 | 说明 |
| --- | --- | --- |
| 账号与档案 | 已完成 | 注册、登录、JWT 校验、账号与 Profile 查询/修改/删除 |
| 源码提交 | 已完成 | 支持 `.java`、`.cpp`、`.cc`、`.cxx`，一次请求完成上传与批改 |
| 双层批改 | 已完成 | 规则静态检查与 LLM 深度批改，返回阶段状态和分数可信度 |
| 错题归档 | 已完成 | 列表、详情、分类过滤、标记已掌握 |
| 学生报告 | 已完成 | 错题分布、薄弱点、复习清单、月度曲线、正确率 |
| 教师看板 | 基本完成 | 当前统计全部用户，`classId` 尚未参与筛选 |
| 学习助手 | 已完成 | SSE 流式返回，支持会话 ID 和 JDBC 会话记忆 |
| 邮箱验证码、找回密码、相似题推荐 | 未实现 | 属于扩展项，不影响本轮基础流程验收 |

默认单元测试和编译未发现阻断运行的代码错误。真实 MySQL、Flyway 和 DeepSeek 链路依赖本机服务与密钥，必须在集成环境中另行执行，不能用默认单元测试结果替代联调结论。

## 本轮修补内容

### Service 与数据库衔接

- 文件、错题、学生报告和教师看板服务改为直接注入持久化端口。缺少数据库适配器时应用会在启动阶段失败，不再运行到业务请求时才报错。
- 对已有 ID 执行保存时，若数据库中不存在对应记录会立即报错，不再静默插入一条新数据。
- 源码提交历史按 `submittedAt DESC, id DESC` 返回；错题历史按 `createdAt DESC, id DESC` 返回。同一时间的数据也有稳定顺序。
- 学生报告的所有按用户查询入口统一校验用户 ID。
- 账号读取事务改为只读事务。

### 权限边界

- `/user/{id}` 及其 Profile、修改、删除接口只允许本人或管理员访问。
- `/core/teacher/**` 只允许 `TEACHER` 或 `ADMIN` 角色访问。
- 学生侧数据继续从 JWT 对应的当前用户取得，不接受前端传入任意用户 ID。

本项目实际使用的设计方式控制在两类：

1. 端口—适配器：Service 依赖 `SourceFileStore`、`ErrorRecordStore`，JPA 类负责具体落库。
2. AOP：账号资源的“本人或管理员”规则集中处理，避免在多个 Controller 方法中重复权限代码。

## 主要结构

```text
src/main/java/com/mylab/ailearn
├─ base
│  ├─ user                 账号、档案、DTO 与 MapStruct 映射
│  ├─ security             登录、JWT 与认证过滤器
│  ├─ repository           账号和档案 JPA Repository
│  └─ global
│     ├─ configs           Security、OpenAPI、当前用户解析
│     └─ security          用户资源授权注解与 AOP
└─ core
   ├─ controller           提交、错题、看板、学习助手接口
   ├─ service              业务流程、统计与 AI 调用
   ├─ service/spi          持久化端口
   ├─ repository           JPA 实体、Repository 与适配器
   └─ model                批改、错题和报表模型
```

Controller 负责 HTTP 边界，Service 管理业务流程和事务，Repository 负责数据库访问。JPA Entity 不直接作为账号接口的响应对象。

## 数据库

数据库使用 MySQL，结构由 Flyway 管理，Hibernate 配置为 `ddl-auto: validate`。

| 迁移 | 内容 |
| --- | --- |
| `V1` | 创建 `app_user` |
| `V2` | 登录字段改为邮箱，创建 `profile` 及账号外键 |
| `V3` | 用户名和角色迁移到 `profile` |
| `V6` | 创建 `source_file` 和用户查询索引 |
| `V7` | 创建 `error_record` 和用户查询索引 |

已有迁移文件不能修改、改名或重新编号。需要补表、字段、外键时应新建迁移并先确认版本号。

当前需要注意：`source_file.owner_user_id`、`error_record.owner_user_id` 和 `error_record.source_file_id` 只有业务关联与索引，尚未建立数据库外键。直接补外键可能被历史孤立数据阻断，因此本轮没有擅自修改表结构；合并前应先检查实际库数据，再决定是否新增迁移。

## 接口一览

公开接口：

| 方法 | 地址 | 用途 |
| --- | --- | --- |
| `POST` | `/user/register` | 注册学生或教师账号 |
| `POST` | `/login` | 邮箱和密码换取 JWT |
| `POST` | `/login/validate` | 校验 Bearer Token |

登录后接口：

| 方法 | 地址 | 用途 |
| --- | --- | --- |
| `GET` | `/user/{id}` | 查询本人账号；管理员可代查 |
| `GET` | `/user/{id}/profile` | 查询本人档案；管理员可代查 |
| `PUT` | `/user/{id}` | 修改本人账号；管理员可代改 |
| `PUT` | `/user/{id}/profile` | 修改本人档案；管理员可代改 |
| `DELETE` | `/user/{id}` | 删除本人账号；管理员可代删 |
| `POST` | `/core/submissions` | Multipart 上传源码并立即返回完整批改结果 |
| `GET` | `/core/review/errors` | 分页查询当前用户错题，可按 `category` 过滤 |
| `GET` | `/core/review/errors/{errorId}` | 查询当前用户的一条错题 |
| `POST` | `/core/review/errors/{errorId}/mastered` | 标记当前用户错题为已掌握 |
| `GET` | `/core/review/package` | 获取学生复习报告 |
| `GET` | `/core/review/weak-topics` | 获取薄弱知识点 |
| `GET` | `/core/teacher/dashboard` | 获取教师统计，仅教师和管理员可用 |
| `POST` | `/core/assistant/chat` | AI 学习助手，返回 `text/event-stream` |

接口字段、校验条件和响应结构以运行中的 Swagger 为准：

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

## 前端联调说明

本轮未改前端。后续联调时需要按以下后端契约处理：

- 登录响应当前只有 `token`。前端若需要访问 `/user/{id}`，必须保留注册响应中的用户 ID；更稳妥的长期方案是另行评审“当前用户信息”契约，本轮未新增接口。
- 源码上传只有一步：向 `POST /core/submissions` 发送字段名为 `file` 的 Multipart 请求。响应同时包含 `sourceFile` 和 `gradingResult`，不需要再调用第二个“开始批改”接口。
- `gradingResult.status` 和 `gradingResult.stages` 决定成绩是否完整可信；模型或检查阶段不完整时，不应只看 `score`。
- `accuracyRate` 是 `0.0` 到 `1.0` 的小数，页面显示百分比时乘以 100。
- 错题“已掌握”必须调用后端接口，不能只改浏览器本地状态。
- 教师看板暂时是全局聚合，传入 `classId` 不会改变结果，页面上不要把它描述为真实班级筛选。
- 学习助手使用 SSE；首次响应头 `X-Conversation-Id` 需要在后续请求的 `conversationId` 中原样传回。

## 本地运行

需要 Java 17、Maven 和 MySQL。PowerShell 示例：

```powershell
$env:MYSQL_ROOT_DB_PASSWORD = "<本机 MySQL 密码>"
$env:DEEPSEEK_APIKEY = "<DeepSeek API Key>"
$env:JWT_SECRET = "<至少 32 字节的随机密钥>"

mvn spring-boot:run
```

默认数据库地址是 `jdbc:mysql://localhost:3306/ai_learn?createDatabaseIfNotExist=true`，用户是 `root`。应用启动时自动执行待处理的 Flyway 迁移。

不要把数据库密码、JWT、Token 或 API Key 写入源码、配置提交、日志截图和 Issue。

## 测试

日常回归不连接真实数据库和模型：

```powershell
mvn clean test
```

本轮结果：`Tests run: 94, Failures: 0, Errors: 0, Skipped: 3`。跳过的是 `ProjectApplicationTests`、`AiAssistantTest` 和 `ChatModelTest`，三者会启动完整上下文并可能产生真实模型调用。

准备好独立 MySQL、数据库密码和 DeepSeek Key 后，再显式运行集成测试：

```powershell
$env:RUN_INTEGRATION_TESTS = "true"
$env:MYSQL_ROOT_DB_PASSWORD = "<本机 MySQL 密码>"
$env:DEEPSEEK_APIKEY = "<DeepSeek API Key>"
mvn test
```

真实集成测试可能写入数据库、调用外部模型并产生费用，不应在没有隔离数据的共享环境中直接运行。

## 已知限制与后续建议

- 先用备份库检查孤立数据，再为 core 表补充账号和源码外键；不要直接改 V1—V7。
- 教师看板需要有正式的班级、课程和师生关系表后，才能实现可信的班级隔离。
- 登录响应与个人档案页面之间仍缺少稳定的“当前用户信息”契约，应与前端一起确定后再改，避免双方各自猜字段。
- 错题列表目前在 Controller 内完成内存分页。数据量增大后应下推到 Repository，但这属于性能改造，不在本轮正确性补丁范围内。
- 邮箱验证、找回密码和相似题推荐不是基础流程的一部分，后续需要独立需求和独立分支。

## 分支记录

后端整合分支从仓库现有 `backed` 分支建立。其余历史分支中涉及 AI 核心改写的提交没有合入，以免改变现有模型提示、检查规则和评分流程。提交保持按问题拆分，便于审查和回退。

提交前至少执行：

```powershell
git branch --show-current
git status
mvn clean test
```
