<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  sitekey: { type: String, default: '0x4AAAAAAFWKgStSkuI9yGFY' },
  invisible: { type: Boolean, default: false }
})
const emit = defineEmits(['verified'])

const box = ref(null)
const token = ref('')
let widgetId = null
let timer = null

function render() {
  if (!window.turnstile || !box.value) return
  if (widgetId !== null) return
  widgetId = window.turnstile.render(box.value, {
    sitekey: props.sitekey,
    size: props.invisible ? 'invisible' : 'flexible',
    callback: (t) => { token.value = t; emit('verified', t) },
    'expired-callback': () => { token.value = ''; execute() },
    'error-callback': () => { token.value = '' }
  })
  execute()
}

function execute() {
  if (widgetId !== null && window.turnstile && props.invisible) {
    try { window.turnstile.execute(widgetId) } catch {}
  }
}

onMounted(() => {
  timer = setInterval(() => {
    if (window.turnstile) { render(); clearInterval(timer) }
  }, 100)
})

onBeforeUnmount(() => {
  clearInterval(timer)
  if (widgetId !== null && window.turnstile) { try { window.turnstile.remove(widgetId) } catch {} }
})

function getToken() {
  const input = box.value?.querySelector('input[name="cf-turnstile-response"]')
  return input?.value || ''
}

defineExpose({
  get token() { return getToken() },
  reset() {
    if (widgetId !== null && window.turnstile) { try { window.turnstile.reset(widgetId) } catch {} }
  }
})
</script>

<template>
  <div ref="box" style="width:100%;margin-bottom:16px;text-align:center;"></div>
</template>
