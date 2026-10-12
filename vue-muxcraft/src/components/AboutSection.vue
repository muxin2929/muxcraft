<script setup>
import { onMounted, ref } from 'vue'
import { useI18n } from '../i18n/index'
const { t } = useI18n()

// 对应 API 6.1 GET /api/site 的 stats；后端未就绪时用同形 mock
const stats = ref({ projects: 12, experiences: 8, skills: 17, articles: 24 })
onMounted(async () => {
  try {
    const res = await fetch('/api/site')
    const j = await res.json()
    if (j?.code === 200) stats.value = j.data.stats
  } catch (e) { /* mock 保留 */ }
})
</script>

<template>
  <section id="about" class="about" v-reveal>
    <div class="deco-ring" aria-hidden="true"></div>
    <div class="section-head">
      <div>
        <div class="eyebrow">{{ t('about.eyebrow') }}</div>
        <h2 class="title">{{ t('about.title') }}</h2>
      </div>
      <p class="lead">{{ t('about.lead') }}</p>
    </div>
    <div class="about-grid">
      <div class="panel">
        <div class="micro">{{ t('about.identity.micro') }}</div>
        <h3 style="font-size:34px;margin:10px 0">MUX + CRAFT</h3>
        <p class="muted">{{ t('about.identity.desc') }}</p>
      </div>
      <div class="stats">
        <div class="stat"><b>{{ stats.projects }}</b><small>{{ t('stat.projects') }}</small></div>
        <div class="stat"><b>{{ String(stats.experiences).padStart(2, '0') }}</b><small>{{ t('stat.experiences') }}</small></div>
        <div class="stat"><b>{{ String(stats.skills).padStart(2, '0') }}</b><small>{{ t('stat.skills') }}</small></div>
        <div class="stat"><b>{{ stats.articles }}</b><small>{{ t('stat.articles') }}</small></div>
      </div>
    </div>
  </section>
</template>
