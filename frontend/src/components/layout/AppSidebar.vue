<template>
  <aside class="sidebar" :class="{ open: sidebarOpen }">
    <div class="logo">
      <div class="logo-mark">
        <img src="@/assets/logo.svg" alt="菲林日记" class="logo-mark-icon" />
      </div>
      <div>
        <div class="logo-text">菲林日记</div>
        <div class="logo-sub">Film Diary</div>
      </div>
    </div>

    <div class="nav-section">
      <div class="nav-label">导航</div>
      <div class="nav-menu">
        <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }" @click="sidebarClose">
          <span class="nav-icon">◈</span> 首页
        </router-link>
        <router-link to="/discover" class="nav-item" :class="{ active: $route.path === '/discover' }" @click="sidebarClose">
          <span class="nav-icon">◎</span> 发现
        </router-link>
        <router-link to="/records" class="nav-item" :class="{ active: $route.path === '/records' }" @click="sidebarClose">
          <span class="nav-icon">◷</span> 观影记录
        </router-link>
        <router-link to="/reviews" class="nav-item" :class="{ active: $route.path === '/reviews' }" @click="sidebarClose">
          <span class="nav-icon">▢</span> 我的影评
        </router-link>
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
import { useAuthStore } from '@/stores/auth'
import { useSidebar } from '@/composables/useSidebar'

const authStore = useAuthStore()
const { isOpen: sidebarOpen, close: sidebarClose } = useSidebar()
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
  background: rgba(220, 53, 69, 0.08);
  border-color: rgba(220, 53, 69, 0.25);
  color: #dc3545;
}
</style>
