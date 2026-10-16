<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useI18n } from '../i18n/index'
import { getProjects } from '../api/content'

const { t } = useI18n()
const records = ref([])
const loading = ref(true)
const err = ref('')

const gridRef = ref(null)
const twoRows = ref(0)        // 前两行卡片的实测高度，写入 --proj-2rows
let rowObserver = null
let onResize = null

// 量出容器应有的高度：PC 端取前两行卡片高度，手机端只取一行（一行后才出滚动条）
function measureTwoRows() {
  const grid = gridRef.value
  if (!grid) return
  const cards = Array.from(grid.children)
  if (!cards.length) return
  const rows = []
  for (const c of cards) {
    const top = c.offsetTop
    const bottom = top + c.offsetHeight
    const last = rows[rows.length - 1]
    if (!last || Math.abs(top - last.top) > 2) rows.push({ top, bottom })
    else if (bottom > last.bottom) last.bottom = bottom
  }
  const baseTop = rows[0].top
  const lastBottom = rows[rows.length - 1].bottom
  const onMobile = window.matchMedia('(max-width: 768px)').matches
  const rowBox = onMobile
    ? (rows[0].bottom - baseTop)
    : (rows.length >= 2 ? rows[1].bottom : rows[0].bottom) - baseTop
  // 容器尾部还有 padding / 最后一张卡片的外边距，也要算进去，否则刚好满行时仍会多出一条细滚动条
  const trailing = Math.min(32, Math.max(0, grid.scrollHeight - (lastBottom - baseTop)))
  const next = Math.ceil(rowBox + trailing)
  if (next !== twoRows.value) twoRows.value = next
}

onMounted(async () => {
  try {
    const data = await getProjects({ page: 1, size: 50 })
    records.value = data.records || []
  } catch (e) {
    err.value = e.message
  } finally {
    loading.value = false
  }
  await nextTick()
  measureTwoRows()
  onResize = () => measureTwoRows()
  window.addEventListener('resize', onResize)
  if (typeof ResizeObserver !== 'undefined') {
    rowObserver = new ResizeObserver(() => measureTwoRows())
    if (gridRef.value) rowObserver.observe(gridRef.value)
  }
})

onUnmounted(() => {
  if (onResize) window.removeEventListener('resize', onResize)
  if (rowObserver) { rowObserver.disconnect(); rowObserver = null }
})

// 面包格 bento 排版：每行 12 列
// row0: 7+5  （大+次高）
// row1: 4+4+4（标准三列）
// row2: 6+6  （两个等宽大卡）
// row3: 4+4+4
const patterns = [
  ['p-big', 'p-tall', ''],
  ['', '', ''],
  ['p-wide', 'p-wide', ''],
  ['', '', ''],
]
function cls(i) {
  const row = Math.floor(i / 3) % patterns.length
  const col = i % 3
  return patterns[row][col] || ''
}
</script>

<template>
  <main class="page">
    <div class="page-head" v-reveal>
      <router-link to="/" class="back-link">{{ t('back') }}</router-link>
      <div class="eyebrow" style="margin-top:14px">{{ t('projects.eyebrow') }}</div>
      <h2 class="title">{{ t('projects.title') }}</h2>
      <p class="lead" style="margin-top:10px">{{ t('projects.lead') }}</p>
    </div>

    <p v-if="loading" class="muted" style="margin-top:20px">Loading…</p>
    <p v-else-if="err" class="muted" style="margin-top:20px">{{ err }}</p>
    <div v-else-if="records.length===0" class="panel" style="margin-top:20px"><p class="muted">—</p></div>

    <div
      class="projects"
      v-else
      ref="gridRef"
      v-reveal
      :style="{ marginTop: '20px', '--proj-2rows': (twoRows ? twoRows + 'px' : '0px') }"
    >
      <router-link
        v-for="(p,i) in records" :key="p.id"
        class="card"
        :class="cls(i)"
        :to="`/projects/${p.id}`"
      >
        <div class="feature" :style="p.coverUrl ? {background:`center/cover url(${p.coverUrl})`}:{}"></div>
        <span class="tag">{{ p.skills?.[0]?.name || 'PROJECT' }}</span>
        <h3>{{ p.name }}</h3>
        <p class="muted">{{ p.summary }}</p>
        <div class="bottom"><small class="muted">PROJECT / {{ String(p.id).padStart(3,'0') }}</small><b>↗</b></div>
      </router-link>
    </div>
  </main>
</template>

<style scoped>
.p-big  { grid-column: span 7 !important; min-height: 320px; }
.p-tall { grid-column: span 5 !important; min-height: 320px; }
.p-wide { grid-column: span 6 !important; min-height: 240px; }
.p-big .feature, .p-tall .feature, .p-wide .feature { height: 180px; }
@media (max-width: 900px) {
  .p-big, .p-tall, .p-wide { grid-column: span 6 !important; }
}
@media (max-width: 640px) {
  .p-big, .p-tall, .p-wide { grid-column: span 12 !important; }
}
</style>
