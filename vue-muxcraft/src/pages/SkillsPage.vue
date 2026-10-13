<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from '../i18n/index'
import { getSkills } from '../api/content'

const { t } = useI18n()
const groups = ref([])
const loading = ref(true)
const err = ref('')

onMounted(async () => {
  try {
    groups.value = await getSkills()
  } catch (e) {
    err.value = e.message
  } finally {
    loading.value = false
  }
})
function stars(n) { return '★'.repeat(n || 0) + '☆'.repeat(5 - (n || 0)) }
const flat = () => groups.value.flatMap(g => g.skills || []).sort((a, b) => (a.sortOrder ?? 999) - (b.sortOrder ?? 999))
</script>

<style scoped>
.skill-icon.has-img { padding: 0; overflow: hidden; }
.skill-icon.has-img img { width: 100%; height: 100%; object-fit: cover; }
</style>

<template>
  <main class="page">
    <div class="page-head" v-reveal>
      <router-link to="/" class="back-link">{{ t('back') }}</router-link>
      <div class="eyebrow" style="margin-top:14px">{{ t('skills.eyebrow') }}</div>
      <h2 class="title">{{ t('skills.title') }}</h2>
      <p class="lead" style="margin-top:10px">{{ t('skills.lead') }}</p>
    </div>

    <p v-if="loading" class="muted" style="margin-top:20px">Loading…</p>
    <p v-else-if="err" class="muted" style="margin-top:20px">{{ err }}</p>
    <div v-else class="skills" v-reveal style="margin-top:20px">
      <div class="skill" v-for="s in flat()" :key="s.id">
        <div class="skill-icon" v-if="!s.iconUrl">{{ s.name?.[0]?.toUpperCase() }}</div>
        <div class="skill-icon has-img" v-else><img :src="s.iconUrl" :alt="s.name" /></div>
        <b>{{ s.name }}</b>
        <p class="muted">{{ s.description }}</p>
        <div class="stars">{{ stars(s.rating) }}</div>
      </div>
    </div>
  </main>
</template>
