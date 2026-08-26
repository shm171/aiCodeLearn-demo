// 路由与权限守卫：未登录跳登录页；教师/管理员路由按 meta.roles 控制
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

declare module 'vue-router' {
  interface RouteMeta {
    /** 公开路由（无需登录），如登录页 */
    public?: boolean
    /** 允许访问的角色，空表示登录即可访问 */
    roles?: string[]
    /** 页面标题 */
    title?: string
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true, title: '登录 · AI Learn 教师端' },
  },
  {
    path: '/',
    redirect: () => {
      const store = useUserStore()
      return store.token ? '/teacher/dashboard' : '/login'
    },
  },
  {
    path: '/teacher',
    component: () => import('@/layouts/TeacherLayout.vue'),
    meta: { roles: ['TEACHER'] },
    children: [
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: () => import('@/views/teacher/Dashboard.vue'),
        meta: { roles: ['TEACHER'], title: '数据看板 · 教师端' },
      },
      {
        path: 'questions',
        name: 'TeacherQuestions',
        component: () => import('@/views/teacher/QuestionList.vue'),
        meta: { roles: ['TEACHER'], title: '题目与作业管理 · 教师端' },
      },
      {
        path: 'review',
        name: 'TeacherReview',
        component: () => import('@/views/teacher/ReviewList.vue'),
        meta: { roles: ['TEACHER'], title: '学生提交审阅 · 教师端' },
      },
      {
        path: 'review/:id',
        name: 'TeacherReviewDetail',
        component: () => import('@/views/teacher/ReviewDetail.vue'),
        meta: { roles: ['TEACHER'], title: '审阅详情 · 教师端' },
      },
      {
        path: 'wrong-questions',
        name: 'TeacherWrongQuestions',
        component: () => import('@/views/teacher/WrongQuestions.vue'),
        meta: { roles: ['TEACHER'], title: '错题管理 · 教师端' },
      },
      {
        path: 'students',
        name: 'TeacherStudents',
        component: () => import('@/views/teacher/Students.vue'),
        meta: { roles: ['TEACHER'], title: '学生学习报告 · 教师端' },
      },
      {
        path: 'students/:id/report',
        name: 'TeacherStudentReport',
        component: () => import('@/views/teacher/StudentReport.vue'),
        meta: { roles: ['TEACHER'], title: '学习报告详情 · 教师端' },
      },
      {
        path: 'profile',
        name: 'TeacherProfile',
        component: () => import('@/views/teacher/Profile.vue'),
        meta: { roles: ['TEACHER'], title: '个人中心 · 教师端' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const store = useUserStore()

  // 公开页面直接放行
  if (to.meta.public) return true

  // 未登录 -> 登录页（携带回跳地址）
  if (!store.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 已登录但还没有 Profile（角色）时拉取一次；失败说明 Token 失效
  if (!store.profile) {
    try {
      await store.fetchProfile()
    } catch {
      store.logout()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  // 角色校验（学生/教师/管理员细粒度授权待后端负责人统一后接入）
  const roles = to.meta.roles
  if (roles && roles.length && !roles.includes(store.role)) {
    if (store.role === 'TEACHER') return '/teacher/dashboard'
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · AI Learn` : 'AI Learn · 教师端'
})

export default router