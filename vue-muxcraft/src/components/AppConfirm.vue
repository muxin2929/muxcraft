<script setup>
import { useConfirmState } from '../composables/ui'
const s = useConfirmState()
</script>

<template>
  <Teleport to="body">
    <div v-if="s.open" class="c-mask" @click.self="s.resolve(false)">
      <div class="c-box">
        <h3>{{ s.title }}</h3>
        <p class="muted">{{ s.message }}</p>
        <div class="c-ops">
          <button class="outline" @click="s.resolve(false)">取消</button>
          <button class="cta danger" @click="s.resolve(true)">确认</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.c-mask {
  position: fixed; inset: 0; z-index: 400;
  background: rgba(245,247,245,.55); backdrop-filter: blur(6px);
  display: grid; place-items: center;
}
.c-box {
  width: min(380px, 90vw);
  background: #faf9f5; border: 1px solid #e0ebef; border-radius: 20px;
  padding: 24px; box-shadow: 0 24px 70px rgba(40,75,90,.18);
}
.c-box h3 { margin: 0 0 8px; font-size: 17px; }
.c-box p { font-size: 13px; margin: 0 0 18px; }
.c-ops { display: flex; justify-content: flex-end; gap: 10px; }
.cta.danger { background: #c0574f; }
</style>
