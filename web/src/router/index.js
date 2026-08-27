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

  // 主布局（带导航栏），首页公开，其他页面按需加 requiresAuth
  {
    path: '/',
    component: MainLayout,
    children: [
      // 首页：公开，不需要登录
      {
        path: '',
        name: 'home',
        component: () => import('../views/HomeView.vue'),
        meta: { title: '首页', requiresAuth: false },
      },
      // 提交作业：需要登录
      {
        path: 'submit',
        name: 'submit',
        component: () => import('../views/student/SubmitView.vue'),
        meta: { title: '提交作业', requiresAuth: true },
      },
      // 个人中心：需要登录
      {
        path: 'profile',
        name: 'profile',
        component: () => import('../views/student/ProfileView.vue'),
        meta: { title: '个人中心', requiresAuth: true },
      },
      // 以后加需要登录的页面，单独加 meta: { requiresAuth: true }
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
