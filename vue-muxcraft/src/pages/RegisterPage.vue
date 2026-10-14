<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from '../i18n/index'
import AvatarBubble from '../components/AvatarBubble.vue'
import Turnstile from '../components/Turnstile.vue'

const router = useRouter()
const { t, state } = useI18n()

const userId = ref('')
const nickname = ref('')
const password = ref('')
const confirm = ref('')
const showPwd = ref(false)
const avatarFile = ref(null)
const avatarPreview = ref('')
const err = ref('')
const ok = ref('')
const loading = ref(false)
const fileInput = ref(null)
const turnstileRef = ref(null)

function pickAvatar() { fileInput.value?.click() }
function onFile(e) {
  const f = e.target.files?.[0]
  if (!f) return
  avatarFile.value = f
  // 用 ObjectURL 本地预览，不上传到 OSS
  if (avatarPreview.value) URL.revokeObjectURL(avatarPreview.value)
  avatarPreview.value = URL.createObjectURL(f)
  e.target.value = ''
}

async function submit() {
  err.value = ''; ok.value = ''
  if (userId.value.trim().length < 3 || userId.value.trim().length > 32) return (err.value = t('register.err.userid'))
  if (!nickname.value.trim() || nickname.value.trim().length > 32) return (err.value = t('register.err.nickname'))
  if (password.value.length < 8) return (err.value = t('register.err.password'))
  if (password.value !== confirm.value) return (err.value = t('register.err.confirm'))
  const cfToken = turnstileRef.value?.token || ''

  const fd = new FormData()
  fd.append('userId', userId.value.trim())
  fd.append('nickname', nickname.value.trim())
  fd.append('password', password.value)
  fd.append('cf-turnstile-response', cfToken)
  if (avatarFile.value) fd.append('avatar', avatarFile.value)

  loading.value = true
  try {
    const res = await fetch('/api/auth/register', { method: 'POST', credentials: 'include', body: fd })
    const json = await res.json()
    if (json.code !== 200) {
      err.value = json.message || 'Register failed'
      turnstileRef.value?.reset()
    } else {
      ok.value = t('register.ok')
      setTimeout(() => router.push({ path: '/', query: { login: '1' } }), 900)
    }
  } catch (e) {
    err.value = '网络错误：' + e.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="auth-wrap">
    <div class="blob b1" aria-hidden="true"></div>
    <div class="blob b2" aria-hidden="true"></div>
    <div>
      <router-link to="/" class="back-link" style="display:inline-block;margin-bottom:16px">{{ t('back')
        }}</router-link>
      <div class="auth-card auth-card-wide" style="position:relative">
        <router-link to="/" class="close-x" style="position:absolute;top:14px;right:16px;font-size:22px;text-decoration:none;color:var(--muted);">×</router-link>
        <div class="auth-head">
          <div>
            <div class="micro">REGISTER // MUXCRAFT</div>
            <h1>{{ t('register.title') }}</h1>
            <p class="muted">{{ t('register.desc') }}</p>
          </div>
          <div class="avatar-upload" @click="pickAvatar">
            <AvatarBubble :nickname="nickname || '?'" :avatar-url="avatarPreview" :size="72" />
            <input ref="fileInput" type="file" accept="image/*" hidden @change="onFile" />
            <small v-if="!avatarFile">{{ t('register.avatar') }} ↑</small>
            <small v-else style="color:var(--blue2)">已选择 · 点击重选</small>
          </div>
        </div>

        <p class="muted" style="font-size:12px;margin:0 0 18px">{{ t('register.avatarHint') }}</p>

        <div class="form-grid">
          <div class="field">
            <label>{{ t('register.userId') }}</label>
            <input v-model="userId" :placeholder="state.locale === 'zh' ? '请输入登录账号' : 'Enter login ID'" />
          </div>
          <div class="field">
            <label>{{ t('register.nickname') }}</label>
            <input v-model="nickname" :placeholder="state.locale === 'zh' ? '请输入昵称' : 'Enter nickname'" />
          </div>
          <div class="field">
            <label>{{ t('register.password') }}</label>
            <div class="pwd-wrap">
              <input v-model="password" :type="showPwd ? 'text' : 'password'" :placeholder="state.locale === 'zh' ? '请输入密码' : 'Enter password'" />
              <button type="button" class="eye" @click="showPwd = !showPwd" :tabindex="-1">
                <svg v-if="!showPwd" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8aa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8aa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
              </button>
            </div>
          </div>
          <div class="field">
            <label>{{ t('register.confirm') }}</label>
            <div class="pwd-wrap">
              <input v-model="confirm" :type="showPwd ? 'text' : 'password'" :placeholder="state.locale === 'zh' ? '请再次输入密码' : 'Confirm password'" @keyup.enter="submit" />
              <button type="button" class="eye" @click="showPwd = !showPwd" :tabindex="-1">
                <svg v-if="!showPwd" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8aa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8aa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
              </button>
            </div>
          </div>
        </div>

        <p class="form-err">{{ err }}</p>
        <p class="form-ok">{{ ok }}</p>
        <Turnstile ref="turnstileRef" />
        <button class="cta" style="width:100%" :disabled="loading" @click="submit">
          {{ loading ? '…' : t('register.submit') }}
        </button>
        <p class="switch">{{ t('register.have') }} <router-link :to="{ path: '/', query: { login: '1' } }">{{
          t('register.toLogin') }}</router-link></p>
      </div>
    </div>
  </main>
</template>

<style scoped>
.pwd-wrap { position: relative; display: flex; align-items: center; }
.pwd-wrap input { width: 100%; padding-right: 42px; }
.eye { position: absolute; right: 10px; top: calc(50% - 2px); transform: translateY(-50%); background: none; border: none; cursor: pointer; padding: 2px; display: flex; align-items: center; color: #8aa; }
.eye:hover { color: #567; }
.pwd-wrap input::-ms-reveal,
.pwd-wrap input::-ms-clear { display: none; }
.pwd-wrap input::-webkit-credentials-auto-fill-button { visibility: hidden; display: none; }
</style>
