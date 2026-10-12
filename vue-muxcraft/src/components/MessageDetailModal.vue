<script setup>
import { ref, watch, computed, inject, onMounted, onUnmounted } from 'vue'
import { useI18n } from '../i18n/index'
import { useUser } from '../store/user'
import AvatarBubble from './AvatarBubble.vue'
import AdminBadge from './AdminBadge.vue'
import { toast, confirmDialog } from '../composables/ui'
import { postGuestbook } from '../api/content'
import { sendMessage, blockUser, unblockUser } from '../api/chat'
import { fmtDateTime } from '../utils/time'

const props = defineProps({
  item: { type: Object, default: null },     // 当前留言/评论/建议
  type: { type: String, default: 'guestbook' }, // guestbook | comment | suggestion
  projectId: { type: [Number, String], default: null }
})
const emit = defineEmits(['close', 'reply-sent', 'private-chat', 'block', 'delete'])

const { t } = useI18n()
const { state: uState } = useUser()
const openLogin = inject('openLogin')

// 401 弹登录框时自动关闭详情
function onAuthRequired() { emit('close') }
onMounted(() => window.addEventListener('auth:required', onAuthRequired))
onUnmounted(() => window.removeEventListener('auth:required', onAuthRequired))

const mode = ref('')   // '' | 'reply' | 'private'
const draft = ref('')

// 屏蔽状态完全由后端返回的 item.blockedByMe 驱动
const isBlocked = computed(() => !!props.item?.blockedByMe)

watch(() => props.item, () => { mode.value = ''; draft.value = '' })

// 屏蔽按钮可见：未登录(blockedByMe=null)不显示；不是自己；管理员不能被屏蔽，但已屏蔽管理员可解除
const canBlock = computed(() => {
  if (!props.item) return false
  if (props.item.blockedByMe === null || props.item.blockedByMe === undefined) return false
  if (Number(props.item.userId) === Number(uState.user?.id)) return false
  if (uState.user?.role === 'ADMIN' || uState.user?.role === 'admin') return false
  // 对方是管理员时，只有已屏蔽才显示按钮（用于解除）
  if (props.item.role === 'ADMIN' || props.item.role === 'admin') return isBlocked.value
  return true
})

async function submitReply() {
  const text = draft.value.trim()
  if (!text) return toast(t('chat.emptyReply'), 'error')
  try {
    if (props.type === 'guestbook') {
      await postGuestbook(text, props.item.id)
    } else if (props.type === 'comment') {
      const { postComment } = await import('../api/content')
      await postComment(props.projectId, text, props.item.id)
    }
    toast(t('chat.replied'))
    draft.value = ''
    mode.value = ''
    emit('reply-sent')
  } catch (e) {
    toast(e.message, 'error')
    if (e.status === 401 || e.code === 401) {
      draft.value = ''
      mode.value = ''
      emit('close')
    }
  }
}

function openPrivate() {
  if (!uState.user) return toast(t('chat.loginFirst'), 'error')
  if (Number(props.item.userId) === Number(uState.user.id)) return toast('不能和自己聊天', 'error')
  emit('private-chat', props.item)
}

async function doBlock() {
  const currentlyBlocked = isBlocked.value
  const title = currentlyBlocked ? '取消屏蔽' : '屏蔽用户'
  const message = currentlyBlocked
    ? `确定取消屏蔽「${props.item.nickname}」？取消后可以重新看到对方发言。`
    : `确定屏蔽「${props.item.nickname}」？屏蔽后互相不能看到对方发言，也不能私聊。`
  const ok = await confirmDialog({ title, message })
  if (!ok) return
  try {
    if (currentlyBlocked) {
      await unblockUser(props.item.userId)
      toast('已取消屏蔽')
    } else {
      await blockUser(props.item.userId)
      toast('已屏蔽')
    }
    emit('block', props.item)
    emit('close')
  } catch (e) { toast(e.message || '操作失败', 'error') }
}
</script>

<template>
  <Teleport to="body">
    <div v-if="item" class="dm-mask" @click.self="emit('close')">
      <div class="dm-box">
        <button class="dm-close" @click="emit('close')">×</button>

        <!-- 头部：头像 + 用户信息 -->
        <div class="dm-head">
          <AvatarBubble :nickname="item.nickname" :avatar-url="item.avatarUrl" :size="56" />
          <div>
            <div class="dm-name">
              {{ item.nickname }}
              <AdminBadge :role="item.role" />
            </div>
            <small v-if="item.replyToNickname" class="dm-reply-tag">回复 {{ item.replyToNickname }}</small>
            <small class="dm-uid">@{{ item.account || item.userId }}</small>
            <div class="dm-time">{{ fmtDateTime(item.createdAt) }}</div>
          </div>
        </div>

        <!-- 项目名（评论） -->
        <div v-if="type === 'comment' && item.projectName" class="dm-project">
          {{ t('chat.project') }}：{{ item.projectName }}
        </div>

        <!-- 建议标题 -->
        <div v-if="type === 'suggestion' && item.title" class="dm-title">{{ item.title }}</div>

        <!-- 正文 -->
        <div class="dm-content">{{ item.content }}</div>

        <!-- 操作区 -->
        <div v-if="!mode" class="dm-actions">
          <template v-if="type !== 'suggestion'">
            <button class="dm-btn" @click="mode = 'reply'">{{ t('chat.reply') }}</button>
          </template>
          <button v-if="Number(item.userId) !== Number(uState.user?.id)" class="dm-btn" @click="openPrivate">{{ t('chat.private') }}</button>
          <button v-if="canBlock" class="dm-btn ghost" @click="doBlock">{{ isBlocked ? '取消屏蔽' : t('chat.block') }}</button>
        </div>

        <!-- 回复输入框 -->
        <div v-if="mode === 'reply'" class="dm-reply">
          <textarea v-model="draft" :placeholder="t('chat.replyPh')" rows="3"></textarea>
          <div class="dm-reply-ops">
            <button class="dm-btn ghost" @click="mode = ''; draft = ''">{{ t('chat.cancel') }}</button>
            <button class="dm-btn primary" @click="submitReply">{{ t('chat.replySend') }}</button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.dm-mask {
  position: fixed; inset: 0; z-index: 350;
  background: rgba(245,247,245,.6); backdrop-filter: blur(6px);
  display: grid; place-items: center; padding: 20px;
}
.dm-box {
  width: min(520px, 94vw); max-height: 85vh; overflow-y: auto;
  background: #faf9f5; border: 1px solid #e0ebef; border-radius: 20px;
  padding: 26px; position: relative;
  box-shadow: 0 24px 70px rgba(40, 75, 90, .18);
}
.dm-close {
  position: absolute; top: 12px; right: 14px;
  border: none; background: none; font-size: 22px; cursor: pointer; color: var(--muted);
}
.dm-head { display: flex; gap: 14px; align-items: center; margin-bottom: 14px; }
.dm-name { font-size: 16px; font-weight: 700; }
.dm-uid { color: var(--muted); font-size: 12px; }
.dm-reply-tag { color: var(--blue2); font-size: 11px; margin-left: 8px; }
.dm-time { color: var(--muted); font-size: 12px; margin-top: 2px; }
.dm-project { font-size: 12px; color: var(--blue2); margin-bottom: 10px; }
.dm-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; }
.dm-content {
  font-size: 14px; line-height: 1.7; color: var(--ink);
  background: rgba(255,255,255,.6); border-radius: 12px; padding: 14px;
  white-space: pre-wrap; margin: 10px 0 16px;
}
.dm-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.dm-btn {
  padding: 7px 16px; border-radius: 10px; font-size: 12px; cursor: pointer;
  border: 1px solid var(--line); background: #fff; color: var(--ink);
  transition: .15s;
}
.dm-btn:hover { background: #fff; border-color: #b7dce8; }
.dm-btn.ghost { color: var(--muted); }
.dm-btn.primary { background: var(--ink); color: #fff; border-color: var(--ink); }
.dm-reply { margin-top: 14px; }
.dm-reply textarea {
  width: 100%; padding: 10px 12px; border: 1px solid var(--line);
  border-radius: 10px; background: #fff; font-size: 13px; resize: vertical;
  font-family: inherit;
}
.dm-reply-ops { display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px; }
@media (max-width: 720px) {
  .dm-box { padding: 20px; }
  .dm-actions { gap: 6px; }
}
</style>
