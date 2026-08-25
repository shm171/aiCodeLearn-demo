// 路由配置：定义网址和页面对应关系
import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

const routes = [
  // 登录页（不需要登录）
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false },
  },

  // 主布局（带导航栏），子页面都需要登录
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true }, // 父路由标记需要登录，子路由继承
    children: [
      // 首页
      {
        path: '',
        name: 'home',
        component: () => import('../views/HomeView.vue'),
        meta: { title: '首页' },
      },
      // 以后加新页面在 children 里照着加
    ],
  },

  // 404兜底
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

// 全局前置守卫：登录校验 + 设置页面标题
router.beforeEach((to, from, next) => {
  // 设置浏览器标签标题
  document.title = to.meta.title ? `${to.meta.title} - EduCode` : 'EduCode'

  // 判断是否已登录（localStorage里有没有token）
  const isLoggedIn = !!localStorage.getItem('token')

  // 访问需要登录的页面，但没登录 → 跳登录页
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
    return
  }

  // 已登录用户访问登录页 → 直接跳首页
  if (to.name === 'login' && isLoggedIn) {
    next('/')
    return
  }

  next() // 其他情况正常放行
})

export default router
