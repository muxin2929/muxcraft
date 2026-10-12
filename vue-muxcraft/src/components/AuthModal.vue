<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from '../i18n/index'
import { login } from '../api/auth'
import { useUser } from '../store/user'
import { toast } from '../composables/ui'

const props = defineProps({ open: Boolean })
const emit = defineEmits(['close'])
const router = useRouter()
const { t, state } = useI18n()
const { setSession, state: uState, confirmLogout } = useUser()

async function doLogout() {
  await confirmLogout()
  emit('close')
}

const userId = ref('')
const password = ref('')
const showPwd = ref(false)
const err = ref('')
const ok = ref('')
const loading = ref(false)

watch(
  () => props.open,
  (v) => {
    if (v) { err.value = ''; ok.value = ''; }
    document.body.style.overflow = v ? 'hidden' : ''
  }
)

async function submit() {
  err.value = ''; ok.value = ''
  if (!userId.value.trim() || !password.value) { err.value = t('login.fail'); return }
  loading.value = true
  try {
    const data = await login({ userId: userId.value.trim(), password: password.value })
    setSession(data)
    toast('登录成功，欢迎 ' + data.user.nickname)
    emit('close')
    setTimeout(() => location.reload(), 500)
  } catch (e) {
    err.value = e.message || t('login.fail')
    toast(err.value, 'error')
  } finally {
    loading.value = false
  }
}

function goRegister() {
  emit('close')
  router.push('/register')
}
</script>

<template>
  <div class="modal" :class="{ open }" @click.self="emit('close')">
    <div class="modal-box">
      <button class="close-x" aria-label="close" @click="emit('close')">×</button>
      <template v-if="!uState.user">
        <div class="micro">ACCESS MUXCRAFT</div>
        <h2>{{ t('login.title') }}</h2>
        <p class="muted">{{ t('login.desc') }}</p>
        <label>{{ t('login.id') }}</label>
        <input v-model="userId" :placeholder="state.locale === 'zh' ? '请输入账号' : 'Enter your account'" @keyup.enter="submit" />
        <label>{{ t('login.password') }}</label>
        <div class="pwd-wrap">
          <input v-model="password" :type="showPwd ? 'text' : 'password'" :placeholder="state.locale === 'zh' ? '请输入密码' : 'Enter your password'" @keyup.enter="submit" />
          <button type="button" class="eye" @click="showPwd = !showPwd" :tabindex="-1">
            <svg v-if="!showPwd" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8aa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8aa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
          </button>
        </div>
        <p class="form-err">{{ err }}</p>
        <button class="cta" style="width:100%" :disabled="loading" @click="submit">
          {{ loading ? '…' : t('login.submit') }}
        </button>
        <button class="outline" style="width:100%;margin-top:10px" @click="goRegister">{{ t('login.create') }}</button>
      </template>
      <template v-else>
        <div class="micro">SESSION</div>
        <h2>{{ uState.user.nickname }}</h2>
        <p class="form-ok">{{ ok || t('login.success') + uState.user.nickname }}</p>
        <button class="outline" style="width:100%" @click="doLogout">{{ t('logout') }}</button>
      </template>
    </div>
  </div>
</template>

<style scoped>
.pwd-wrap { position: relative; display: flex; align-items: center; }
.pwd-wrap input { width: 100%; padding-right: 42px; }
.eye { position: absolute; right: 10px; top: calc(50% - 2px); transform: translateY(-50%); background: none; border: none; cursor: pointer; padding: 2px; display: flex; align-items: center; color: #8aa; }
.eye:hover { color: #567; }
/* 隐藏浏览器原生密码显示按钮 */
.pwd-wrap input::-ms-reveal,
.pwd-wrap input::-ms-clear { display: none; }
.pwd-wrap input::-webkit-credentials-auto-fill-button { visibility: hidden; display: none; }
</style>
