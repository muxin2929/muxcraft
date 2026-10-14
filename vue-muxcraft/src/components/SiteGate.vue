<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const SHOW = ref(false)
const box = ref(null)
let widgetId = null
let timer = null

function renderWidget() {
  if (!window.turnstile || !box.value) return
  if (widgetId !== null) {
    try { window.turnstile.reset(widgetId) } catch {}
    return
  }
  widgetId = window.turnstile.render(box.value, {
    sitekey: '0x4AAAAAAFWKgStSkuI9yGFY',
    callback: async (token) => {
      localStorage.setItem('mux-verified', String(Date.now()))
      try {
        await fetch('/api/auth/verify-turnstile', {
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ 'cf-turnstile-response': token })
        })
      } catch {}
      setTimeout(() => { SHOW.value = false }, 300)
    }
  })
}

function onRequireShow() {
  SHOW.value = true
  setTimeout(() => {
    if (window.turnstile) renderWidget()
    else {
      timer = setInterval(() => {
        if (window.turnstile) { renderWidget(); clearInterval(timer) }
      }, 100)
    }
  }, 100)
}

onMounted(() => {
  const ts = localStorage.getItem('mux-verified')
  if (ts && Date.now() - Number(ts) < 2 * 60 * 60 * 1000) {
    return
  }
  onRequireShow()
  window.addEventListener('sitegate:show', onRequireShow)
})

onBeforeUnmount(() => {
  clearInterval(timer)
  window.removeEventListener('sitegate:show', onRequireShow)
})
</script>

<template>
  <div v-if="SHOW" class="gate-mask">
    <div class="gate-box">
      <h1 class="gate-site">muxcraft.cfd</h1>
      <h2 class="gate-title">正在进行安全验证</h2>
      <p class="gate-desc">本网站使用安全服务防护恶意自动程序。在验证您不是自动程序期间，将显示此页面。</p>
      <div ref="box" class="gate-widget"></div>
    </div>
  </div>
</template>

<style scoped>
.gate-mask {
  position: fixed; inset: 0; z-index: 9999;
  background: #faf9f5; display: flex; align-items: flex-start; justify-content: center;
  padding-top: 12vh;
}
.gate-box { width: 100%; max-width: 640px; padding: 0 24px; }
.gate-site { font-size: 42px; font-weight: 800; color: #1a2a33; margin-bottom: 8px; }
.gate-title { font-size: 28px; font-weight: 700; color: #1a2a33; margin-bottom: 16px; }
.gate-desc { font-size: 15px; color: #556; line-height: 1.6; margin-bottom: 28px; }
.gate-widget { display: flex; justify-content: flex-start; }
</style>
