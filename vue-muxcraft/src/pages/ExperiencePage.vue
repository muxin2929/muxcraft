<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useI18n } from '../i18n/index'
import { getExperiences } from '../api/content'

const { t } = useI18n()
const records = ref([])
const loading = ref(true)
const err = ref('')
const scrollRef = ref(null)
let isDown = false, startX = 0, scrollLeft = 0
let wheelBound = false

onMounted(async () => {
  try {
    const data = await getExperiences({ page: 1, size: 50 })
    records.value = (data.records || []).sort((a, b) => {
      const da = a.endDate || '9999-12-31'
      const db = b.endDate || '9999-12-31'
      return db.localeCompare(da)
    })
  } catch (e) {
    err.value = e.message
  } finally {
    loading.value = false
  }
})

// 等 DOM 渲染完再绑定 wheel
watch(loading, async (v) => {
  if (v === false && !wheelBound) {
    await nextTick()
    if (scrollRef.value) {
      scrollRef.value.addEventListener('wheel', onWheel, { passive: false })
      wheelBound = true
    }
  }
})

onBeforeUnmount(() => {
  if (scrollRef.value) {
    scrollRef.value.removeEventListener('wheel', onWheel)
  }
})

function onMouseDown(e) {
  const el = scrollRef.value
  if (!el) return
  isDown = true
  el.classList.add('dragging')
  startX = e.pageX - el.offsetLeft
  scrollLeft = el.scrollLeft
}
function onMouseLeave() { isDown = false; scrollRef.value?.classList.remove('dragging') }
function onMouseUp() { isDown = false; scrollRef.value?.classList.remove('dragging') }
function onMouseMove(e) {
  const el = scrollRef.value
  if (!el || !isDown) return
  e.preventDefault()
  const x = e.pageX - el.offsetLeft
  el.scrollLeft = scrollLeft - (x - startX) * 1.5
}
// 鼠标滚轮在卡片区域时转为水平滚动
function onWheel(e) {
  const el = scrollRef.value
  if (!el) return
  e.preventDefault()
  el.scrollLeft += e.deltaY
}
</script>

<template>
  <main class="page">
    <div class="page-head" v-reveal>
      <router-link to="/" class="back-link">{{ t('back') }}</router-link>
      <div class="eyebrow" style="margin-top:14px">{{ t('experience.eyebrow') }}</div>
      <h2 class="title">{{ t('experience.title') }}</h2>
      <p class="lead" style="margin-top:10px">{{ t('experience.lead') }}</p>
    </div>

    <p v-if="loading" class="muted" style="margin-top:20px">Loading…</p>
    <p v-else-if="err" class="muted" style="margin-top:20px">{{ err }}</p>
    <div v-else-if="records.length===0" class="panel" style="margin-top:20px"><p class="muted">—</p></div>

    <div ref="scrollRef" class="h-scroll" v-else v-reveal
      @mousedown="onMouseDown" @mouseleave="onMouseLeave" @mouseup="onMouseUp" @mousemove="onMouseMove">
      <a
        v-for="n in records" :key="n.id"
        class="h-card"
        :class="{ hascover: n.coverUrl }"
        :style="n.coverUrl ? { backgroundImage: `url(${n.coverUrl})` } : {}"
        :href="n.pageUrl || '#'" target="_blank" rel="noopener"
      >
        <div v-if="n.coverUrl" class="cover-mask"></div>
        <div class="card-content">
          <div class="eyebrow">{{ n.startDate }}<span v-if="n.endDate"> — {{ n.endDate }}</span></div>
          <h3>{{ n.title }}</h3>
          <p class="muted">{{ n.description }}</p>
          <span class="h-link">Open ↗</span>
        </div>
      </a>
    </div>
  </main>
</template>

<style scoped>
.h-scroll {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding: 10px 2px 20px;
  cursor: grab;
  user-select: none;
}
.h-scroll.dragging { cursor: grabbing; }
.h-scroll::-webkit-scrollbar { height: 8px }
.h-scroll::-webkit-scrollbar-thumb { background: var(--line); border-radius: 4px }
.h-card {
  flex: 0 0 calc((100% - 32px) / 3);
  min-width: 240px;
  padding: 22px;
  border: 1px solid var(--line);
  border-radius: 20px;
  background: rgba(255,255,255,.6);
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
  transition: .25s;
}
.cover-mask {
  position: absolute; inset: 0;
  background: linear-gradient(180deg, rgba(250,249,245,.82), rgba(250,249,245,.95));
}
.card-content { position: relative; z-index: 1; }
.h-card:hover { transform: translateY(-4px); box-shadow: var(--shadow); border-color: #b7dce8; }
.h-card h3 { margin: 8px 0; }
.h-link { display: inline-block; margin-top: 14px; font-size: 12px; color: var(--blue2); font-weight: 700; }
@media (max-width: 900px) { .h-card { flex: 0 0 calc((100% - 16px)/2); } }
@media (max-width: 560px) { .h-card { flex: 0 0 85%; } }
</style>
