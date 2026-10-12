<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from '../i18n/index'
import { getAchievements } from '../api/content'

const { t } = useI18n()
const records = ref([])
const loading = ref(true)
const err = ref('')

// 卡片配色（按索引轮换）：背景色 + 装饰圆点/纹理
const themes = [
  { bg: 'linear-gradient(135deg,#eaf6f4,#dcecf7)', dot: '#9dddd7' },
  { bg: 'linear-gradient(135deg,#eef3fb,#f4ecf7)', dot: '#a9d8f5' },
  { bg: 'linear-gradient(135deg,#fdf4e7,#e8f4ec)', dot: '#f0c987' },
  { bg: 'linear-gradient(135deg,#e6f4f1,#fce8ef)', dot: '#71b9df' },
]

onMounted(async () => {
  try {
    const list = await getAchievements()
    records.value = [...(list || [])].sort((a, b) =>
      String(a.year || '').localeCompare(String(b.year || ''))
    )
  } catch (e) { err.value = e.message }
  finally { loading.value = false }
})
</script>

<template>
  <main class="page">
    <div class="page-head" v-reveal>
      <router-link to="/" class="back-link">{{ t('back') }}</router-link>
      <div class="eyebrow" style="margin-top:14px">{{ t('achievements.eyebrow') }}</div>
      <h2 class="title">{{ t('achievements.title') }}</h2>
      <p class="lead" style="margin-top:10px">{{ t('achievements.lead') }}</p>
    </div>
    <p v-if="loading" class="muted" style="margin-top:20px">Loading…</p>
    <p v-else-if="err" class="muted" style="margin-top:20px">{{ err }}</p>

    <div v-else class="rail" v-reveal>
      <div class="line"></div>
      <div class="h-scroll">
        <div class="item" v-for="(a, i) in records" :key="a.id">
          <div class="dot"></div>
          <div class="year">{{ a.year }}</div>
          <div class="card" :style="{ background: themes[i % themes.length].bg }">
            <span class="blob" :style="{ background: themes[i % themes.length].dot }"></span>
            <span class="ghost">{{ a.year }}</span>
            <h3>{{ a.title }}</h3>
            <p v-if="a.description" class="muted">{{ a.description }}</p>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.rail { position: relative; margin-top: 40px; padding: 10px 2px 30px; }
.line {
  position: absolute;
  left: 0; right: 0; top: 22px;
  height: 2px;
  background: linear-gradient(90deg, var(--blue), var(--cyan));
  opacity: .5;
}
.h-scroll {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  padding: 0 2px 10px;
}
.h-scroll::-webkit-scrollbar { height: 8px; }
.h-scroll::-webkit-scrollbar-thumb { background: var(--line); border-radius: 4px; }
.item {
  flex: 0 0 260px;
  scroll-snap-align: start;
  position: relative;
  padding-top: 40px;
}
.dot {
  position: absolute;
  top: 15px; left: 0;
  width: 14px; height: 14px;
  border-radius: 50%;
  background: var(--paper);
  border: 3px solid var(--blue2);
}
.year {
  position: absolute;
  top: 0; left: 0;
  font-size: 13px;
  font-weight: 800;
  color: var(--blue2);
  letter-spacing: 2px;
}
.card {
  position: relative;
  border: 1px solid var(--line);
  border-radius: 18px;
  padding: 22px 20px;
  min-height: 160px;
  overflow: hidden;
  transition: .25s;
}
.card:hover { transform: translateY(-3px); box-shadow: var(--shadow); }
/* 右上角装饰圆 */
.blob {
  position: absolute;
  right: -30px; top: -30px;
  width: 110px; height: 110px;
  border-radius: 50%;
  opacity: .35;
  filter: blur(2px);
}
/* 巨大水印年份 */
.ghost {
  position: absolute;
  right: 10px; bottom: -10px;
  font-size: 64px;
  font-weight: 900;
  color: rgba(36,52,61,.07);
  letter-spacing: -2px;
  line-height: 1;
  pointer-events: none;
}
.card h3 { position: relative; margin: 0 0 10px; font-size: 19px; }
.card p { position: relative; margin: 0; font-size: 14px; line-height: 1.7; }

@media (max-width: 640px) {
  .item { flex: 0 0 80%; }
}
</style>
