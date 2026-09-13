/**
 * 统一 API 请求封装：
 * - 自动附带会话令牌（X-Auth-Token 请求头）
 * - 收到 401 时清除本地登录态并跳转登录页
 */
const TOKEN_KEY = 'authToken'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem('rememberMe')
  localStorage.removeItem('savedUsername')
  localStorage.removeItem('savedPassword')
  try {
    // 动态引入避免循环依赖（router -> stores/auth -> api）
    import('@/stores/auth').then(({ useAuthStore }) => {
      const authStore = useAuthStore()
      authStore.isLoggedIn = false
      authStore.username = ''
    })
  } catch { /* ignore */ }
  if (!location.hash.startsWith('#/login')) {
    location.hash = '#/login'
  }
}

export async function apiFetch(url, options = {}) {
  const token = getToken()
  const headers = { ...(options.headers || {}) }
  if (token) headers['X-Auth-Token'] = token

  const res = await fetch(url, { ...options, headers })

  if (res.status === 401) {
    clearSession()
    throw new Error('未登录或会话已过期')
  }
  return res
}
