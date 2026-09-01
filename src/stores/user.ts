// Pinia 用户状态：token 持久化、Profile 与角色
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getProfile, login as apiLogin, updateProfile as apiUpdateProfile } from '@/api/auth'
import type { LoginReq, Profile } from '@/api/schema'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const profile = ref<Profile | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  /** 角色来自 Profile（README：角色不写入 JWT，需从 Profile 查询） */
  const role = computed(() => profile.value?.role || '')

  async function login(payload: LoginReq) {
    const resp = await apiLogin(payload)
    token.value = resp.token
    localStorage.setItem('token', resp.token)
    await fetchProfile()
  }

  async function fetchProfile() {
    profile.value = await getProfile()
  }

  async function updateProfile(data: Partial<Profile>) {
    if (!profile.value?.id) throw new Error('缺少当前用户 ID')
    profile.value = await apiUpdateProfile(profile.value.id, data)
  }

  function logout() {
    token.value = ''
    profile.value = null
    localStorage.removeItem('token')
  }

  return { token, profile, isLoggedIn, role, login, fetchProfile, updateProfile, logout }
})