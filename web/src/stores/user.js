// 用户状态管理：保存登录信息，多页面共享
import { defineStore } from 'pinia'
import { getUserInfoApi, getUserProfileApi } from '../api/user'

export const useUserStore = defineStore('user', {
  // 从localStorage读初始值，刷新页面不丢失登录状态
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: Number(localStorage.getItem('userId')) || null,
    email: localStorage.getItem('email') || '',
    username: localStorage.getItem('username') || '',
    role: localStorage.getItem('role') || '',
  }),

  getters: {
    isLoggedIn: (state) => state.token !== '',
  },

  actions: {
    // 登录成功后调用：保存token、用户ID和邮箱（登录接口会返回这三项）
    setLoginInfo(token, userId, email) {
      this.token = token
      this.userId = userId
      this.email = email
      localStorage.setItem('token', token)
      localStorage.setItem('userId', String(userId))
      localStorage.setItem('email', email)
    },

    // 注册成功后调用：保存用户ID
    setUserId(userId, email) {
      this.userId = userId
      this.email = email
      localStorage.setItem('userId', String(userId))
      localStorage.setItem('email', email)
    },

    // 保存用户档案信息
    setProfile({ email, username, role }) {
      if (email) {
        this.email = email
        localStorage.setItem('email', email)
      }
      if (username !== undefined) {
        this.username = username
        localStorage.setItem('username', username)
      }
      if (role !== undefined) {
        this.role = role
        localStorage.setItem('role', role)
      }
    },

    // 登录后拉取真实用户信息（用户ID来自登录/注册响应）
    // 个别接口失败不阻断登录流程，只更新拿到的部分
    async fetchUserInfo() {
      if (!this.userId) return
      const [user, profile] = await Promise.all([
        getUserInfoApi(this.userId).catch(() => null),
        getUserProfileApi(this.userId).catch(() => null),
      ])
      this.setProfile({
        email: user?.email || undefined,
        username: profile?.username,
        role: profile?.role || undefined,
      })
    },

    // 退出登录：清空信息
    logout() {
      this.token = ''
      this.userId = null
      this.email = ''
      this.username = ''
      this.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('email')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
    },
  },
})
