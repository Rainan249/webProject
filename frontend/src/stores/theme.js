import { defineStore } from 'pinia'
import { ref } from 'vue'

const STORAGE_KEY = 'theme'
const THEMES = ['light', 'dark']

/**
 * 读取初始主题：
 * 1. 用户显式选过 -> 用它
 * 2. 没选过 -> 跟随系统偏好
 */
function resolveInitialTheme() {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (THEMES.includes(saved)) return saved
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

/**
 * 主题落在 <html data-theme> 上，:root / [data-theme="dark"] 两套变量
 * 定义在 styles/style.css。首屏由 index.html 里的内联脚本提前打标，
 * 避免浅色先渲染一帧再跳深色（FOUC）。
 */
function applyTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme)
}

export const useThemeStore = defineStore('theme', () => {
  const theme = ref(resolveInitialTheme())
  applyTheme(theme.value)

  function setTheme(next) {
    if (!THEMES.includes(next)) return
    theme.value = next
    localStorage.setItem(STORAGE_KEY, next)
    applyTheme(next)
  }

  function toggleTheme() {
    setTheme(theme.value === 'dark' ? 'light' : 'dark')
  }

  return { theme, setTheme, toggleTheme }
})
