<template>
  <div class="toast-container">
    <TransitionGroup name="toast">
      <div v-for="toast in toasts" :key="toast.id" class="toast" :class="`toast-${toast.type}`">
        <span class="toast-icon">{{ toast.type === 'success' ? '✓' : toast.type === 'error' ? '✕' : 'ℹ' }}</span>
        {{ toast.message }}
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { useToast } from '@/composables/useToast'
const { toasts } = useToast()
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.toast {
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  backdrop-filter: blur(12px);
  box-shadow: var(--shadow-md);
  display: flex;
  align-items: center;
  gap: 8px;
  animation: toastIn 0.3s ease;
}
.toast-success { background: var(--success-bg); color: var(--success); border: 1px solid var(--success-border); }
.toast-error { background: var(--danger-bg); color: var(--danger); border: 1px solid var(--danger-border); }
.toast-info { background: var(--accent-bg); color: var(--accent); border: 1px solid var(--accent-border); }
.toast-enter-active { animation: toastIn 0.3s ease; }
.toast-leave-active { animation: toastOut 0.3s ease; }
@keyframes toastIn { from { opacity: 0; transform: translateX(40px); } to { opacity: 1; transform: translateX(0); } }
@keyframes toastOut { from { opacity: 1; transform: translateX(0); } to { opacity: 0; transform: translateX(40px); } }
</style>
