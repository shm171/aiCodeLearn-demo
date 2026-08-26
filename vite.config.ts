import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// 开发环境代理：/api -> http://localhost:8080（后端 Spring Boot），并去掉 /api 前缀。
// 若后端接口路径本身带 /api 前缀，可去掉 rewrite。
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'echarts': ['echarts'],
          'element-plus': ['element-plus', '@element-plus/icons-vue'],
          'vue-vendor': ['vue', 'vue-router', 'pinia', 'axios'],
        },
      },
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})