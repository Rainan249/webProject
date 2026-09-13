import { onMounted, onUnmounted } from 'vue'

export function useInfiniteScroll(callback, options = {}) {
  const { threshold = 600 } = options
  let el = null

  function handleScroll() {
    if (el === window) {
      const scrollBottom = window.innerHeight + window.scrollY
      const docHeight = document.documentElement.scrollHeight
      if (scrollBottom >= docHeight - threshold) callback()
      return
    }
    if (el && el.scrollTop + el.clientHeight >= el.scrollHeight - threshold) {
      callback()
    }
  }

  onMounted(() => {
    // 只有当 .main 真正可滚动（内容超出容器高度）时才监听它，
    // 否则实际滚动的是 window/body，监听 .main 永远不会触发
    const main = document.querySelector('.main')
    el = main && main.scrollHeight > main.clientHeight + 1 ? main : window
    el.addEventListener('scroll', handleScroll, { passive: true })
    // 内容不足一屏时也尝试触发一次，避免"永远滚不到加载点"
    handleScroll()
  })

  onUnmounted(() => {
    if (el) el.removeEventListener('scroll', handleScroll)
  })
}