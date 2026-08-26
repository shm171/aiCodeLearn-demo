// 用户状态管理：保存登录信息，多页面共享
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  // 从localStorage读初始值，刷新页面不丢失登录状态
  state: () => ({
    token: localStorage.getItem('token') || '',
    email: localStorage.getItem('email') || '',
  }),

  getters: {
    isLoggedIn: (state) => state.token !== '',
  },

  actions: {
    // 登录成功后调用：保存token和邮箱
    setLoginInfo(token, email) {
      this.token = token
      this.email = email
      localStorage.setItem('token', token)
      localStorage.setItem('email', email)
    },

    // 退出登录：清空信息
    logout() {
      this.token = ''
      this.email = ''
      localStorage.removeItem('token')
      localStorage.removeItem('email')
    },
  },
})
