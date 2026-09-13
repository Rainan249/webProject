<template>
  <div class="community-page">
    <div class="community-header">
      <h1 class="community-title">社区</h1>
      <p class="community-sub">来自所有用户的影评 · 共 {{ reviews.length }} 条</p>
    </div>

    <LoadingSpinner v-if="isLoading" />

    <div v-else-if="reviews.length === 0" class="community-empty">
      <div class="empty-icon">💬</div>
      <p>还没有人留下评价，去写第一篇影评吧</p>
    </div>

    <div v-else class="community-list">
      <article v-for="review in reviews" :key="review.id" class="community-card">
        <div class="review-side">
          <div class="review-avatar">{{ (review.username || '匿').charAt(0).toUpperCase() }}</div>
          <div class="review-user">
            <div class="review-user-name">{{ review.username || '匿名用户' }}</div>
            <div class="review-time">{{ review.updatedAt || review.createdAt }}</div>
          </div>
        </div>

        <div class="review-main">
          <div class="review-movie" @click="goMovie(review)">
            <img v-if="review.posterPath" :src="`https://image.tmdb.org/t/p/w92${review.posterPath}`" :alt="review.title" class="review-movie-poster">
            <div v-else class="review-movie-poster fallback">🎬</div>
            <div class="review-movie-info">
              <div class="review-movie-title">{{ review.title }}</div>
              <div class="review-movie-meta">
                <span>TMDB {{ review.tmdbRating ? review.tmdbRating.toFixed(1) : '—' }}</span>
                <span v-if="review.releaseDate">{{ review.releaseDate?.slice(0, 4) }}</span>
              </div>
            </div>
          </div>

          <div class="review-stars">
            <span class="stars">{{ '★'.repeat(Math.round((review.userRating || 0) / 2)) }}{{ '☆'.repeat(5 - Math.round((review.userRating || 0) / 2)) }}</span>
            <span class="stars-num">{{ review.userRating ?? 0 }}/10</span>
          </div>

          <p class="review-content">{{ review.content }}</p>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { apiFetch } from '@/utils/api'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

const router = useRouter()
const reviews = ref([])
const isLoading = ref(true)

async function load() {
  isLoading.value = true
  try {
    const res = await apiFetch('/api/community/reviews')
    reviews.value = await res.json()
  } catch (e) {
    console.error('加载社区影评失败:', e)
  } finally {
    isLoading.value = false
  }
}

function goMovie(review) {
  // 跳到首页搜索该电影（详情弹窗依赖影片库数据）
  router.push({ path: '/', query: { search: review.title } })
}

onMounted(load)
</script>

<style scoped>
.community-page { max-width: 860px; }
.community-header { margin-bottom: 28px; }
.community-title {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}
.community-sub { font-size: 14px; color: var(--ink-muted); }

.community-empty {
  text-align: center;
  padding: 100px 20px;
  color: var(--ink-muted);
}
.empty-icon { font-size: 56px; margin-bottom: 12px; opacity: 0.4; }

.community-list { display: flex; flex-direction: column; gap: 16px; }

.community-card {
  display: flex;
  gap: 18px;
  background: var(--bg-card);
  backdrop-filter: blur(12px);
  border: 1px solid var(--frost-border);
  border-radius: 14px;
  padding: 20px 22px;
  transition: all 0.25s ease;
}
.community-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }

/* 左：评价者 */
.review-side {
  flex: 0 0 130px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
}
.review-avatar {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--accent-light) 0%, var(--accent) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}
.review-user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  word-break: break-all;
}
.review-time { font-size: 12px; color: var(--ink-muted); }

/* 右：影评内容 */
.review-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 10px; }

.review-movie {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: var(--frost);
  border: 1px solid var(--frost-border);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  width: fit-content;
  max-width: 100%;
}
.review-movie:hover { background: var(--frost-hover); border-color: var(--accent-border); }
.review-movie-poster {
  width: 40px;
  height: 56px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--bleed-a), var(--bleed-b));
  display: flex; align-items: center; justify-content: center;
  font-size: 18px;
}
.review-movie-info { min-width: 0; }
.review-movie-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.review-movie-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: var(--ink-muted);
  margin-top: 2px;
}

.review-stars { display: flex; align-items: center; gap: 8px; }
.stars { color: var(--accent); letter-spacing: 2px; font-size: 15px; }
.stars-num { font-size: 13px; color: var(--ink-muted); }

.review-content {
  font-size: 14px;
  line-height: 1.75;
  color: var(--ink-secondary);
  white-space: pre-wrap;
  margin: 0;
}

@media (max-width: 640px) {
  .community-card { flex-direction: column; gap: 12px; }
  .review-side {
    flex: none;
    flex-direction: row;
    text-align: left;
    align-items: center;
    gap: 10px;
  }
}
</style>
