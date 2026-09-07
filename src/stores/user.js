// Pinia 用户状态：token 持久化、Profile 与角色
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getProfile, login as apiLogin, updateProfile as apiUpdateProfile } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const profile = ref(null)

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => profile.value?.role || '')

  async function login(payload) {
    const resp = await apiLogin(payload)
    token.value = resp.token
    localStorage.setItem('token', resp.token)
    await fetchProfile()
  }

  async function fetchProfile() {
    profile.value = await getProfile()
  }

  async function updateProfile(data) {
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