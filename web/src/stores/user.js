// 用户状态管理：学生和教师共用一套登录状态
import { defineStore } from 'pinia'
import { getUserInfoApi, getUserProfileApi } from '../api/user'

function getTeacherCandidateId() {
  return Number(import.meta.env.VITE_TEMP_TEACHER_ID) || null
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: Number(localStorage.getItem('userId')) || null,
    email: localStorage.getItem('email') || '',
    username: localStorage.getItem('username') || '',
    role: localStorage.getItem('role') || '',
  }),

  getters: {
    isLoggedIn: (state) => state.token !== '',
    isTeacher: (state) => state.role === 'TEACHER',
    isStudent: (state) => state.role === 'STUDENT' || state.role === '',
    homePath: (state) => state.role === 'TEACHER' ? '/teacher/dashboard' : '/',
  },

  actions: {
    setLoginInfo(token, email) {
      this.token = token
      this.email = email
      localStorage.setItem('token', token)
      localStorage.setItem('email', email)
    },

    setUserId(userId, email) {
      this.userId = userId
      this.email = email
      localStorage.setItem('userId', String(userId))
      localStorage.setItem('email', email)
    },

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

    async loadCandidate(userId) {
      if (!userId) return false

      try {
        const [user, profile] = await Promise.all([
          getUserInfoApi(userId),
          getUserProfileApi(userId),
        ])

        // 防止 A 账号的 userId 被错误套到 B 账号的登录 Token 上
        if (this.email && user?.email && user.email !== this.email) {
          return false
        }

        this.userId = userId
        localStorage.setItem('userId', String(userId))
        this.setProfile({
          email: user?.email || this.email,
          username: profile?.username,
          role: profile?.role,
        })
        return true
      } catch {
        return false
      }
    },

    // 学生使用注册时保存的 ID；已有教师账号使用环境变量里的临时教师 ID
    async fetchUserInfo() {
      const candidates = []

      if (this.userId) candidates.push(Number(this.userId))

      const teacherId = getTeacherCandidateId()
      if (teacherId && !candidates.includes(teacherId)) {
        candidates.push(teacherId)
      }

      for (const candidate of candidates) {
        if (await this.loadCandidate(candidate)) {
          return true
        }
      }

      return !!this.role
    },

    // 退出时保留 userId 和 email 作为下次登录的匹配线索，清除其余认证状态
    logout() {
      this.token = ''
      this.username = ''
      this.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
    },
  },
})