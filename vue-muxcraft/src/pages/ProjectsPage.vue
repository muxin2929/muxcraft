<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from '../i18n/index'
import { getProjects } from '../api/content'

const { t } = useI18n()
const records = ref([])
const loading = ref(true)
const err = ref('')

onMounted(async () => {
  try {
    const data = await getProjects({ page: 1, size: 9 })
    records.value = data.records || []
  } catch (e) {
    err.value = e.message
  } finally {
    loading.value = false
  }
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

    <div class="projects" v-else v-reveal style="margin-top:20px">
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
