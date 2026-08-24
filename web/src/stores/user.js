// 用户状态管理：保存登录信息，多页面共享
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  // 从localStorage读初始值，刷新页面不丢失登录状态
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || '',
  }),

  getters: {
    isLoggedIn: (state) => state.token !== '',
  },

  actions: {
    // 登录成功后调用：保存token和用户名
    setLoginInfo(token, username) {
      this.token = token
      this.username = username
      localStorage.setItem('token', token)
      localStorage.setItem('username', username)
    },

    // 退出登录：清空信息
    logout() {
      this.token = ''
      this.username = ''
      localStorage.removeItem('token')
      localStorage.removeItem('username')
    },
  },
})
