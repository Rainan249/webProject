import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import router from '@/router'
import { setToken, getToken, clearSession } from '@/utils/api'

export const useAuthStore = defineStore('auth', () => {
  const isLoggedIn = ref(false)
  const username = ref('')

  const isAuthenticated = computed(() => isLoggedIn.value)

  // 页面刷新后：有 Token 即视为已登录（服务端重启或 Token 失效时，
  // 第一个 /api 请求会收到 401 并自动清会话跳回登录页）
  if (getToken()) {
    isLoggedIn.value = true
    username.value = localStorage.getItem('savedUsername') || ''
  }

  async function login(user, password) {
    const res = await fetch('/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: user, password }),
    })
    const data = await res.json()
    if (data.success) {
      isLoggedIn.value = true
      username.value = data.username || user
      setToken(data.token) // 保存会话令牌
      return { success: true }
    }
    return { success: false, message: data.message || '账号或密码错误' }
  }

  async function logout() {
    // 通知服务端销毁会话（失败不阻塞本地登出）
    try {
      await fetch('/api/logout', { method: 'POST', headers: { 'X-Auth-Token': getToken() || '' } })
    } catch { /* ignore */ }
    clearSession()
    router.push('/login')
  }

  return { isLoggedIn, username, isAuthenticated, login, logout }
})
