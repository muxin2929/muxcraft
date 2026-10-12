<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useI18n } from '../i18n/index'

const { state } = useI18n()
const heroEl = ref(null)
const gridEl = ref(null)
const orbEl = ref(null)
const orb2El = ref(null)

// 顶部 micro 文字：打字机逐字打出 -> 停顿 -> 删除 -> 下一句
const phaseText = ref('')
let typeTimer = null
function startTyping() {
  const phrases = (state.locale === 'zh'
    ? ['正在初始化工坊 // 节点 MUX-01', '正在连接数字工坊', '工坊状态 // 在线', '欢迎来到木心工坊']
    : ['INITIALIZING WORKSHOP // NODE MUX-01', 'CONNECTING TO DIGITAL WORKSHOP', 'WORKSHOP STATUS // ONLINE', 'WELCOME TO MUXCRAFT'])
  let pi = 0, ci = 0, deleting = false
  function tick() {
    const cur = phrases[pi]
    if (!deleting) {
      ci++
      phaseText.value = cur.slice(0, ci)
      if (ci >= cur.length) { deleting = true; typeTimer = setTimeout(tick, 1600); return }
      typeTimer = setTimeout(tick, 55)
    } else {
      ci--
      phaseText.value = cur.slice(0, ci)
      if (ci <= 0) { deleting = false; pi = (pi + 1) % phrases.length; typeTimer = setTimeout(tick, 300); return }
      typeTimer = setTimeout(tick, 28)
    }
  }
  tick()
}
watch(() => state.locale, () => {
  clearTimeout(typeTimer)
  phaseText.value = ''
  startTyping()
})

// 跟随鼠标的轻微视差
let raf = 0
function onMove(e) {
  cancelAnimationFrame(raf)
  raf = requestAnimationFrame(() => {
    const r = heroEl.value.getBoundingClientRect()
    const x = (e.clientX - r.left) / r.width - 0.5
    const y = (e.clientY - r.top) / r.height - 0.5
    if (orbEl.value) orbEl.value.style.transform = `translate(${x * 30}px, ${y * 30}px)`
    if (orb2El.value) orb2El.value.style.transform = `translate(${x * -22}px, ${y * -22}px)`
    if (gridEl.value) gridEl.value.style.marginLeft = `${x * 10}px`
  })
}

function enter() {
  const el = document.getElementById('about')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

onMounted(() => {
  heroEl.value?.addEventListener('mousemove', onMove)
  startTyping()
})
onUnmounted(() => {
  heroEl.value?.removeEventListener('mousemove', onMove)
  clearTimeout(typeTimer)
  cancelAnimationFrame(raf)
})
</script>

<template>
  <section class="hero" ref="heroEl">
    <div class="grid-fx" ref="gridEl"></div>
    <div class="orb" ref="orbEl"></div>
    <div class="orb two" ref="orb2El"></div>
    <div class="blob b1" aria-hidden="true"></div>
    <div class="blob b2" aria-hidden="true"></div>
    <div class="hero-content" :class="{ zh: state.locale === 'zh' }">
      <div class="micro">{{ phaseText }}<span class="caret">▊</span></div>
      <h1 class="welcome">{{ state.locale === 'zh' ? '欢迎来到' : 'Welcome To' }}<span>{{ state.locale === 'zh' ? '木心工坊' : 'MuXCraft' }}</span></h1>
      <p class="sub">{{ state.locale === 'zh'
        ? '木心的数字工坊——一个收纳项目、经历、技术、想法与一切值得被做出来之物的鲜活空间。'
        : "Muxin's Digital Workshop — a living space for projects, experience, technology, ideas and things worth making." }}</p>
      <button class="cta" @click="enter">{{ state.locale === 'zh' ? '进入工坊 →' : 'ENTER WORKSHOP →' }}</button>
      <div class="scroll-hint">{{ state.locale === 'zh' ? '下滑探索 ↓' : 'SCROLL TO EXPLORE ↓' }}</div>
    </div>
  </section>
</template>

<style scoped>
@media (max-width: 640px) {
  .hero .cta { width: auto !important; padding: 9px 20px !important; font-size: 12px !important; }
}
</style>
