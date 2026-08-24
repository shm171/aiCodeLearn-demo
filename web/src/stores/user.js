// ============================================================
// 用户状态管理（Pinia store）
// 作用：保存"整个应用都要用到"的用户数据（比如登录状态），
//      任何页面都可以用 useUserStore() 来读取或修改这些数据。
//
// 什么时候需要用到 store？
// 当一个数据在"多个页面"都要用到时（比如用户名要显示在
// 导航栏和首页），就放进 store；只在一个页面用的数据，
// 直接写在那个页面的 <script setup> 里就行。
// ============================================================

import { defineStore } from 'pinia'

// defineStore 的第一个参数 'user' 是这个 store 的唯一名字（不要和其他 store 重复）
// 约定俗成的写法：export const useXxxStore = defineStore('xxx', {...})
export const useUserStore = defineStore('user', {
  // ---------- state：数据（相当于组件里的 data） ----------
  // 注意：state 必须是一个"返回对象的函数"
  state: () => ({
    // token 是登录后后端发的"通行证"，以后每次请求都要带上它
    // 从 localStorage 里读初始值，这样刷新页面后登录状态不会丢
    token: localStorage.getItem('token') || '',
    // 用户名（同样从 localStorage 读取）
    username: localStorage.getItem('username') || '',
  }),

  // ---------- getters：计算属性（根据 state 算出来的值） ----------
  getters: {
    // 是否已登录：token 不为空就是已登录
    isLoggedIn: (state) => state.token !== '',
  },

  // ---------- actions：方法（用来修改数据） ----------
  actions: {
    // 保存登录信息（登录成功后调用）
    setLoginInfo(token, username) {
      this.token = token
      this.username = username
      // 同时存到 localStorage，刷新页面也不会丢
      localStorage.setItem('token', token)
      localStorage.setItem('username', username)
    },

    // 退出登录：清空登录信息
    logout() {
      this.token = ''
      this.username = ''
      localStorage.removeItem('token')
      localStorage.removeItem('username')
    },
  },
})
