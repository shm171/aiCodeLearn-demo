# EduCode 智能学习平台 - 学员端前端

Vue 3 + Vite + Vue Router + Pinia + Axios + Element Plus（JavaScript）

## 启动项目

```bash
npm install    # 第一次使用或依赖有变化时执行
npm run dev    # 启动开发服务器，浏览器打开终端提示的地址（默认 http://localhost:5173）
```

其他命令：

```bash
npm run build    # 打包（产物在 dist/ 文件夹）
npm run preview  # 预览打包后的产物
```

## 目录结构

```
src/
├── main.js          # 入口文件：注册 Pinia / Router / Element Plus
├── App.vue          # 根组件
├── style.css        # 全局样式
├── router/          # 路由配置（网址 ↔ 页面对应关系）
├── stores/          # Pinia 状态管理（全局共享数据）
├── utils/           # 工具函数（Axios 请求封装等）
├── layout/          # 布局组件（顶部导航栏等公共部分）
└── views/           # 页面组件
```

详细的开发记录见 [AI-开发日志.md](AI-开发日志.md)。
