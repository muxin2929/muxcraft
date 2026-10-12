<script setup>
import { useI18n } from '../i18n/index'
const { t } = useI18n()
</script>

<template>
  <main class="about-page">
    <!-- 背景装饰层 -->
    <div class="about-bg" aria-hidden="true">
      <div class="ab-blob ab1"></div>
      <div class="ab-blob ab2"></div>
      <div class="ab-blob ab3"></div>
      <div class="ab-ring r1"></div>
      <div class="ab-ring r2"></div>
      <div class="ab-grid"></div>
    </div>

    <div class="page about-inner" v-reveal>
      <router-link to="/" class="back-link">{{ t('back') }}</router-link>

      <div class="about-card">
        <div class="eyebrow">{{ t('about.eyebrow') }}</div>
        <h2 class="title">{{ t('about.title') }}</h2>

        <div class="ab-divider"></div>

        <div class="ab-row">
          <span class="ab-label">{{ t('about.devLabel') }}</span>
          <span class="ab-value">{{ t('about.devName') }}</span>
        </div>
        <div class="ab-row">
          <span class="ab-label">{{ t('about.contactLabel') }}</span>
          <a class="ab-value ab-link" href="mailto:m18870756126@163.com">m18870756126@163.com</a>
        </div>

        <div class="ab-note">
          <span class="ab-dot"></span>
          {{ t('about.note') }}
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.about-page {
  min-height: 100vh;
  position: relative;
  padding: 130px 0 80px;
  overflow: hidden;
}

.about-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.ab-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: .6;
}

.ab1 {
  width: 380px;
  height: 380px;
  background: radial-gradient(circle, rgba(169, 216, 245, .6), transparent 70%);
  top: -80px;
  left: -100px;
  animation: float1 12s ease-in-out infinite;
}

.ab2 {
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, rgba(157, 221, 215, .55), transparent 70%);
  bottom: -60px;
  right: -80px;
  animation: float2 15s ease-in-out infinite;
}

.ab3 {
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(195, 182, 240, .4), transparent 70%);
  top: 40%;
  left: 55%;
  animation: float1 18s ease-in-out infinite reverse;
}

@keyframes float1 {

  0%,
  100% {
    transform: translate(0, 0)
  }

  50% {
    transform: translate(30px, -30px)
  }
}

@keyframes float2 {

  0%,
  100% {
    transform: translate(0, 0)
  }

  50% {
    transform: translate(-25px, 25px)
  }
}

.ab-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px dashed rgba(113, 185, 223, .3);
}

.r1 {
  width: 420px;
  height: 420px;
  right: -120px;
  top: 10%;
  animation: spin 35s linear infinite;
}

.r2 {
  width: 260px;
  height: 260px;
  left: -80px;
  bottom: 8%;
  border-color: rgba(157, 221, 215, .4);
  animation: spin 25s linear infinite reverse;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.ab-grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(113, 185, 223, .06) 1px, transparent 1px), linear-gradient(90deg, rgba(113, 185, 223, .06) 1px, transparent 1px);
  background-size: 44px 44px;
  mask-image: radial-gradient(ellipse at center, black 30%, transparent 75%);
  -webkit-mask-image: radial-gradient(ellipse at center, black 30%, transparent 75%);
}

.about-inner {
  position: relative;
  z-index: 1;
}

.about-card {
  margin-top: 28px;
  max-width: 560px;
  background: rgba(250, 249, 245, .82);
  backdrop-filter: blur(10px);
  border: 1px solid var(--line);
  border-radius: 24px;
  padding: 44px 40px;
  box-shadow: var(--shadow);
}

.ab-divider {
  height: 1px;
  background: linear-gradient(90deg, var(--line), transparent);
  margin: 22px 0;
}

.ab-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px dashed rgba(216, 229, 232, .7);
}

.ab-label {
  font-size: 11px;
  letter-spacing: .2em;
  color: var(--muted);
}

.ab-value {
  font-size: 18px;
  font-weight: 700;
}

.ab-link {
  color: var(--blue2);
  cursor: pointer;
}

.ab-link:hover {
  text-decoration: underline;
}

.ab-note {
  margin-top: 26px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.7;
}

.ab-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--blue2);
  box-shadow: 0 0 0 5px rgba(113, 185, 223, .15);
  flex: none;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  50% {
    box-shadow: 0 0 0 9px rgba(113, 185, 223, .08);
  }
}

@media (max-width: 560px) {
  .about-card {
    padding: 30px 22px;
  }

  .ab-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
