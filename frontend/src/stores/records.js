import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useConfirm } from '@/composables/useConfirm'
import { useToast } from '@/composables/useToast'
import { useMovieStore } from '@/stores/movies'
import { apiFetch } from '@/utils/api'

export const useRecordStore = defineStore('records', () => {
  const records = ref([])
  const currentFilter = ref('all')
  const isLoading = ref(false)
  const { show: confirmShow } = useConfirm()
  const { show: toastShow } = useToast()

  async function loadRecords() {
    isLoading.value = true
    try {
      const url = currentFilter.value === 'all'
        ? '/api/records'
        : `/api/records/status/${currentFilter.value}`
      const res = await apiFetch(url)
      records.value = await res.json()
    } catch (e) {
      console.error('加载记录失败:', e)
    } finally {
      isLoading.value = false
    }
  }

  async function deleteRecord(id, title) {
    const ok = await confirmShow('删除记录', `确定要删除「${title}」吗？<br>此操作不可恢复。`)
    if (!ok) return
    try {
      await apiFetch(`/api/records/${id}`, { method: 'DELETE' })
      // 同步首页"已添加"按钮状态
      const movieStore = useMovieStore()
      const rec = records.value.find(r => r.id === id)
      if (rec) movieStore.recordMovieIds.delete(rec.movieId)
      toastShow('已删除')
      loadRecords()
    } catch (e) {
      toastShow('删除失败', 'error')
    }
  }

  async function markAsWatched(movieId) {
    try {
      const res = await apiFetch(`/api/records/movie/${movieId}`)
      if (res.ok) {
        const text = await res.text()
        if (text) {
          const record = JSON.parse(text)
          if (record && record.id && record.status === 'wishlist') {
            await apiFetch(`/api/records/${record.id}/status?status=watched`, { method: 'PUT' })
          }
        }
      }
    } catch (e) {
      console.error('更新状态失败:', e)
    }
  }

  return { records, currentFilter, isLoading, loadRecords, deleteRecord, markAsWatched }
})
