# EduCode 开发日志

> 本文档记录 EduCode 智能学习平台（学员端前端）每一步开发做了什么，方便随时回顾。

---

## 2026-08-23 第二步：清理 Vite 模板遗留文件

清理了脚手架自带的默认内容，换成项目自己的：

- `README.md`：原来是 Vite 模板的英文说明，已替换为项目介绍（启动方法 + 目录结构）
- `src/components/`：HelloWorld 组件删除后留下的空目录，已删除
- `dist/`：验证构建时生成的产物（已被 .gitignore 忽略），已删除
- HelloWorld 组件、Vite 默认图片（vue.svg、vite.svg 等）和模板默认样式：此步之前已删除/覆盖，无需再处理
- 保留：`.vscode/extensions.json`（推荐 Volar 插件，对 Vue 开发有用）

---

## 2026-08-23 第一步：搭建前端项目基础框架

### 一、这一步做了什么

1. 梳理了项目现状：`package.json` 中**已安装好全部所需依赖**（Vue 3、Vue Router、Pinia、Axios、Element Plus），因此本次**没有新安装任何依赖**。
2. 创建了完整的目录结构和基础代码框架：
   - 配置好 **Vue Router** 路由（含登录页、首页、404 页，以及带导航栏的主布局）
   - 配置好 **Pinia** 状态管理（用户登录信息 store，演示了登录 → 保存 → 退出的完整流程）
   - 配置好 **Axios** 请求封装（统一请求地址、自动带 token、统一错误提示）
   - 配置好 **Element Plus** 注册（组件库 + 全部图标全局注册）
3. 所有代码均为 **JavaScript**（未使用 TypeScript），并写了**大量中文注释**，注释面向零基础小白。
4. 验证：`npm run build` 构建通过，项目可正常编译。

### 二、项目使用的依赖（均在 package.json 中，无需再安装）

| 依赖 | 版本 | 作用 |
| ---- | ---- | ---- |
| vue | ^3.5.40 | Vue 3 框架本身 |
| vue-router | ^5.2.0 | 路由：管理"网址 ↔ 页面"的对应关系 |
| pinia | ^4.0.3 | 状态管理：保存全局共享的数据（如登录状态） |
| axios | ^1.19.0 | 网络请求：向后端发请求、拿数据 |
| element-plus | ^2.14.5 | UI 组件库：按钮、表单、卡片等现成组件 |
| @element-plus/icons-vue | ^2.3.2 | Element Plus 的图标库 |
| vite | ^8.2.0 | 构建工具：启动开发服务器、打包项目 |
| @vitejs/plugin-vue | ^6.0.8 | Vite 的 Vue 插件（让 Vite 认识 .vue 文件） |

### 三、目录结构和每个关键文件的作用

```
web/                          ← 前端项目根目录（本文件所在位置）
├── AI-开发日志.md              ← 本文件：开发记录
├── index.html                 ← 网页的"外壳"，浏览器最先加载它
├── package.json               ← 依赖清单和启动命令（npm 脚本）
├── vite.config.js             ← Vite 构建工具的配置
├── public/                    ← 静态资源：原样复制到网站的根目录
│   └── favicon.png            ← 浏览器标签页上的小图标
└── src/                       ← 源代码目录（我们写的代码都在这）
    ├── main.js                ← 入口文件：创建应用 + 安装 Pinia/Router/Element Plus
    ├── App.vue                ← 根组件：整个应用的"外壳"，内部只有 <router-view>
    ├── style.css              ← 全局样式：对整站生效的基础样式
    ├── assets/                ← 需要"打包处理"的静态资源（如图片）
    │   └── logo.png           ← 网站 logo
    ├── router/
    │   └── index.js           ← 路由表：定义网址和页面的对应关系 + 页面标题守卫
    ├── stores/
    │   └── user.js            ← Pinia store：保存登录 token、用户名，提供登录/退出方法
    ├── utils/
    │   └── request.js         ← Axios 封装：统一请求地址、自动带 token、统一错误提示
    ├── layout/
    │   └── MainLayout.vue     ← 主布局：顶部导航栏 + 主体区域（首页等都显示在里面）
    └── views/                 ← 页面文件夹（以后所有页面都放在这里）
        ├── HomeView.vue       ← 首页：欢迎信息和功能占位卡片
        ├── LoginView.vue      ← 登录页：表单 + 校验 + 模拟登录（后端好了换成真实请求）
        └── NotFoundView.vue   ← 404 页：访问不存在的网址时显示
```

### 四、路由规划（当前）

| 网址 | 页面 | 说明 |
| ---- | ---- | ---- |
| `/login` | LoginView | 登录页（独立页面，不带导航栏） |
| `/` | MainLayout → HomeView | 首页（套在带导航栏的主布局里） |
| 其他任何网址 | NotFoundView | 404 兜底页 |

### 五、几个重要的"约定"（以后开发要遵守）

1. **新页面**放在 `src/views/` 里，然后在 `src/router/index.js` 的路由表里加一条记录。
2. **全局共享的数据**（多个页面都要用）放进 `src/stores/` 的 store；只在一个页面用的数据写在该页面自己的 `<script setup>` 里。
3. **发网络请求**统一用 `src/utils/request.js` 导出的 `request`，不要直接用 axios。
4. 页面专属样式写在 `<style scoped>` 里；全站通用的样式写在 `src/style.css` 里。

### 六、如何启动项目

```bash
# 1. 进入前端项目目录（如果还没在 web 目录里）
cd web

# 2. 启动开发服务器（会自动打开/提示网址，默认 http://localhost:5173）
npm run dev
```

其他常用命令：

```bash
npm run build     # 打包项目，产物在 dist/ 文件夹（发布上线时用）
npm run preview   # 预览打包后的产物（先 build 再 preview）
```

### 七、下一步计划（待办）

- [ ] 等后端登录接口做好后，把 `LoginView.vue` 里的模拟登录换成真实请求（代码里已留好注释）
- [ ] 开发"我的课程"等业务页面
- [ ] 在 `router/index.js` 的全局守卫里加"未登录自动跳登录页"的检查
- [ ] 如需连后端，在 `vite.config.js` 里配置代理（把 /api 请求转发到后端端口）
