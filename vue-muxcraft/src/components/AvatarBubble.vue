<script setup>
import { computed } from 'vue'

// 头像：有 avatarUrl 用图片；否则用昵称首字 + 渐变底色（对应 API 文档默认头像规则）
const props = defineProps({
  nickname: { type: String, default: '' },
  avatarUrl: { type: String, default: '' },
  size: { type: Number, default: 40 }
})

const palette = [
  'linear-gradient(135deg,#a9d8f5,#71b9df)',
  'linear-gradient(135deg,#9dddd7,#6ec6bd)',
  'linear-gradient(135deg,#c3b6f0,#9d8fd4)',
  'linear-gradient(135deg,#f5c6a9,#e0a478)',
  'linear-gradient(135deg,#f0a9c0,#d47fa0)',
  'linear-gradient(135deg,#b8e6a9,#8cc47a)'
]

const initial = computed(() => {
  const n = (props.nickname || '?').trim()
  return n ? n[0] : '?'
})

const bg = computed(() => {
  let h = 0
  for (const ch of props.nickname || '') h = (h * 31 + ch.charCodeAt(0)) >>> 0
  return palette[h % palette.length]
})
</script>

<template>
  <div
    class="avatar-bubble"
    :style="{ width: size + 'px', height: size + 'px', background: avatarUrl ? 'none' : bg, fontSize: size * 0.42 + 'px' }"
    :title="nickname"
  >
    <img v-if="avatarUrl" :src="avatarUrl" :alt="nickname" />
    <template v-else>{{ initial }}</template>
  </div>
</template>
