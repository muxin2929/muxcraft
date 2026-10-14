<script setup>
import { ref, provide, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteTopBar from './components/SiteTopBar.vue'
import SideDrawer from './components/SideDrawer.vue'
import AuthModal from './components/AuthModal.vue'
import SiteFooter from './components/SiteFooter.vue'
import AppToast from './components/AppToast.vue'
import AppConfirm from './components/AppConfirm.vue'
import ChatWidget from './components/ChatWidget.vue'
import SiteGate from './components/SiteGate.vue'
import { getSite } from './api/content'
import { tryRestoreSession } from './api/auth'
import { useUser } from './store/user'

const drawerOpen = ref(false)
const loginOpen = ref(false)
const chatRef = ref(null)
const route = useRoute()
const router = useRouter()
const { state: uState, setSession } = useUser()

// 全站动态站点信息（标题/描述等）
const site = ref({})
provide('site', site)

// 供详情页“私聊”按钮触发聊天窗口
provide('openChatWith', (user) => chatRef.value?.openChatWith(user))

onMounted(async () => {
  document.body.style.overflow = ''
  // 页面加载时用 cookie 恢复登录态
  if (!uState.token && uState.user) {
    try {
      const data = await tryRestoreSession()
      if (data) setSession({ token: data.token, user: data.user })
    } catch { /* 未登录 */ }
  }
  try {
    site.value = await getSite() || {}
    if (site.value.siteTitle) document.title = site.value.siteTitle
    if (site.value.siteDescription) {
      let m = document.querySelector('meta[name="description"]')
      if (!m) { m = document.createElement('meta'); m.name = 'description'; document.head.appendChild(m) }
      m.content = site.value.siteDescription
    }
  } catch (e) { /* 后端未就绪时用默认 */ }
})

// 供内部组件（留言/评论）打开登录弹窗
provide('openLogin', () => {
  if (route.path === '/register') {
    router.push({ path: '/', query: { login: '1' } })
  } else {
    loginOpen.value = true
  }
})

// 401 时自动弹出登录框
function onAuthRequired() { loginOpen.value = true }
onMounted(() => window.addEventListener('auth:required', onAuthRequired))
onUnmounted(() => window.removeEventListener('auth:required', onAuthRequired))

// 注册成功/点“去登录”带 ?login=1 跳回首页：弹出登录框后立即清掉 query，
// 避免刷新时 URL 残留导致弹窗再次自动弹出。
watch(
  () => route.query.login,
  (v) => {
    if (v) {
      loginOpen.value = true
      router.replace({ path: route.path, query: {} })
    }
  },
  { immediate: true }
)
</script>

<template>
  <SiteGate />
  <SiteTopBar @open-menu="drawerOpen = true" @open-login="loginOpen = true" />
  <router-view v-slot="{ Component }">
    <component :is="Component" />
  </router-view>
  <SiteFooter />
  <SideDrawer :open="drawerOpen" @close="drawerOpen = false" @open-login="loginOpen = true" />
  <AuthModal :open="loginOpen" @close="loginOpen = false" />
  <AppToast />
  <AppConfirm />
  <ChatWidget ref="chatRef" />
</template>
