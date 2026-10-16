<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from '../i18n/index'
import { getArticles } from '../api/content'
import { fmtDate } from '../utils/time'

const { t } = useI18n()
const records = ref([])
const loading = ref(true)
const err = ref('')

onMounted(async () => {
  try {
    const data = await getArticles({ page: 1, size: 50 })
    records.value = data.records || []
  } catch (e) { err.value = e.message }
  finally { loading.value = false }
})

function openLink(a) {
  if (a.slug) window.open(a.slug, '_blank', 'noopener')
}
</script>

<template>
  <main class="page">
    <div class="page-head" v-reveal>
      <router-link to="/" class="back-link">{{ t('back') }}</router-link>
      <div class="eyebrow" style="margin-top:14px">{{ t('blog.eyebrow') }}</div>
      <h2 class="title">{{ t('blog.title') }}</h2>
      <p class="lead" style="margin-top:10px">{{ t('blog.lead') }}</p>
    </div>
    <p v-if="loading" class="muted" style="margin-top:20px">Loading…</p>
    <p v-else-if="err" class="muted" style="margin-top:20px">{{ err }}</p>
    <div class="blog" v-else v-reveal style="margin-top:20px">
      <article class="article" v-for="a in records" :key="a.id"
        :class="{ clickable: !!a.slug }"
        @click="openLink(a)">
        <div class="text">
          <small>{{ fmtDate(a.publishedAt) }}</small>
          <h3>{{ a.title }}</h3>
          <p class="muted">{{ a.summary }}</p>
          <span v-if="a.slug" class="read">READ ↗</span>
        </div>
        <div v-if="a.coverUrl" class="cover"
          :style="{ backgroundImage: `url(${a.coverUrl})` }"></div>
      </article>
    </div>
  </main>
</template>

<style scoped>
.article {
  display: flex;
  align-items: stretch;
  gap: 20px;
  border: 1px solid var(--line);
  border-radius: 18px;
  background: rgba(255,255,255,.6);
  padding: 20px 22px;
  margin-bottom: 14px;
  transition: .25s;
  overflow: hidden;
}
.article.clickable { cursor: pointer; }
.article.clickable:hover { transform: translateY(-3px); box-shadow: var(--shadow); border-color: #b7dce8; }
.text { flex: 1; min-width: 0; }
.text small { color: var(--blue2); font-weight: 700; letter-spacing: 1px; }
.text h3 { margin: 6px 0 8px; font-size: 20px; }
.text p { margin: 0; font-size: 14px; line-height: 1.7; }
.read { display: inline-block; margin-top: 10px; font-size: 12px; font-weight: 700; color: var(--blue2); }
.cover {
  flex: 0 0 140px;
  border-radius: 12px;
  background-size: cover;
  background-position: center;
  opacity: .85;
}
@media (max-width: 640px) {
  .article { flex-direction: column; }
  .cover { flex: none; height: 120px; width: 100%; }
}
</style>
