# AI Learn 后端接口文档（前端联调用）

> 本文档根据后端实际代码整理，前端写接口时以此为准。
> 后端基础地址：`http://localhost:8080`
> 接口文档（Swagger UI）：`http://localhost:8080/swagger-ui.html`
> OpenAPI JSON：`http://localhost:8080/v3/api-docs`

---

## 一、认证说明

### 需要认证的接口
除了「注册」和「登录」之外，其他接口都需要在请求头携带 JWT：

```
Authorization: Bearer <token>
```

### 获取 Token
调用登录接口，成功后响应里会返回 `token` 字段，前端存在 localStorage，之后每次请求自动带上。

### Token 失效
如果接口返回 `401` 状态码，说明 token 过期或无效，前端应清除本地 token 并跳转到登录页。

---

## 二、已实现接口

### 1. 用户注册

- **接口地址**：`POST /user/register`
- **是否需要登录**：否（公开接口）
- **说明**：注册学生或教师账号，不能注册管理员

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | string | 是 | 邮箱，必须是合法邮箱格式 |
| password | string | 是 | 密码，8-16位 |
| username | string | 是 | 用户名，最多50个字符 |
| role | string | 是 | 角色，只能填 `STUDENT`（学生）或 `TEACHER`（教师） |

**请求示例：**
```json
{
  "email": "student@example.com",
  "password": "12345678",
  "username": "小明",
  "role": "STUDENT"
}
```

**成功响应（201）：**
```json
{
  "id": 1,
  "email": "student@example.com"
}
```

**失败响应（400）：** 参数校验失败，返回具体错误信息

---

### 2. 用户登录

- **接口地址**：`POST /login`
- **是否需要登录**：否（公开接口）
- **说明**：用邮箱和密码换取 JWT token

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | string | 是 | 邮箱 |
| password | string | 是 | 密码，8-16位 |

**请求示例：**
```json
{
  "email": "student@example.com",
  "password": "12345678"
}
```

**成功响应（200）：**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**失败响应（401）：** 邮箱或密码错误，无响应体

---

### 3. 校验 Token 是否有效

- **接口地址**：`POST /login/validate`
- **是否需要登录**：是（在请求头传 token）
- **说明**：校验当前 token 是否有效

**请求头：**
```
Authorization: Bearer <token>
```

**成功响应（200）：**
```json
true
```

token 无效时返回 `false`。

---

### 4. 查询账号信息

- **接口地址**：`GET /user/{id}`
- **是否需要登录**：是
- **说明**：根据用户ID查询账号信息

**路径参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 用户ID |

**成功响应（200）：**
```json
{
  "id": 1,
  "email": "student@example.com"
}
```

---

### 5. 查询用户档案

- **接口地址**：`GET /user/{id}/profile`
- **是否需要登录**：是
- **说明**：查询用户的详细档案（用户名、角色、注册时间）

**路径参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 用户ID |

**成功响应（200）：**
```json
{
  "userId": 1,
  "username": "小明",
  "role": "STUDENT",
  "createdAt": "2026-08-22T10:30:00"
}
```

**role 字段说明：**
- `STUDENT`：学生
- `TEACHER`：教师
- `ADMIN`：管理员

---

### 6. 修改账号信息

- **接口地址**：`PUT /user/{id}`
- **是否需要登录**：是
- **说明**：修改邮箱和密码

**路径参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 用户ID |

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | string | 是 | 新邮箱 |
| password | string | 是 | 新密码，8-16位 |

**成功响应（200）：**
```json
{
  "id": 1,
  "email": "newemail@example.com"
}
```

---

### 7. 修改档案用户名

- **接口地址**：`PUT /user/{id}/profile`
- **是否需要登录**：是
- **说明**：只能修改用户名，不能修改角色

**路径参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 用户ID |

**请求体（JSON）：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 新用户名，最多50字符 |

**成功响应（200）：**
```json
{
  "userId": 1,
  "username": "新用户名",
  "role": "STUDENT",
  "createdAt": "2026-08-22T10:30:00"
}
```

---

### 8. 删除用户

- **接口地址**：`DELETE /user/{id}`
- **是否需要登录**：是
- **说明**：删除指定用户（一般管理员用，学员端不需要）

**路径参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 用户ID |

**成功响应（204）：** 无响应体

---

## 三、尚未实现的接口（后续开发）

以下接口后端还没写，前端开发时先用 Mock 数据占位，等后端实现后再替换：

| 功能模块 | 预计接口 | 说明 |
|---------|---------|------|
| 文件上传 | POST /upload 或类似 | 学员上传代码/作业文件 |
| 题目匹配 | GET /questions、GET /questions/{id} | 题目列表、题目详情 |
| 提交作答 | POST /submissions | 提交代码作答 |
| 静态检查结果 | GET /submissions/{id}/check | 静态代码检查结果 |
| LLM批改结果 | GET /submissions/{id}/review | LLM深度批改结果 |
| 错题本 | GET /wrong-questions、DELETE /wrong-questions/{id} | 错题列表、移出错题本 |
| 学习报告 | GET /students/{id}/report | 学生学习报告数据 |
| 教师端 | 题目管理、作业管理、提交审阅等 | 教师端B负责 |
| 管理端 | 用户管理、权限管理等 | 管理端B负责 |

> 以上接口地址为预估，实际以后端 Swagger 文档为准。后端实现后会更新本文档。

---

## 四、前端调用注意事项

1. **基础地址**：所有接口前面加 `http://localhost:8080`
2. **请求格式**：请求体用 JSON（`Content-Type: application/json`）
3. **认证方式**：需要登录的接口在请求头加 `Authorization: Bearer <token>`
4. **错误处理**：
   - `400`：参数错误，看响应里的错误提示
   - `401`：未登录或token过期，清除token跳登录页
   - `404`：接口不存在或资源不存在
   - `500`：服务器内部错误
5. **用户ID从哪来**：登录后可以调用 `/login/validate` 或在本地存用户信息；查询档案时需要用户ID，建议登录后把用户ID存在 localStorage 或 Pinia 里
6. **角色判断**：登录后获取用户档案，根据 `role` 字段判断是学生/教师/管理员，路由守卫据此跳转对应首页

---

## 五、快速测试流程

1. 启动后端：在后端项目目录运行 `.\mvnw.cmd spring-boot:run`
2. 打开 Swagger：浏览器访问 `http://localhost:8080/swagger-ui.html`
3. 注册测试账号：调用 `POST /user/register`，role 填 `STUDENT`
4. 登录：调用 `POST /login`，拿到 token
5. 测试认证接口：在 Swagger 右上角点 `Authorize`，粘贴 token，然后调用需要认证的接口

---

> 本文档随后端开发进度更新。后端新增接口后，以 Swagger 实时文档为准。
