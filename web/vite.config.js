import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],

  // 开发服务器代理：解决前后端分开跑（5173 vs 8080）的跨域问题
  // 前端请求 /api/xxx 会被转发到后端，转发时去掉 /api 前缀
  // 例：/api/login → http://localhost:8080/login
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
