<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useI18n } from '../i18n/index'
import { useUser } from '../store/user'
import AccountModal from './AccountModal.vue'

const props = defineProps({ open: Boolean })
const emit = defineEmits(['close', 'open-login'])
const { t } = useI18n()
const { state: uState, confirmLogout } = useUser()

const accountOpen = ref(false)

async function doLogout() {
  const ok = await confirmLogout()
  if (ok) emit('close')
}

function onKey(e) {
  if (e.key === 'Escape') { emit('close'); accountOpen.value = false }
}
onMounted(() => window.addEventListener('keydown', onKey))
onUnmounted(() => window.removeEventListener('keydown', onKey))

// 抽屉打开时锁定背景滚动，避免主页跟着滚、卡顿
watch(
  () => props.open,
  (v) => { document.body.style.overflow = v ? 'hidden' : '' }
)
</script>

<template>
  <div class="overlay" :class="{ open }" @click.self="emit('close')">
    <aside class="drawer">
      <button class="close-x" aria-label="close" @click="emit('close')">×</button>
      <div class="micro">SYSTEM MENU</div>
      <h2>{{ t('menu.title') }}</h2>
      <small>{{ t('menu.sub') }}</small>

      <nav class="navlist">
        <div class="drawer-group">{{ t('menu.group.explore') }}</div>
        <router-link to="/" @click="emit('close')">{{ t('nav.home') }}</router-link>
        <router-link to="/profile" @click="emit('close')">{{ t('explore.01') }}</router-link>
        <router-link to="/experience" @click="emit('close')">{{ t('explore.02') }}</router-link>
        <router-link to="/skills" @click="emit('close')">{{ t('explore.03') }}</router-link>
        <router-link to="/projects" @click="emit('close')">{{ t('explore.04') }}</router-link>
        <router-link to="/achievements" @click="emit('close')">{{ t('explore.05') }}</router-link>
        <router-link to="/blog" @click="emit('close')">{{ t('explore.06') }}</router-link>
        <router-link to="/guestbook" @click="emit('close')">{{ t('explore.07') }}</router-link>

        <div class="drawer-group">{{ t('menu.group.system') }}</div>
        <router-link to="/contact" @click="emit('close')">{{ t('menu.suggestion') }}</router-link>
        <router-link to="/about" @click="emit('close')">{{ t('menu.about') }}</router-link>
        <router-link v-if="uState.user && uState.user.role === 'ADMIN'" to="/admin" @click="emit('close')">
          {{ t('menu.settings') }}
        </router-link>

        <div class="drawer-group">{{ t('menu.group.account') }}</div>
        <template v-if="uState.user">
          <a @click="accountOpen = true">{{ t('menu.accountEdit') }}</a>
          <a style="color:#c0574f" @click="doLogout">{{ t('menu.logout') }}</a>
        </template>
        <a v-else @click="emit('close'); emit('open-login')">{{ t('menu.login') }}</a>
      </nav>
    </aside>
  </div>

  <AccountModal :open="accountOpen" @close="accountOpen = false" />
</template>
