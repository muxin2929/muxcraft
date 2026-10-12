<script setup>
import { useToasts } from '../composables/ui'
const list = useToasts()
</script>

<template>
  <Teleport to="body">
    <div class="toast-wrap">
      <transition-group name="toast">
        <div v-for="t in list" :key="t.id" class="toast" :class="t.type">
          <span class="dot">{{ t.type === 'success' ? '✓' : '!' }}</span>
          <span>{{ t.message }}</span>
        </div>
      </transition-group>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-wrap {
  position: fixed; top: 90px; left: 50%; transform: translateX(-50%);
  z-index: 500; display: flex; flex-direction: column; gap: 8px; align-items: center;
  pointer-events: none;
}
.toast {
  display: flex; align-items: center; gap: 8px;
  background: #faf9f5; border: 1px solid #d8e5e8;
  padding: 10px 18px; border-radius: 999px;
  font-size: 13px; color: #24343d;
  box-shadow: 0 10px 30px rgba(40,75,90,.12);
}
.toast .dot {
  width: 18px; height: 18px; border-radius: 50%;
  display: grid; place-items: center;
  background: #9dddd7; color: #24343d; font-size: 11px; font-weight: 700;
}
.toast.error { border-color: #ecc7c2; }
.toast.error .dot { background: #f0b4ad; color: #7a2e28; }
.toast-enter-active, .toast-leave-active { transition: all .25s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateY(-8px); }
</style>
