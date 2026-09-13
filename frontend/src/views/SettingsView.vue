<template>
  <div class="settings-page">
    <h1 class="settings-title">设置</h1>

    <div class="settings-layout">
      <!-- 左侧分类侧边栏 -->
      <aside class="settings-nav">
        <button v-for="tab in tabs" :key="tab.key" type="button"
          class="settings-nav-item" :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key">
          <span class="settings-nav-icon">{{ tab.icon }}</span>
          <span>{{ tab.label }}</span>
        </button>
      </aside>

      <!-- 右侧内容区 -->
      <section class="settings-content">
        <!-- 个人信息 -->
        <div v-if="activeTab === 'profile'" class="settings-card">
          <h3 class="settings-card-title">个人信息</h3>

          <div class="profile-top">
            <div class="profile-avatar">{{ avatarChar }}</div>
            <div class="profile-meta">
              <div class="profile-name">{{ authStore.username || '用户' }}</div>
              <div class="profile-tag">本地账户</div>
            </div>
          </div>

          <div class="settings-form">
            <div class="form-group">
              <label class="form-label" for="profile-nickname">昵称</label>
              <div class="field field-countable" :class="{ filled: nickname.length > 0 }">
                <span class="field-icon">👤</span>
                <input id="profile-nickname" ref="nicknameInput" type="text" class="form-input" v-model="nickname"
                  maxlength="20" placeholder="给自己起个名字" autocomplete="nickname">
                <span class="field-suffix">
                  <span class="field-count">{{ nickname.length }}/20</span>
                  <button v-if="nickname" type="button" class="field-clear" title="清空昵称"
                    aria-label="清空昵称" @click="clearNickname">✕</button>
                </span>
              </div>
              <p class="form-tip">昵称仅保存在本设备，用于界面显示。</p>
            </div>
            <div class="settings-actions">
              <button type="button" class="settings-submit" @click="saveProfile" :disabled="profileLoading">
                {{ profileLoading ? '保存中...' : '保存' }}
              </button>
            </div>
          </div>

          <div class="settings-divider"></div>

          <h4 class="settings-subtitle">修改密码</h4>
          <div class="settings-form">
            <div class="form-group">
              <label class="form-label" for="old-password">旧密码</label>
              <div class="field">
                <span class="field-icon">🔒</span>
                <input id="old-password" type="password" class="form-input" v-model="oldPassword"
                  placeholder="请输入旧密码" autocomplete="current-password">
              </div>
            </div>
            <div class="form-group">
              <label class="form-label" for="new-password">新密码</label>
              <div class="field">
                <span class="field-icon">🔑</span>
                <input id="new-password" type="password" class="form-input" v-model="newPassword"
                  placeholder="至少 6 位" autocomplete="new-password">
              </div>
            </div>
            <div class="form-group">
              <label class="form-label" for="confirm-password">确认新密码</label>
              <div class="field">
                <span class="field-icon">🔑</span>
                <input id="confirm-password" type="password" class="form-input" v-model="confirmPassword"
                  placeholder="再次输入新密码" autocomplete="new-password">
              </div>
            </div>
            <div v-if="pwdMsg" class="settings-msg" :class="pwdOk ? 'ok' : 'err'">{{ pwdMsg }}</div>
            <div class="settings-actions">
              <button type="button" class="settings-submit" @click="submitPassword" :disabled="loading">
                {{ loading ? '提交中...' : '确认修改' }}
              </button>
            </div>
            <p class="settings-hint">修改成功后将自动退出，需要用新密码重新登录。</p>
          </div>
        </div>

        <!-- 观影偏好 -->
        <div v-else-if="activeTab === 'preference'" class="settings-card">
          <h3 class="settings-card-title">观影偏好</h3>
          <p class="settings-desc">设置默认打开的影片库排序与每页加载数量。</p>
          <div class="settings-form">
            <div class="form-group">
              <label class="form-label" for="pref-sort">默认排序</label>
              <div class="field field-select">
                <select id="pref-sort" class="form-input" v-model="prefSort">
                  <option value="vote_average.desc">评分最高</option>
                  <option value="popularity.desc">最热门</option>
                  <option value="primary_release_date.desc">最新上映</option>
                </select>
                <span class="field-caret">▾</span>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label" for="pref-page-size">每次加载数量</label>
              <div class="field field-select">
                <select id="pref-page-size" class="form-input" v-model="prefPageSize">
                  <option value="1">少（20 部/页）</option>
                  <option value="2">标准（40 部/次）</option>
                  <option value="3">多（60 部/次）</option>
                </select>
                <span class="field-caret">▾</span>
              </div>
            </div>
            <div class="settings-actions">
              <button type="button" class="settings-submit" @click="savePreference">保存</button>
            </div>
          </div>
        </div>

        <!-- 外观 -->
        <div v-else-if="activeTab === 'appearance'" class="settings-card">
          <h3 class="settings-card-title">外观</h3>
          <p class="settings-desc">选择界面的明暗风格，立即生效并自动记住。</p>
          <div class="appearance-options">
            <button v-for="opt in themeOptions" :key="opt.value" type="button"
              class="appearance-option" :class="{ active: themeStore.theme === opt.value }"
              :aria-pressed="themeStore.theme === opt.value"
              @click="themeStore.setTheme(opt.value)">
              <span class="appearance-swatch" :class="opt.value">
                <span class="swatch-bar"></span>
                <span class="swatch-body">
                  <span class="swatch-card"></span>
                  <span class="swatch-card"></span>
                  <span class="swatch-card"></span>
                </span>
              </span>
              <span class="appearance-option-name">{{ opt.label }}</span>
              <span class="appearance-option-state">
                {{ themeStore.theme === opt.value ? '✓ 当前' : '点击切换' }}
              </span>
            </button>
          </div>
          <p class="settings-hint appearance-hint">主题会保存在本设备，下次打开自动沿用。</p>
        </div>

        <!-- 关于 -->
        <div v-else-if="activeTab === 'about'" class="settings-card">
          <h3 class="settings-card-title">关于</h3>
          <div class="about-logo">🎬</div>
          <div class="about-name">菲林日记</div>
          <div class="about-sub">Film Diary · v1.0</div>
          <p class="settings-desc about-desc">
            一个记录观影生活的小应用：发现电影、管理想看/已看清单、写下影评。<br>
            电影数据由 <a href="https://www.themoviedb.org/" target="_blank" rel="noopener">TMDB</a> 提供。
          </p>
          <div class="settings-divider"></div>
          <div class="about-row"><span>前端</span><span>Vue 3 · Vite · Pinia · Bootstrap</span></div>
          <div class="about-row"><span>后端</span><span>Spring Boot · SQLite</span></div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { apiFetch, clearSession } from '@/utils/api'
import { useToast } from '@/composables/useToast'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const authStore = useAuthStore()
const { show: toastShow } = useToast()
const themeStore = useThemeStore()

const themeOptions = [
  { value: 'light', label: '浅色' },
  { value: 'dark', label: '深色' },
]

const tabs = [
  { key: 'profile', icon: '👤', label: '个人信息' },
  { key: 'preference', icon: '🎞', label: '观影偏好' },
  { key: 'appearance', icon: '🎨', label: '外观' },
  { key: 'about', icon: 'ℹ', label: '关于' },
]
const activeTab = ref('profile')

const avatarChar = computed(() => (authStore.username || 'U').charAt(0).toUpperCase())

/* ── 个人信息 ── */
const nickname = ref(localStorage.getItem('nickname') || '')
const nicknameInput = ref(null)
const profileLoading = ref(false)

// 清空后把光标留在输入框里，方便直接重新输入
async function clearNickname() {
  nickname.value = ''
  await nextTick()
  nicknameInput.value?.focus()
}

function saveProfile() {
  profileLoading.value = true
  setTimeout(() => {
    localStorage.setItem('nickname', nickname.value.trim())
    if (nickname.value.trim()) authStore.username = nickname.value.trim()
    profileLoading.value = false
    toastShow('个人信息已保存')
  }, 300)
}

/* ── 修改密码 ── */
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const pwdMsg = ref('')
const pwdOk = ref(false)
const loading = ref(false)

async function submitPassword() {
  pwdMsg.value = ''
  if (!oldPassword.value) { pwdMsg.value = '请输入旧密码'; pwdOk.value = false; return }
  if (newPassword.value.length < 6) { pwdMsg.value = '新密码长度至少 6 位'; pwdOk.value = false; return }
  if (newPassword.value !== confirmPassword.value) { pwdMsg.value = '两次输入的新密码不一致'; pwdOk.value = false; return }

  loading.value = true
  try {
    const res = await apiFetch('/api/password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ oldPassword: oldPassword.value, newPassword: newPassword.value }),
    })
    const data = await res.json()
    if (data.success) {
      toastShow('密码修改成功，请重新登录')
      clearSession()
      authStore.isLoggedIn = false
      router.replace('/login')
    } else {
      pwdMsg.value = data.message || '修改失败'
      pwdOk.value = false
    }
  } catch (e) {
    pwdMsg.value = '网络错误，请稍后重试'
    pwdOk.value = false
  } finally {
    loading.value = false
  }
}

/* ── 观影偏好 ── */
const prefSort = ref(localStorage.getItem('prefSort') || 'vote_average.desc')
const prefPageSize = ref(localStorage.getItem('prefPageSize') || '2')

function savePreference() {
  localStorage.setItem('prefSort', prefSort.value)
  localStorage.setItem('prefPageSize', prefPageSize.value)
  toastShow('偏好已保存，下次打开首页生效')
}
</script>

<style scoped>
.settings-page { max-width: 860px; }
.settings-title {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 28px;
}

/* ── 左右布局 ── */
.settings-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* 左侧分类导航 */
.settings-nav {
  flex: 0 0 180px;
  background: var(--bg-card);
  backdrop-filter: blur(12px);
  border: 1px solid var(--frost-border);
  border-radius: 14px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  position: sticky;
  top: 92px;
}
.settings-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 14px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: var(--ink-secondary);
  font-size: 14px;
  font-family: var(--font-body);
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}
.settings-nav-item:hover { background: var(--frost-hover); color: var(--ink); }
.settings-nav-item.active {
  background: var(--accent-bg);
  color: var(--accent);
  font-weight: 600;
}
.settings-nav-icon { width: 20px; text-align: center; }

/* 右侧内容 */
.settings-content { flex: 1; min-width: 0; }
.settings-card {
  background: var(--bg-card);
  backdrop-filter: blur(12px);
  border: 1px solid var(--frost-border);
  border-radius: 14px;
  padding: 28px 32px;
  box-shadow: var(--shadow-sm);
}
.settings-card-title {
  font-family: var(--font-display);
  font-size: 20px;
  margin-bottom: 20px;
}
.settings-subtitle {
  font-size: 16px;
  font-weight: 600;
  color: var(--ink);
  margin: 0 0 16px;
}
.settings-desc {
  font-size: 14px;
  color: var(--ink-muted);
  margin-bottom: 20px;
  line-height: 1.6;
}
.settings-divider {
  height: 1px;
  background: var(--frost-border);
  margin: 28px 0;
}
.settings-form { display: flex; flex-direction: column; gap: 18px; }

/* ── 表单控件 ── */
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-secondary);
  letter-spacing: 0.02em;
  transition: color 0.2s ease;
}
.form-group:focus-within .form-label { color: var(--accent); }
.form-tip {
  font-size: 12px;
  color: var(--ink-muted);
  line-height: 1.5;
}

/* 输入框容器（承载前后置图标） */
.field {
  position: relative;
  display: flex;
  align-items: center;
}
.field-icon {
  position: absolute;
  left: 15px;
  font-size: 15px;
  line-height: 1;
  color: var(--ink-muted);
  pointer-events: none;
  transition: color 0.2s ease, transform 0.2s ease;
}
.field:focus-within .field-icon {
  color: var(--accent);
  transform: scale(1.08);
}

.form-input {
  width: 100%;
  padding: 13px 16px;
  background-color: var(--frost);
  border: 1.5px solid var(--frost-border);
  border-radius: 12px;
  font-size: 14px;
  font-family: var(--font-body);
  color: var(--ink);
  outline: none;
  transition: border-color 0.25s ease, box-shadow 0.25s ease, background-color 0.25s ease;
}
.form-input::placeholder { color: var(--ink-muted); }
.form-input:hover:not(:focus) {
  border-color: var(--accent-border);
  background-color: var(--frost-hover);
}
.form-input:focus {
  border-color: var(--accent);
  background-color: var(--input-focus-bg);
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.14);
}

/* 带前置图标的输入框留出内边距 */
.field:not(.field-select) .form-input {
  padding-left: 43px;
  padding-right: 43px;
}
/* 昵称：右侧还有字数统计与清空按钮 */
.field.field-countable .form-input { padding-right: 96px; }

/* 昵称：字数统计与一键清空 */
.field-suffix {
  position: absolute;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.field-count {
  font-size: 12px;
  color: var(--ink-muted);
  font-variant-numeric: tabular-nums;
  opacity: 0;
  transform: translateX(4px);
  transition: opacity 0.2s ease, transform 0.2s ease, color 0.2s ease;
  pointer-events: none;
}
.field:focus-within .field-count,
.field.filled .field-count { opacity: 1; transform: translateX(0); }
.field.filled .field-count { color: var(--ink-secondary); }
.field-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background-color: var(--hover-tint);
  color: var(--ink-secondary);
  font-size: 11px;
  line-height: 1;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}
.field-clear:hover {
  background-color: var(--accent-fill-to);
  color: #fff;
  transform: scale(1.1);
}

/* 下拉选择框 */
.field-select .form-input {
  appearance: none;
  -webkit-appearance: none;
  padding-right: 40px;
  cursor: pointer;
}
.field-caret {
  position: absolute;
  right: 16px;
  font-size: 12px;
  color: var(--ink-muted);
  pointer-events: none;
  transition: color 0.2s ease, transform 0.2s ease;
}
.field-select:focus-within .field-caret {
  color: var(--accent);
  transform: translateY(1px);
}

.settings-actions { display: flex; justify-content: flex-end; }

/* 个人信息头部 */
.profile-top {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 24px;
}
.profile-avatar {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--accent-light) 0%, var(--accent) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  box-shadow: 0 4px 16px rgba(184, 134, 11, 0.25);
}
.profile-name {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
}
.profile-tag {
  display: inline-block;
  margin-top: 6px;
  padding: 2px 10px;
  border-radius: 20px;
  background: var(--frost);
  border: 1px solid var(--frost-border);
  font-size: 12px;
  color: var(--ink-muted);
}

/* 消息提示 */
.settings-msg {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
}
.settings-msg.err {
  background: var(--danger-bg);
  border: 1px solid var(--danger-border);
  color: var(--danger);
}
.settings-msg.ok {
  background: var(--success-bg);
  border: 1px solid var(--success-border);
  color: var(--success);
}

/* 提交按钮 */
.settings-submit {
  padding: 11px 32px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--accent-light) 0%, var(--accent) 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  font-family: var(--font-body);
  cursor: pointer;
  transition: all 0.25s ease;
}
.settings-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(184, 134, 11, 0.3);
}
.settings-submit:disabled { opacity: 0.6; cursor: not-allowed; }
.settings-hint {
  font-size: 12px;
  color: var(--ink-muted);
  text-align: right;
}

/* 外观选项 */
.appearance-options { display: flex; gap: 16px; }
.appearance-option {
  flex: 0 0 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 16px;
  border: 2px solid var(--frost-border);
  border-radius: 14px;
  background: var(--frost);
  font-size: 14px;
  font-family: var(--font-body);
  color: var(--ink);
  cursor: pointer;
  transition: border-color 0.2s ease, background-color 0.2s ease, transform 0.2s ease;
}
.appearance-option:hover { transform: translateY(-2px); border-color: var(--accent-border); }
.appearance-option.active { border-color: var(--accent); background: var(--accent-bg); }

/* 迷你界面预览：故意写死两套主题的本色，
   这样无论当前处于哪个主题，两个选项都能正确代表自己 */
.appearance-swatch {
  width: 100%;
  height: 76px;
  border-radius: 10px;
  border: 1px solid var(--frost-border);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: #f6f4ef;
}
.appearance-swatch.dark { background: #14120f; }
.swatch-bar {
  height: 14px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.78);
  border-bottom: 1px solid rgba(0, 0, 0, 0.07);
}
.appearance-swatch.dark .swatch-bar {
  background: rgba(26, 23, 20, 0.92);
  border-bottom-color: rgba(255, 255, 255, 0.10);
}
.swatch-body {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  padding: 8px;
}
.swatch-card {
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(0, 0, 0, 0.07);
}
.appearance-swatch.dark .swatch-card {
  background: rgba(38, 34, 30, 0.9);
  border-color: rgba(255, 255, 255, 0.10);
}
.swatch-body .swatch-card:first-child { box-shadow: inset 0 -7px 0 rgba(184, 134, 11, 0.4); }

.appearance-option-name { font-weight: 600; }
.appearance-option-state { font-size: 12px; color: var(--ink-muted); }
.appearance-option.active .appearance-option-state { color: var(--accent); font-weight: 600; }
.appearance-hint { text-align: left; margin-top: 18px; }

/* 关于 */
.about-logo { font-size: 44px; text-align: center; margin-bottom: 8px; }
.about-name {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 700;
  text-align: center;
}
.about-sub {
  text-align: center;
  font-size: 13px;
  color: var(--ink-muted);
  margin-bottom: 20px;
}
.about-desc { text-align: center; }
.about-desc a { color: var(--accent); }
.about-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--ink-secondary);
  padding: 8px 0;
}
.about-row span:first-child { color: var(--ink-muted); }

/* 移动端：侧栏横向排列 */
@media (max-width: 640px) {
  .settings-layout { flex-direction: column; }
  .settings-nav {
    flex: none;
    width: 100%;
    flex-direction: row;
    overflow-x: auto;
    position: static;
  }
  .settings-nav-item { flex: 1; justify-content: center; white-space: nowrap; }
  .settings-content { width: 100%; }
}
</style>
