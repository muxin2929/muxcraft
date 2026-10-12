<script setup>
import { ref, onMounted, onBeforeUnmount, inject } from 'vue'
import { useI18n } from '../i18n/index'
import { useUser } from '../store/user'
import AvatarBubble from './AvatarBubble.vue'

const emit = defineEmits(['open-menu', 'open-login'])
const { state, t, toggleLocale } = useI18n()
const { state: uState } = useUser()
const openLogin = inject('openLogin', () => {})

const scrolled = ref(false)
let raf = 0
function onScroll() {
  if (raf) return
  raf = requestAnimationFrame(() => {
    scrolled.value = window.scrollY > 24
    raf = 0
  })
}
onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
  if (raf) cancelAnimationFrame(raf)
})
</script>

<template>
  <header class="topbar" :class="{ scrolled }">
    <router-link to="/" class="brand">MUXCRAFT</router-link>
    <div class="actions">
      <span class="status">{{ t('status') }}</span>

      <button class="lang-btn" :title="state.locale === 'en' ? '切换到中文' : 'Switch to English'" @click="toggleLocale">
        {{ state.locale === 'en' ? '中' : 'EN' }}
      </button>

      <button v-if="!uState.user" class="iconbtn" @click="openLogin">{{ t('login') }}</button>
      <div v-else class="avatar-chip" @click="openLogin" :title="uState.user.nickname">
        <AvatarBubble :nickname="uState.user.nickname" :avatar-url="uState.user.avatarUrl" :size="30" />
        <span class="name">{{ uState.user.nickname }}</span>
      </div>

      <button class="menu-btn" aria-label="menu" @click="emit('open-menu')"><span></span></button>
    </div>
  </header>
</template>
