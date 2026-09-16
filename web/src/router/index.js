// 路由配置：学员端（个人自学平台）
import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('../views/HomeView.vue'),
        meta: { title: '首页', requiresAuth: false },
      },
      {
        path: 'submit',
        name: 'submit',
        component: () => import('../views/student/SubmitView.vue'),
        meta: { title: '提交作业', requiresAuth: true },
      },
      {
        path: 'submissions',
        name: 'submissions',
        component: () => import('../views/student/SubmissionHistoryView.vue'),
        meta: { title: '提交历史', requiresAuth: true },
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('../views/student/ProfileView.vue'),
        meta: { title: '个人中心', requiresAuth: true },
      },
      {
        path: 'report',
        name: 'report',
        component: () => import('../views/student/ReportView.vue'),
        meta: { title: '学习报告', requiresAuth: true },
      },
      {
        path: 'wrong-questions',
        name: 'wrong-questions',
        component: () => import('../views/student/WrongQuestionsView.vue'),
        meta: { title: '错题本', requiresAuth: true },
      },
    ],
  },
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

router.beforeEach(async (to) => {
  document.title = to.meta.title ? `${to.meta.title} - EduCode` : 'EduCode'

  const userStore = useUserStore()

  // 访问需要登录的页面，但没登录 → 跳登录页（带回来时地址）
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 已登录用户访问登录页 → 直接回首页
  if (to.name === 'login' && userStore.isLoggedIn) {
    return '/'
  }

  return true
})

export default router
