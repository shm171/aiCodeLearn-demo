# AI Learn

## 前端联调入口：Swagger API 实时文档

本项目使用 springdoc-openapi 自动读取正在运行的 Controller、DTO 和校验注解，并生成 Swagger 文档。它不是手工维护的静态接口清单：后端修改接口并重新启动项目后，前端刷新页面就能看到当前运行版本的最新 API。

前端开发人员先启动 MySQL 和后端项目：

```powershell
.\mvnw.cmd spring-boot:run
```

日志出现 `Started ProjectApplication` 后访问：

| 地址 | 用途 |
| --- | --- |
| `http://localhost:8080/swagger-ui.html` | Swagger 可视化接口页面，会进入实际 UI 页面 |
| `http://localhost:8080/swagger-ui/index.html` | Swagger UI 的直接地址 |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON，可供前端工具读取或生成类型 |

Swagger UI 中可以查看每个接口的请求方式、地址、请求 DTO、字段约束、响应 DTO 和是否需要 JWT，也可以点击 `Try it out` 直接发送测试请求。

需要认证的接口按下面的步骤调试：

1. 先调用公开的 `POST /user/register` 准备测试账号，或者直接使用已有测试账号。
2. 调用公开的 `POST /login`，从响应中复制 JWT。
3. 点击 Swagger UI 右上角的 `Authorize`。
4. 在 `bearerAuth` 中只粘贴 Token 本身，不需要手写 `Bearer `，Swagger 会按照 Bearer 认证格式添加请求头。
5. 再调用带锁标记的接口，Swagger 会自动携带 `Authorization: Bearer <token>`。

Swagger 页面和 `/v3/api-docs` 已在 Security 中公开，查看文档不需要登录；业务接口是否公开仍以现有 Security 规则为准。不要把真实账号密码、生产 Token 或密钥放进 Swagger 示例、截图、Git、Issue 或聊天记录。

后端成员通过 Swagger 向前端传递接口信息时，应遵守：

- Controller 的路径、HTTP 方法和真实 DTO 是接口文档的来源，不另外维护一份容易过期的字段清单。
- 使用 `@Tag` 对同一业务的接口分组，使用 `@Operation` 简要说明接口用途。
- 受保护接口使用现有 `bearerAuth` 安全声明，不要重复实现新的 Token 方案。
- DTO 字段使用准确的类型和校验注解；字段含义容易误解时，再补充简短的 OpenAPI 说明。
- 修改接口后先完成编译和测试，重新启动后端，并亲自在 Swagger UI 检查请求与响应结构。
- 通知前端时说明功能分支、接口变化、是否需要认证、主要请求/响应字段和可能的错误状态。
- 不要为了让 Swagger 好看而改变实际业务规则，也不要直接把 Entity 暴露为接口响应。

前端联调时应以当前功能分支实际运行出的 Swagger 文档为准。如果页面打不开，依次检查后端是否成功启动、端口是否为 8080、控制台是否存在数据库或 Flyway 错误，以及访问地址是否正确。

## 一、项目简介

目前已经完成用户账号与 Profile、用户注册登录与档案修改、Spring Data JPA 数据访问、MapStruct 对象映射、Spring Security、JWT Token 认证、Swagger/OpenAPI 接口文档和 Flyway 数据库迁移。

AI 文件上传、题目匹配、静态检查、LLM 批改、错题归档和教师看板属于后续开发内容。本文档重点说明后续成员应遵守的开发与协作规则。

## 二、当前包结构

```text
com.mylab.ailearn
├─ ProjectApplication
└─ base
   ├─ entity
   ├─ repository
   ├─ global
   │  ├─ configs
   │  └─ enums
   ├─ security
   │  ├─ controller
   │  ├─ dto
   │  ├─ filter
   │  └─ service
   └─ user
      ├─ controller
      ├─ service
      ├─ mapper
      └─ dtos
         ├─ sendto
         └─ sendback
```

- `ProjectApplication`：项目启动入口，真实类路径为 `com.mylab.ailearn.ProjectApplication`。它位于根包，因此能够扫描现有的 `com.mylab.ailearn.base` 和未来的 `com.mylab.ailearn.core`。
- `entity`：存放 JPA 实体，每个实体负责与数据库表进行映射。
- `repository`：继承 Spring Data JPA 接口，负责数据库查询和持久化。
- `controller`：接收 HTTP 请求、校验参数并返回响应，不负责复杂业务流程。
- `service`：处理业务规则、流程编排和事务。
- `mapper`：使用 MapStruct 转换 Entity 与 DTO，减少重复的字段复制代码。
- `dto` / `dtos`：数据传输对象，用于隔离接口数据和数据库实体，避免向前端暴露密码哈希、数据库关联等内部字段。
- `sendto`：客户端传入的请求数据。
- `sendback`：服务端返回给客户端的响应数据。
- `security`：负责登录认证、JWT 生成与校验以及请求过滤。
- `global`：存放公共配置与枚举；当前分别位于 `configs` 和 `enums`。

## 三、后续 AI 核心开发计划

后续 AI 核心代码统一放在：

```text
src/main/java/com/mylab/ailearn/core
```

对应 Java 包名：

```java
com.mylab.ailearn.core
```

`core` 模块参考现有 `user` 模块按职责分层：

```text
core
├─ controller
├─ service
├─ mapper
├─ dto
├─ entity       按实际数据库需求创建
└─ repository   按实际数据库需求创建
```

各层必须遵守以下规则：

- Controller 只处理接口地址、请求参数、参数校验和响应，不直接访问 Repository。
- Service 负责业务逻辑、流程编排和事务边界。需要多个数据库操作共同成功或回滚时，在 Service 方法上管理事务。
- Repository 只负责数据库访问，不编写跨步骤业务流程。
- Entity 只负责数据库映射，不作为 HTTP 接口响应直接返回。
- Mapper 使用 MapStruct 完成 Entity 与 DTO 的字段转换，不在 Mapper 中判断权限、调用数据库或编写业务逻辑。
- DTO 只用于接口或模块间的数据传输，不能直接把 Entity 返回给前端。

后续计划包括：

```text
文件上传
题目匹配
静态代码检查
LLM 深度批改
错误记录
错题归档
学生学习报告
教师数据看板
```

不同成员应优先独立完成自己负责的模块。模块之间的连接、用户身份传递、Security 权限和整体流程编排，最后由我统一检查和整合。

## 四、Git 分支与提交规则

> **警告：千万、千万、千万不要直接向 `main` 提交代码。**
>
> 每个人开发前必须创建自己的功能分支，完成后通过 Pull Request 交给我检查和整合。

开始开发前，在项目根目录执行：

```powershell
git switch main
git pull --ff-only
git switch -c feature/模块名称
git branch --show-current
```

开发完成后，先检查改动，再提交自己的功能分支：

```powershell
git status
git add .
git commit -m "feat: 功能说明"
git push -u origin feature/模块名称
```

然后在 GitHub 创建 Pull Request：

```text
base: main
compare: feature/模块名称
```

推荐分支名：

```text
feature/core-upload
feature/static-check
feature/llm-grading
feature/error-record
docs/readme
fix/login-error
```

每次提交前必须执行：

```powershell
git branch --show-current
git status
```

如果当前分支显示为 `main`，立即停止提交，先切换或创建功能分支。

团队成员还必须遵守：

- 一个功能对应一个独立分支，不在同一分支混合多个无关任务。
- 不要自行把功能分支合并到 `main`，由项目负责人通过 Pull Request 检查和整合。
- 不要对 `main` 强制推送。
- 不要提交 `.idea`、`target`、本地密码、JWT、Token、API Key 或其他密钥。
- `git add .` 后仍要再次检查 `git status`，避免把本地文件或无关改动加入提交。
- 不要在一个提交中混入大量无关格式化、重命名或其他成员的代码修改。
- 发现工作区已有别人的未提交修改时，不要覆盖、回滚或删除，应先联系对应成员或项目负责人。

即使当前 GitHub 私有仓库无法免费强制执行分支保护规则，所有成员也必须主动遵守以上流程。

## 五、数据库开发规范

当前项目的数据库技术约定如下：

- 数据库使用 MySQL。
- 业务数据库访问使用 Spring Data JPA。
- 当前 Java 业务代码没有使用 `JdbcTemplate`，也没有手写 JDBC。
- JDBC 相关依赖继续保留，用于 JPA/Hibernate 底层数据库连接以及未来可能的兼容性开发。
- DTO 与 Entity 的转换使用 MapStruct。
- 数据库表结构版本由 Flyway 管理。
- Hibernate 的 `ddl-auto: validate` 只校验实体与数据库表是否一致，不创建、修改或删除表。

> **数据库变更警告：**如果负责的模块需要新增、减少或修改数据库字段、表、外键、索引、实体或实体关系，不要直接自行决定。请先通过微信联系项目负责人，由项目负责人协调表结构、Flyway 迁移版本以及对其他模块的影响。

任何成员都不能：

- 修改、删除、改名或重新编号已经存在的迁移文件。
- 手工修改共享数据库后不提供对应迁移。
- 未确认就随意占用 Flyway 迁移版本号。
- 为了让项目启动而关闭 Flyway。
- 把 `ddl-auto` 改成 `update` 来代替迁移文件。
- 删除其他成员负责的表、字段、外键或索引。
- 在没有评估查询方式和数据量时随意添加大量索引。
- 通过删除 `flyway_schema_history` 记录来掩盖迁移问题。

## 六、Flyway 本地数据库迁移教程

### 1. Flyway 是什么

Flyway 用 SQL 文件记录数据库结构的每一次变化。每名成员拉取相同代码后，Flyway 会按照相同版本顺序升级本地数据库，避免出现“代码相同但表结构不同”的问题。

迁移目录：

```text
src/main/resources/db/migration
```

文件命名规则：

```text
V版本号__说明.sql
```

版本号和说明之间必须是两个下划线。例如：

```text
V1__create_app_user.sql
V2__use_email_and_create_profile.sql
V3__move_role_to_profile.sql
```

当前仓库最新迁移版本是 V3，不存在 V4 文件。下一次迁移通常从 V4 开始，但编写前必须先通过微信联系项目负责人确认版本号，防止多人同时创建相同版本。

### 2. 当前迁移内容

- V1：创建最初的 `app_user` 用户账号表，当时包含用户名、密码哈希和角色。
- V2：把账号登录字段从用户名调整为邮箱，并创建与账号一对一关联的 `profile` 表。
- V3：为 Profile 增加用户名和角色，迁移已有数据，为缺少档案的历史账号补建 Profile，最后从 AppUser 删除角色字段。

V1、V2、V3 都已经属于历史迁移。它们不能随意删除、修改、改名或重新编号，否则其他成员已经执行过的迁移会发生校验和不一致。

### 3. Flyway 如何工作

应用启动时，Flyway 会：

1. 根据配置连接 MySQL。
2. 检查数据库中的 `flyway_schema_history`。
3. 对比项目迁移目录中的 SQL 文件。
4. 按版本顺序执行尚未运行的迁移。
5. 保存迁移版本、说明、执行时间、校验和以及执行结果。
6. 跳过已经成功执行且校验和一致的迁移。

常见状态：

- `Success`：迁移已经在当前数据库成功执行，不会再次执行。
- `Pending`：本地存在该迁移文件，但当前数据库还没有执行。
- `checksum mismatch`：已经执行过的迁移文件后来被修改，数据库记录的校验和与本地文件不一致。

遇到 checksum mismatch 时，不要随便执行 `repair`、删除历史记录或重建共享数据库。先停止操作并联系项目负责人，确认文件为什么变化以及应采用哪种修复方式。

### 4. 本地迁移步骤

1. 启动本地 MySQL 服务。
2. 确认 MySQL 地址、端口、用户名和密码正确，并确认账号具有建库和修改表结构的权限。
3. 在当前 PowerShell 终端临时设置本地数据库密码：

```powershell
$env:DB_PASSWORD = "<你的本地 MySQL 密码>"
```

`application.yml` 和 POM 中的 Flyway Maven 插件都会读取 `DB_PASSWORD`。环境变量只对当前终端会话有效，不要把真实密码写进项目文件、README 或提交记录。

4. 检查 `src/main/resources/application.yml` 中的数据源和 Flyway 配置，不要改变团队约定的数据库地址和数据库名。
5. 如果使用 Maven Flyway 插件，还要检查 `pom.xml` 中插件使用的数据库参数。应用启动配置和 Maven 插件配置是两套入口，需要指向同一个目标数据库。
6. 在项目根目录查看迁移状态：

```powershell
.\mvnw.cmd flyway:info
```

7. 确认哪些迁移是 `Success`，哪些是 `Pending`。如果出现 `Failed`、checksum mismatch 或版本冲突，先联系负责人，不要强行修复。
8. 确认无误后执行迁移：

```powershell
.\mvnw.cmd flyway:migrate
```

也可以直接启动项目：

```powershell
.\mvnw.cmd spring-boot:run
```

应用启动时会自动执行尚未运行的迁移。

9. 在 MySQL 客户端或 IDEA Database 工具中检查：

```text
flyway_schema_history
app_user
profile
```

10. 最后运行完整测试：

```powershell
.\mvnw.cmd clean test
```

如果数据库已经处于 V3，Flyway 会提示数据库已经是最新版本，不会重复修改表或数据。

### 5. IDEA Database 工具

成员可以使用 IDEA 自带的 Database 工具：

- 连接本地 MySQL。
- 查看表、字段、主键、外键和索引。
- 预览、编写或辅助生成 SQL。
- 对照实体检查新的 Flyway 迁移文件。

IDEA 自动生成的 SQL 不能未经检查就直接执行。正确流程是：

```text
提出数据库需求
→ 微信联系负责人
→ 确认表结构和迁移版本
→ 使用 IDEA 辅助生成 SQL
→ 检查 SQL
→ 写入新的 Flyway 文件
→ 本地测试
→ 通过功能分支提交
```

## 七、模块解耦规范

当前启动类位于 `com.mylab.ailearn`，Spring Boot 默认组件扫描已经能够同时覆盖：

```text
com.mylab.ailearn.base
com.mylab.ailearn.core
```

未来模块应尽量保持高度解耦。`core` 模块不得直接依赖以下 User 模块内部实现：

```text
AppUser
Profile
UserRepository
ProfileRepository
UserService
```

AI 数据需要记录用户归属时，只保存稳定的账号 ID。例如在 core 自己的实体中保存：

```java
private Long ownerUserId;
```

对应数据库字段可使用：

```text
owner_user_id
```

不要使用以下方式表示账号归属：

- 使用用户名作为关联依据。
- 使用邮箱作为关联依据。
- 使用 Profile ID 代替账号 ID。
- 在一个字符串中保存逗号分隔的多个用户 ID。
- 在普通字段中保存 JSON 用户 ID 列表。

单用户归属使用一个 `owner_user_id`。需要表达多用户关系时，后续通过独立关联表建模，并先与负责人确认数据库设计。

模块之间优先通过以下内容协作：

- `Long` 类型的稳定 ID。
- DTO。
- 精简且职责清晰的接口。
- 明确的输入与输出对象。

不要跨模块直接调用其他成员模块内部的 Repository，也不要直接修改其他成员负责的 Entity。确实需要跨模块能力时，先确定公开接口或 DTO 边界，再由负责人协调连接方式。

每名成员应先保证自己的模块能够独立编译和测试。模块之间最终的连接、Security 权限、当前用户身份传递和整体业务流程由项目负责人统一收官整合。

## 八、Spring Security 与 Token 认证

当前项目使用：

```text
Token-based authentication
```

它不是基于 Cookie/Session 保存登录状态的认证方式。完整流程如下：

1. 客户端向 `POST /login` 提交邮箱和密码。
2. Spring Security 通过 `AuthenticationManager` 调用现有 `UserDetailsService`。
3. `UserService` 使用 `UserRepository` 按邮箱查询 AppUser，取得邮箱和 BCrypt 密码哈希。
4. `UserService` 再使用 `ProfileRepository` 查询 Profile 中的当前角色。
5. Spring Security 使用 BCrypt 校验用户提交的密码。
6. 登录成功后，`JwtService` 生成 JWT。
7. JWT 返回给客户端。
8. 客户端保存 Token，并在后续请求头中主动携带：

```http
Authorization: Bearer <token>
```

9. `JwtAuthenticationFilter` 从 `Authorization` 请求头提取 Token。
10. `JwtService` 校验 Token 签名、邮箱 subject 和过期时间。
11. 过滤器通过 `UserDetailsService` 重新查询当前账号与 Profile 角色。
12. 校验成功后，过滤器把认证对象写入当前请求的 `SecurityContext`。
13. `SecurityConfig` 根据公开接口和认证规则决定请求能否访问。

当前实现需要注意：

- `SecurityConfig` 使用 `SessionCreationPolicy.STATELESS`，服务端不使用 Session 保存登录状态。
- 当前认证不依靠 Cookie；Token 由客户端保存并放入 `Authorization` 请求头。
- JWT 使用邮箱作为 subject。
- JWT 当前默认有效期为 `86_400_000` 毫秒，即 24 小时，以 `JwtService` 中的常量为准。
- 角色没有直接写入 JWT，而是在认证时从 Profile 查询，因此角色以数据库当前值为准。
- `POST /user/register`、`POST /login` 和 `POST /login/validate` 当前公开；Swagger UI、OpenAPI JSON 和 `/error` 也已放行。
- 其他接口默认要求通过认证。
- 学生、教师和管理员的细粒度接口授权仍属于后续开发内容。
- core 模块不要重复实现登录、Token 生成或 Token 校验，应复用现有 Security 基础设施。
- 正式环境必须通过 `JWT_SECRET` 环境变量提供足够安全的密钥，不能依赖本地默认值。
- 不能在 Git、日志、README 示例、Issue、Pull Request 或聊天中放入真实密码、Token 和密钥。

Token 只能证明请求携带了通过校验的认证信息。后续新增敏感接口时，仍需由负责人统一设计角色授权、资源归属检查和越权访问防护。

## 九、开发完成前的检查

每名成员完成模块后至少执行：

```powershell
git branch --show-current
git status
.\mvnw.cmd clean test
```

提交 Pull Request 前确认：

- 当前分支不是 `main`。
- 项目能够编译，测试全部通过。
- 没有误改 V1、V2、V3 等历史迁移。
- 数据库变化已经通过微信与仓库拥有者确认，并使用已协调的迁移版本。
- 没有提交密码、JWT、Token、API Key 或密钥。
- Controller 没有直接访问 Repository。
- 接口没有直接返回 Entity。
- Mapper 中没有业务逻辑。
- 没有跨模块调用其他模块的内部 Repository。
- 没有把用户名、邮箱或字符串列表当作稳定用户关联。
- README、接口说明和实际代码保持一致。

各成员负责独立模块开发；我在最后负责数据库协调、代码审查、分支合并以及最终模块整合。
