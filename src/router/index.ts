// 路由与权限守卫：未登录跳登录页；管理端路由要求 ADMIN 角色
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    roles?: string[]
    title?: string
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true, title: '登录 · AI Learn 管理端' },
  },
  {
    path: '/',
    redirect: () => {
      const store = useUserStore()
      return store.token ? '/admin/dashboard' : '/login'
    },
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { roles: ['ADMIN'] },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/AdminDashboard.vue'),
        meta: { roles: ['ADMIN'], title: '数据概览 · 管理端' },
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { roles: ['ADMIN'], title: '用户管理 · 管理端' },
      },
      {
        path: 'roles',
        name: 'AdminRoles',
        component: () => import('@/views/admin/RoleManage.vue'),
        meta: { roles: ['ADMIN'], title: '角色与权限 · 管理端' },
      },
      {
        path: 'config',
        name: 'AdminConfig',
        component: () => import('@/views/admin/ConfigManage.vue'),
        meta: { roles: ['ADMIN'], title: '基础配置 · 管理端' },
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/Profile.vue'),
        meta: { roles: ['ADMIN'], title: '个人中心 · 管理端' },
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
  if (to.meta.public) return true
  if (!store.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (!store.profile) {
    try {
      await store.fetchProfile()
    } catch {
      store.logout()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }
  const roles = to.meta.roles
  if (roles && roles.length && !roles.includes(store.role)) {
    if (store.role === 'ADMIN') return '/admin/dashboard'
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · AI Learn` : 'AI Learn · 管理端'
})

export default router