<template>
  <aside ref="rootEl" class="sidebar" :class="{ open: sidebarOpen, compact: isCompact && !isMobile }">
    <div class="logo">
      <div class="logo-mark">
        <img src="@/assets/logo.svg" alt="菲林日记" class="logo-mark-icon" />
      </div>
      <div>
        <div class="logo-text">菲林日记</div>
        <div class="logo-sub">Film Diary</div>
      </div>
    </div>

    <div class="nav-pill">
      <div class="nav-section">
        <div class="nav-menu">
          <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }" @click="sidebarClose">
            <span class="nav-icon nav-icon-brand" aria-hidden="true">
              <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" focusable="false">
                <path d="M625.566 589.1c31.214 0 56.52 25.304 56.52 56.52 0 31.214-25.306 56.52-56.52 56.52H399.485c-31.214 0-56.518-25.306-56.518-56.52 0-31.216 25.304-56.52 56.518-56.52h226.081z m301.439 282.599c0 31.214-25.306 56.52-56.52 56.52h-471c-31.214 0-56.518-25.306-56.518-56.52 0-31.216 25.304-56.52 56.518-56.52h414.48V502.075L512.319 232.402 211.085 501.366v370.333c0 31.214-25.304 56.52-56.52 56.52-31.214 0-56.518-25.306-56.518-56.52V476.742c-0.396-15.064 5.043-30.249 16.56-41.746l357.495-319.203c22.267-22.231 58.374-22.231 80.642 0 0.273 0.271 0.45 0.599 0.716 0.874l356.672 318.866c4.103 4.101 7.189 8.774 9.778 13.642 4.378 8.028 7.093 17.095 7.093 26.885v395.639z" fill="#5E9DF3"></path>
                <path d="M512.546 99.114c-31.216 0-56.522 25.304-56.522 56.52 0 31.214 25.306 56.52 56.522 56.52 31.214 0 56.52-25.306 56.52-56.52 0-31.216-25.306-56.52-56.52-56.52z m357.959 715.918c-31.216 0-56.52 25.304-56.52 56.52 0 31.214 25.304 56.52 56.52 56.52 31.214 0 56.52-25.306 56.52-56.52-0.001-31.216-25.306-56.52-56.52-56.52z m-471-226.079c-31.214 0-56.518 25.304-56.518 56.52 0 31.214 25.304 56.52 56.518 56.52 31.216 0 56.52-25.306 56.52-56.52-0.001-31.217-25.305-56.52-56.52-56.52z" fill="#3080EE"></path>
              </svg>
            </span> 首页
          </router-link>
          <router-link to="/discover" class="nav-item" :class="{ active: $route.path === '/discover' }" @click="sidebarClose">
            <span class="nav-icon">◎</span> 发现
          </router-link>
          <router-link to="/community" class="nav-item" :class="{ active: $route.path === '/community' }" @click="sidebarClose">
            <span class="nav-icon">💬</span> 社区
          </router-link>
          <router-link to="/records" class="nav-item" :class="{ active: $route.path === '/records' }" @click="sidebarClose">
            <span class="nav-icon">◷</span> 观影记录
          </router-link>
          <router-link to="/reviews" class="nav-item" :class="{ active: $route.path === '/reviews' }" @click="sidebarClose">
            <span class="nav-icon">▢</span> 我的影评
          </router-link>
          <router-link to="/settings" class="nav-item" :class="{ active: $route.path === '/settings' }" @click="sidebarClose">
            <span class="nav-icon">⚙</span> 设置
          </router-link>
        </div>
      </div>
    </div>

    <div class="sidebar-spacer"></div>

    <div class="sidebar-footer">
      <div class="avatar">{{ authStore.username?.charAt(0)?.toUpperCase() || 'U' }}</div>
      <div class="user-meta">
        <div class="user-name">{{ authStore.username || '用户' }}</div>
      </div>
      <button type="button" class="logout-btn" @click="authStore.logout()">退出</button>
    </div>
  </aside>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useSidebar } from '@/composables/useSidebar'

const authStore = useAuthStore()
const { isOpen: sidebarOpen, close: sidebarClose } = useSidebar()

/* 下滑时整条导航两端向中间收拢成胶囊；回顶时从中间向两边撑开 */
const isCompact = ref(false)
const isMobile = ref(false)
let lastY = 0
let mq = null

/** 测量胶囊目标宽度（菜单 + 内边距），写入 CSS 变量供 max-width 平滑插值 */
function measureCapsule() {
  const el = document.querySelector('.sidebar')
  if (!el) return
  const menu = el.querySelector('.nav-pill')
  if (!menu) return
  // 菜单宽 + 胶囊左右内边距(10*2) + 边框(1*2) + 一点余量
  const w = Math.ceil(menu.getBoundingClientRect().width) + 20 + 2 + 4
  el.style.setProperty('--capsule-width', `${w}px`)
}

function onScroll() {
  const y = window.scrollY
  const next = y > 80 && y > lastY
  if (next && !isCompact.value) measureCapsule() // 收缩前刷新目标宽度（字体/窗口变化后仍精确）
  isCompact.value = next
  if (y <= 80) isCompact.value = false
  lastY = y
}

function syncMobile() {
  isMobile.value = window.matchMedia('(max-width: 768px)').matches
}

onMounted(async () => {
  syncMobile()
  mq = window.matchMedia('(max-width: 768px)')
  mq.addEventListener('change', syncMobile)
  window.addEventListener('scroll', onScroll, { passive: true })
  window.addEventListener('resize', measureCapsule)
  await nextTick()
  // 等字体/图标渲染完再量，避免初始测量偏小
  setTimeout(measureCapsule, 300)
  measureCapsule()
})
onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('resize', measureCapsule)
  mq?.removeEventListener('change', syncMobile)
})
</script>

<style scoped>
.logo-mark {
  overflow: hidden;
}
.logo-mark-icon {
  width: 78%;
  height: 78%;
  object-fit: contain;
  display: block;
}
.logout-btn {
  padding: 7px 14px;
  border-radius: 8px;
  border: 1px solid var(--frost-border);
  background: var(--frost);
  color: var(--ink-secondary);
  font-size: 13px;
  font-family: var(--font-body);
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}
.logout-btn:hover {
  background: var(--danger-bg);
  border-color: var(--danger-border);
  color: var(--danger);
}
</style>
