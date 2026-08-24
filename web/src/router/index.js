// 路由配置：定义网址和页面对应关系
import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

const routes = [
  // 登录页（一级路由，不套导航栏）
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录' },
  },

  // 主布局（带导航栏），子页面显示在布局内的 <router-view>
  {
    path: '/',
    component: MainLayout,
    children: [
      // 首页：path为空表示访问 / 时默认显示
      {
        path: '',
        name: 'home',
        component: () => import('../views/HomeView.vue'),
        meta: { title: '首页' },
      },
      // 以后加新页面在 children 里照着加
    ],
  },

  // 404兜底：匹配所有没被上面匹配到的网址，必须放最后
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('../views/NotFoundView.vue'),
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 全局前置守卫：每次页面前修改浏览器标签标题
// 以后可在这里加登录校验（未登录跳登录页）
router.beforeEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - EduCode` : 'EduCode'
})

export default router
