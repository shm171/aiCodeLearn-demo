# AI Learn 管理端前端（Vue 3）

面向 **AI Learn 智能学习平台** 的管理端 Web 前端，对应《AI学习平台前端分工方案》中**前端工程师 B** 负责的**管理端**职责：用户管理、角色与权限管理、基础配置。

技术栈与教师端一致：Vue 3 + TypeScript + Vite + Vue Router 4 + Pinia + Axios + Element Plus。

## 目录结构

```text
src/
├─ api/            # auth.ts / admin.ts / mock.ts / schema.ts
├─ utils/request.ts# Axios + JWT 拦截器
├─ stores/user.ts  # Pinia 用户状态
├─ router/         # 路由 + ADMIN 角色守卫
├─ layouts/AdminLayout.vue
└─ views/
   ├─ Login.vue
   ├─ NotFound.vue
   └─ admin/
      ├─ AdminDashboard.vue  # 数据概览
      ├─ UserManage.vue      # 用户管理（增删改/分配角色）
      ├─ RoleManage.vue      # 角色与权限
      ├─ ConfigManage.vue    # 基础配置
      └─ Profile.vue         # 个人中心
```

## 快速开始

```powershell
pnpm install
pnpm dev        # http://localhost:5175
```

演示账号：`admin@ailearn.com` / `123456`（Mock 模式，默认开启）。

> 若未安装 pnpm：`npm install && npm run dev`。

## Mock 与真实接口
- `.env.development`：`VITE_USE_MOCK=true`（业务数据演示用）、`VITE_USE_MOCK_AUTH=false`（认证走真实后端）。
- 后端管理端接口（`/admin/users`、`/admin/roles`、`/admin/config` 等）尚未实现，联调时以后端 Swagger 为准修改 `src/api/admin.ts` 路径与 `src/api/schema.ts` 类型。
- 登录：后端目前没有 `/user/me`，`auth.ts` 用 `POST /login/validate` 校验 token 后返回演示管理员身份（临时方案，等后端提供用户信息接口后替换）。

## 与教师端/学生端的关系
三个端共用同一个后端数据库与账号体系，通过角色（ADMIN/TEACHER/STUDENT）和稳定 ID 关联。管理端负责创建账号、分配角色、配置平台；教师端/学生端使用对应角色登录使用业务功能。