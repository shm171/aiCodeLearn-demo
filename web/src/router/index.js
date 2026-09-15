// 路由配置：学生端与教师端共用一套应用
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
        meta: { title: '提交作业', requiresAuth: true, roles: ['STUDENT'] },
      },
      {
        path: 'submissions',
        name: 'submissions',
        component: () => import('../views/student/SubmissionHistoryView.vue'),
        meta: { title: '提交历史', requiresAuth: true, roles: ['STUDENT'] },
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('../views/student/ProfileView.vue'),
        meta: { title: '个人中心', requiresAuth: true, roles: ['STUDENT'] },
      },
      {
        path: 'report',
        name: 'report',
        component: () => import('../views/student/ReportView.vue'),
        meta: { title: '学习报告', requiresAuth: true, roles: ['STUDENT'] },
      },
      {
        path: 'wrong-questions',
        name: 'wrong-questions',
        component: () => import('../views/student/WrongQuestionsView.vue'),
        meta: { title: '错题本', requiresAuth: true, roles: ['STUDENT'] },
      },
      {
        path: 'teacher/dashboard',
        name: 'teacher-dashboard',
        component: () => import('../views/teacher/TeacherDashboardView.vue'),
        meta: { title: '班级错题数据', requiresAuth: true, roles: ['TEACHER'] },
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

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (userStore.isLoggedIn && !userStore.role) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      userStore.logout()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  if (to.name === 'login' && userStore.isLoggedIn) {
    return userStore.homePath
  }

  const effectiveRole = userStore.role || 'STUDENT'
  const allowedRoles = to.meta.roles
  if (allowedRoles?.length && !allowedRoles.includes(effectiveRole)) {
    return userStore.homePath
  }

  if (to.name === 'home' && userStore.isTeacher) {
    return '/teacher/dashboard'
  }

  return true
})

export default router