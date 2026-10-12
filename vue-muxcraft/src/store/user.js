import { reactive, watch } from 'vue'
import { confirmDialog, toast } from '../composables/ui'

// 当前登录用户的全局响应式状态。
const STORAGE_KEY = 'mux-user'
const TOKEN_KEY = 'mux-token'

const state = reactive({
  user: null,
  token: ''
})

try {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (raw) state.user = JSON.parse(raw)
  state.token = localStorage.getItem(TOKEN_KEY) || ''
} catch (e) { /* ignore */ }

watch(
  () => state.user,
  (u) => {
    if (u) localStorage.setItem(STORAGE_KEY, JSON.stringify(u))
    else localStorage.removeItem(STORAGE_KEY)
  },
  { deep: true }
)

watch(
  () => state.token,
  (tk) => {
    if (tk) localStorage.setItem(TOKEN_KEY, tk)
    else localStorage.removeItem(TOKEN_KEY)
  }
)

export function setSession({ token, user }) {
  state.token = token || ''
  state.user = user || null
}

export function logout() {
  state.token = ''
  state.user = null
}

// 带确认弹窗的退出登录
export async function confirmLogout() {
  const ok = await confirmDialog({
    title: '退出登录',
    message: '确定要退出当前账号吗？'
  })
  if (!ok) return false
  logout()
  toast('已退出登录')
  return true
}

export function useUser() {
  return { state, setSession, logout, confirmLogout }
}

// 监听 request 拦截器派发的登出事件（避免循环依赖）
if (typeof window !== 'undefined') {
  window.addEventListener('auth:logout', () => {
    state.token = ''
    state.user = null
    document.body.style.overflow = ''
  })
}
