<script setup>
import { inject } from 'vue'
import { useI18n } from '../i18n/index'
const { t } = useI18n()
const site = inject('site', { value: {} })
</script>

<template>
  <footer class="site-footer">
    <div class="footer-watermark" aria-hidden="true">MUX</div>
    <div class="footer-grid page" style="padding:0">
      <div class="footer-brand">
        <div class="fb-name">{{ site.siteTitle || 'MUXCRAFT' }}</div>
        <p>{{ site.siteDescription || t('footer.tagline') }}</p>
      </div>
      <div class="footer-col">
        <h4>{{ t('footer.explore') }}</h4>
        <router-link to="/profile">{{ t('explore.01') }}</router-link>
        <router-link to="/projects">{{ t('explore.04') }}</router-link>
        <router-link to="/blog">{{ t('explore.06') }}</router-link>
        <router-link to="/guestbook">{{ t('explore.07') }}</router-link>
      </div>
      <div class="footer-col">
        <h4>{{ t('footer.system') }}</h4>
        <router-link to="/contact">{{ t('explore.08') }}</router-link>
        <router-link to="/about">{{ t('menu.about') }}</router-link>
      </div>
    </div>
    <div class="footer-bottom page" style="padding:0">
      <span>MUXCRAFT · © 2026</span>
      <span>{{ t('footer.rights') }} · {{ t('footer.made') }}</span>
    </div>
  </footer>
</template>
