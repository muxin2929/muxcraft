<script setup>
import { ref, watch, inject } from 'vue'
import { useUser, setSession, logout } from '../store/user'
import { toast } from '../composables/ui'
import { getAccessToken } from '../api/request'
import AvatarBubble from './AvatarBubble.vue'

const props = defineProps({ open: Boolean })
const emit = defineEmits(['close'])
const { state: uState } = useUser()
const openLogin = inject('openLogin')

const userId = ref('')
const nickname = ref('')
const avatarUrl = ref('')
const fileInput = ref(null)
const pendingFile = ref(null)
const saving = ref(false)

// 修改密码弹窗
const pwdOpen = ref(false)
const oldPwd = ref('')
const newPwd = ref('')
const confirmPwd = ref('')
const pwdSubmitting = ref(false)
const showOldPwd = ref(false)
const showNewPwd = ref(false)
const showConfirmPwd = ref(false)

watch(
  () => props.open,
  (v) => {
    if (v && uState.user) {
      userId.value = uState.user.userId || ''
      nickname.value = uState.user.nickname || ''
      avatarUrl.value = uState.user.avatarUrl || ''
      pendingFile.value = null
    }
    document.body.style.overflow = v ? 'hidden' : ''
  }
)

function pickAvatar() { fileInput.value?.click() }
function onPick(e) {
  const f = e.target.files?.[0]
  if (!f) return
  pendingFile.value = f
  if (avatarUrl.value) URL.revokeObjectURL(avatarUrl.value)
  avatarUrl.value = URL.createObjectURL(f)
  e.target.value = ''
}

async function save() {
  if (!nickname.value.trim()) { toast('昵称不能为空', 'error'); return }
  saving.value = true
  try {
    const fd = new FormData()
    fd.append('data', JSON.stringify({
      nickname: nickname.value.trim(),
      avatarUrl: uState.user.avatarUrl || ''
    }))
    if (pendingFile.value) fd.append('avatar', pendingFile.value)
    const res = await fetch('/api/user/profile', {
      method: 'PUT',
      headers: { Authorization: 'Bearer ' + (getAccessToken() || '') },
      credentials: 'include',
      body: fd
    })
    const json = await res.json()
    if (json.code !== 200) throw new Error(json.message || '保存失败')
    setSession({ token: getAccessToken(), user: { ...uState.user, ...json.data } })
    toast('账户信息已更新')
    emit('close')
  } catch (e) { toast('保存失败：' + e.message, 'error') }
  finally { saving.value = false }
}

function openPwd() {
  oldPwd.value = ''
  newPwd.value = ''
  confirmPwd.value = ''
  pwdOpen.value = true
}

async function submitPwd() {
  if (!oldPwd.value) { toast('请输入原密码', 'error'); return }
  if (!newPwd.value || newPwd.value.length < 8) { toast('新密码至少8位', 'error'); return }
  if (newPwd.value !== confirmPwd.value) { toast('两次新密码不一致', 'error'); return }
  pwdSubmitting.value = true
  try {
    const res = await fetch('/api/user/password', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + (getAccessToken() || '')
      },
      credentials: 'include',
      body: JSON.stringify({ oldPassword: oldPwd.value, newPassword: newPwd.value })
    })
    const json = await res.json()
    if (json.code !== 200) throw new Error(json.message || '修改失败')
    toast('密码修改成功，请重新登录')
    pwdOpen.value = false
    emit('close')
    logout()
    setTimeout(() => openLogin && openLogin(), 300)
  } catch (e) { toast(e.message, 'error') }
  finally { pwdSubmitting.value = false }
}
</script>

<template>
  <div v-if="open" class="acct-mask" @click.self="emit('close')">
    <div class="acct-box">
      <button class="close-x" @click="emit('close')">×</button>
      <div class="micro">ACCOUNT</div>
      <h3>修改账户</h3>

      <div class="acct-avatar" @click="pickAvatar">
        <AvatarBubble :nickname="nickname || '?'" :avatar-url="avatarUrl" :size="80" />
        <div class="acct-avatar-mask">更换</div>
        <input ref="fileInput" type="file" accept="image/*" hidden @change="onPick" />
      </div>

      <div class="field">
        <label>用户 ID（不可修改）</label>
        <input :value="userId" disabled />
      </div>
      <div class="field">
        <label>昵称</label>
        <input v-model="nickname" maxlength="32" />
      </div>

      <div class="acct-actions">
        <button class="btn outline left" @click="openPwd">修改密码</button>
        <button class="btn outline" @click="emit('close')">取消</button>
        <button class="btn cta" :disabled="saving" @click="save">{{ saving ? '保存中…' : '保存' }}</button>
      </div>
    </div>
  </div>

  <!-- 修改密码弹窗 -->
  <div v-if="pwdOpen" class="acct-mask" @click.self="pwdOpen = false">
    <div class="acct-box" style="width:min(380px,92vw)">
      <button class="close-x" @click="pwdOpen = false">×</button>
      <div class="micro">PASSWORD</div>
      <h3>修改密码</h3>

      <div class="field">
        <label>原密码</label>
        <div class="pwd-wrap">
          <input :type="showOldPwd ? 'text' : 'password'" v-model="oldPwd" placeholder="请输入原密码" />
          <button type="button" class="eye" @click="showOldPwd = !showOldPwd" :tabindex="-1">
            <svg v-if="!showOldPwd" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8aa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8aa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
          </button>
        </div>
      </div>
      <div class="field">
        <label>新密码（至少8位）</label>
        <div class="pwd-wrap">
          <input :type="showNewPwd ? 'text' : 'password'" v-model="newPwd" placeholder="请输入新密码" />
          <button type="button" class="eye" @click="showNewPwd = !showNewPwd" :tabindex="-1">
            <svg v-if="!showNewPwd" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8aa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8aa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
          </button>
        </div>
      </div>
      <div class="field">
        <label>确认新密码</label>
        <div class="pwd-wrap">
          <input :type="showConfirmPwd ? 'text' : 'password'" v-model="confirmPwd" placeholder="再次输入新密码" @keyup.enter="submitPwd" />
          <button type="button" class="eye" @click="showConfirmPwd = !showConfirmPwd" :tabindex="-1">
            <svg v-if="!showConfirmPwd" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8aa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8aa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
          </button>
        </div>
      </div>

      <div class="acct-actions">
        <button class="btn outline" @click="pwdOpen = false">取消</button>
        <button class="btn cta" :disabled="pwdSubmitting" @click="submitPwd">{{ pwdSubmitting ? '提交中…' : '确认修改' }}</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.acct-mask {
  position: fixed; inset: 0; background: rgba(245,247,245,.6);
  backdrop-filter: blur(6px); z-index: 400;
  display: flex; align-items: flex-start; justify-content: center;
  overflow-y: auto; padding: 40px 16px;
}
.acct-box {
  width: min(420px, 94vw); background: #faf9f5;
  border: 1px solid #e0ebef; border-radius: 20px; padding: 28px;
  box-shadow: 0 24px 70px rgba(40,75,90,.18); position: relative;
}
.close-x {
  position: absolute; top: 14px; right: 16px; background: none; border: none;
  font-size: 22px; cursor: pointer; color: var(--muted);
}
.acct-avatar {
  position: relative; width: 80px; height: 80px; margin: 10px auto 18px;
  border-radius: 50%; cursor: pointer; overflow: hidden;
}
.acct-avatar-mask {
  position: absolute; inset: 0; background: rgba(36,52,61,.55); color: #fff;
  font-size: 12px; display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: .2s;
}
.acct-avatar:hover .acct-avatar-mask { opacity: 1; }
.field { margin-bottom: 12px; }
.field label { display: block; font-size: 12px; color: var(--muted); margin-bottom: 6px; }
.field input {
  width: 100%; padding: 11px 14px; border: 1px solid var(--line);
  border-radius: 12px; background: #fff; font: inherit;
}
.field input:disabled { background: #eef2f3; color: var(--muted); }
.pwd-wrap { position: relative; display: flex; align-items: center; }
.pwd-wrap input { width: 100%; padding-right: 42px; }
.eye { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; padding: 2px; display: flex; align-items: center; color: #8aa; }
.eye:hover { color: #567; }
.pwd-wrap input::-ms-reveal, .pwd-wrap input::-ms-clear { display: none; }
.acct-actions { margin-top: 18px; display: flex; gap: 10px; justify-content: flex-end; }
.btn {
  padding: 11px 18px; border-radius: 12px; font-weight: 700;
  font-size: 13px; cursor: pointer; transition: .2s; white-space: nowrap;
}
.btn.cta {
  background: var(--ink); color: #fff; border: 1px solid var(--ink);
  box-shadow: 0 12px 28px rgba(36,52,61,.16);
}
.btn.outline {
  background: #fff; color: var(--ink); border: 1px solid var(--line);
}
.btn.left { margin-right: auto; }
.btn:hover { transform: translateY(-1px); }
.btn:disabled { opacity: .6; cursor: not-allowed; transform: none; }
@media (max-width: 600px) {
  .acct-actions { flex-wrap: nowrap; }
  .btn { width: auto !important; flex-shrink: 0; padding: 10px 14px; font-size: 12px; }
}
</style>