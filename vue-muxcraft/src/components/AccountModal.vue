<script setup>
import { ref, watch } from 'vue'
import { useUser, setSession } from '../store/user'
import { toast } from '../composables/ui'
import AvatarBubble from './AvatarBubble.vue'

const props = defineProps({ open: Boolean })
const emit = defineEmits(['close'])
const { state: uState } = useUser()

const userId = ref('')
const nickname = ref('')
const avatarUrl = ref('')
const fileInput = ref(null)
const pendingFile = ref(null)
const saving = ref(false)

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
      headers: { Authorization: 'Bearer ' + (localStorage.getItem('mux-token') || '') },
      body: fd
    })
    const json = await res.json()
    if (json.code !== 200) throw new Error(json.message || '保存失败')
    // 同步更新本地用户信息
    setSession({ token: localStorage.getItem('mux-token'), user: { ...uState.user, ...json.data } })
    toast('账户信息已更新')
    emit('close')
  } catch (e) { toast('保存失败：' + e.message, 'error') }
  finally { saving.value = false }
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
        <button class="outline" @click="emit('close')">取消</button>
        <button class="cta" :disabled="saving" @click="save">{{ saving ? '保存中…' : '保存' }}</button>
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
.acct-actions { margin-top: 18px; display: flex; gap: 10px; justify-content: flex-end; }
</style>
