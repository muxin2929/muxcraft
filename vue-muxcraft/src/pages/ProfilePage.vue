<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from '../i18n/index'
import { getProfile } from '../api/content'

const { t } = useI18n()
const profile = ref(null)
const loading = ref(true)
const err = ref('')

onMounted(async () => {
  try { profile.value = await getProfile() } catch (e) { err.value = e.message }
  finally { loading.value = false }
})
</script>

<template>
  <main class="page">
    <div class="page-head" v-reveal>
      <router-link to="/" class="back-link">{{ t('back') }}</router-link>
      <div class="eyebrow" style="margin-top:14px">{{ t('profile.eyebrow') }}</div>
      <h2 class="title">{{ profile?.name || t('profile.title') }}</h2>
      <p class="lead" style="margin-top:10px">{{ t('profile.lead') }}</p>
    </div>

    <p v-if="loading" class="muted" style="margin-top:20px">Loading…</p>
    <p v-else-if="err" class="muted" style="margin-top:20px">{{ err }}</p>
    <div v-else class="about-grid" v-reveal style="margin-top:30px">
      <div class="panel" style="min-height:330px">
        <div class="micro">PROFILE IMAGE // OSS</div>
        <div v-if="profile.photoUrl || profile.avatarUrl"
          :style="{ height:'190px', borderRadius:'18px', background: `center/cover no-repeat url(${profile.photoUrl || profile.avatarUrl})` }"></div>
        <div v-else
          style="height:190px;border-radius:18px;background:linear-gradient(135deg,#dceff7,#dff4ef);display:grid;place-items:center;color:#6d8792;font-weight:700">
          YOUR PHOTO</div>
        <h3 style="margin:18px 0 5px">{{ profile.name }}<span v-if="profile.location" style="font-size:13px;font-weight:400;color:var(--muted);margin-left:8px">· {{ profile.location }}</span></h3>
        <div class="muted">{{ profile.title }}</div>
        <p v-if="profile.shortIntro" style="margin:10px 0 0;color:var(--muted);font-size:14px;line-height:1.7">{{ profile.shortIntro }}</p>
        <div v-if="profile.githubUrl || profile.websiteUrl" style="margin-top:14px;display:flex;gap:10px;flex-wrap:wrap">
          <a v-if="profile.githubUrl" :href="profile.githubUrl" target="_blank" rel="noopener" class="outline" style="padding:8px 14px;font-size:12px">GitHub ↗</a>
          <a v-if="profile.websiteUrl" :href="profile.websiteUrl" target="_blank" rel="noopener" class="outline" style="padding:8px 14px;font-size:12px">Website ↗</a>
        </div>
      </div>
      <div class="panel">
        <div class="micro">{{ t('profile.aboutMicro') }}</div>
        <h3 style="font-size:28px;margin-top:10px">{{ t('profile.aboutTitle') }}</h3>
        <p class="muted">{{ profile.fullIntro || profile.shortIntro }}</p>
      </div>
    </div>
  </main>
</template>
