<script setup>
import { useI18n } from '../i18n/index'
const { t } = useI18n()

const cards = [
  { n: '01 / PROFILE', to: '/profile', label: 'explore.01' },
  { n: '02 / EXPERIENCE', to: '/experience', label: 'explore.02' },
  { n: '03 / TECHNOLOGY', to: '/skills', label: 'explore.03' },
  { n: '04 / PROJECTS', to: '/projects', label: 'explore.04' },
  { n: '05 / ACHIEVEMENTS', to: '/achievements', label: 'explore.05' },
  { n: '06 / BLOG', to: '/blog', label: 'explore.06' },
  { n: '07 / GUESTBOOK', to: '/guestbook', label: 'explore.07' },
  { n: '08 / CONTACT', to: '/contact', label: 'explore.08' }
]
</script>

<template>
  <section v-reveal>
    <div class="section-head">
      <div>
        <div class="eyebrow">02 / EXPLORE</div>
        <h2 class="title">{{ t('explore.title') }}</h2>
      </div>
      <p class="lead">{{ t('explore.lead') }}</p>
    </div>
    <div class="explore">
      <router-link v-for="c in cards" :key="c.to" class="ex-card" :to="c.to" v-tilt>
        <span class="num">{{ c.n }}</span>
        <strong>{{ t(c.label) }}</strong>
        <span class="arrow">↗</span>
      </router-link>
    </div>
  </section>
</template>
