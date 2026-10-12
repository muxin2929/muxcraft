<script setup>
import { ref, inject } from 'vue'
import { useI18n } from '../i18n/index'
import { useUser } from '../store/user'
import { postSuggestion } from '../api/content'
import { toast } from '../composables/ui'

const { t } = useI18n()
const { state: uState } = useUser()
const openLogin = inject('openLogin')
const title = ref('')
const content = ref('')
const loading = ref(false)
const err = ref('')

async function send() {
  if (!uState.user) return openLogin()
  err.value = ''; loading.value = true
  try {
    await postSuggestion({ title: title.value, content: content.value })
    toast('建议已发送')
    title.value = ''; content.value = ''
  } catch (e) { toast('发送失败：' + e.message, 'error'); err.value = e.message }
  finally { loading.value = false }
}
</script>

<template>
  <main class="page">
    <div class="page-head" v-reveal>
      <router-link to="/" class="back-link">{{ t('back') }}</router-link>
      <div class="eyebrow" style="margin-top:14px">{{ t('contact.eyebrow') }}</div>
      <h2 class="title">{{ t('contact.title') }}</h2>
      <p class="lead" style="margin-top:10px">{{ t('contact.lead') }}</p>
    </div>

    <div class="panel contact-panel" v-reveal style="margin-top:20px">
      <div class="contact-status">
        <span class="tag">{{ uState.user ? t('contact.loggedIn') : t('contact.loginRequired') }}</span>
      </div>
      <div class="field">
        <label>{{ t('contact.titleLbl') }}</label>
        <input v-model="title" :placeholder="t('contact.title')" />
      </div>
      <div class="field">
        <label>{{ t('contact.msgLbl') }}</label>
        <textarea v-model="content" :placeholder="t('contact.placeholder')" style="min-height:140px"></textarea>
      </div>
      <p class="form-err">{{ err }}</p>
      <button class="cta" :disabled="loading" @click="send">{{ t('contact.send') }}</button>
    </div>
  </main>
</template>
