<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useI18n } from '../i18n/index'
import { useUser } from '../store/user'
import { getConversations, getMessages, sendMessage, blockUser, unblockUser, markRead } from '../api/chat'
import request from '../api/request'
import { confirmDialog, toast } from '../composables/ui'
import { useChatSocket } from '../composables/chatSocket'
import AvatarBubble from './AvatarBubble.vue'
import AdminBadge from './AdminBadge.vue'
import { fmtDateTime } from '../utils/time'

const { t } = useI18n()
const { state: uState } = useUser()

const open = ref(false)
const conversations = ref([])
const activeUserId = ref(null)
const activeUser = ref(null)
const messages = ref([])
const draft = ref('')
const listRef = ref(null)
const inputRef = ref(null)
const showAvatarMenu = ref(false)
const selectMode = ref(false)
const selectedMsgIds = ref(new Set())
const unreadTotal = ref(0)
const unreadMap = ref({})  // { userId: count }
const blockedSet = ref(new Set())  // 我屏蔽了的用户 id
const { connect, disconnect, send, subscribe } = useChatSocket()

function autoGrow() {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

let unsub = null

onMounted(() => {
  if (uState.user) { connect(); loadConversations() }
  unsub = subscribe(onWsMessage)
})
onBeforeUnmount(() => unsub && unsub())

watch(() => uState.user, (u, old) => {
  unreadMap.value = {}
  unreadTotal.value = 0
  activeUserId.value = null
  activeUser.value = null
  messages.value = []
  open.value = false
  if (old) disconnect()
  if (u) { connect(); loadConversations() }
})

// 聊天面板打开时锁定背景滚动
watch(open, (v) => {
  document.body.style.overflow = v ? 'hidden' : ''
})
onBeforeUnmount(() => { document.body.style.overflow = '' })
watch(open, (v) => { if (v && !activeUserId.value) loadConversations() })

function onWsMessage(msg) {
  if (!msg) return
  // 兼容不同消息格式：只要有 fromUserId 就处理
  const fromId = Number(msg.fromUserId)
  const toId = Number(msg.toUserId)
  const myId = Number(uState.user?.id)
  // 只处理发给我的消息
  if (toId && myId && toId !== myId) return
  if (msg.type && msg.type !== 'message') return
  if (fromId === activeUserId.value && open.value) {
    getMessages(activeUserId.value, { page: 1, size: 50 }).then(data => {
      messages.value = (data.records || []).map(m => ({ ...m, own: Number(m.fromUserId) === myId }))
      scrollBottom()
    }).catch(() => {
      messages.value.push({ ...msg, own: false })
      scrollBottom()
    })
  } else {
    const uid = fromId
    unreadMap.value = { ...unreadMap.value, [uid]: (unreadMap.value[uid] || 0) + 1 }
    unreadTotal.value = Object.values(unreadMap.value).reduce((a, b) => a + b, 0)
  }
  loadConversations()
}

async function loadConversations() {
  if (!uState.user) return
  try {
    const list = await getConversations() || []
    conversations.value = list
    // 提取已屏蔽用户
    const bs = new Set()
    list.forEach(c => { if (c.blockedByMe || c.blocked) bs.add(c.userId) })
    blockedSet.value = bs
    // 初始化未读数量（后端返回 unreadCount）
    const nm = {}
    let total = 0
    list.forEach(c => {
      const n = c.unreadCount || 0
      if (n > 0) { nm[c.userId] = n; total += n }
    })
    unreadMap.value = nm
    unreadTotal.value = total
  } catch (e) { /* ignore */ }
}

async function openConversation(conv) {
  activeUserId.value = conv.userId
  activeUser.value = conv
  // 清除该会话未读红点
  if (unreadMap.value[conv.userId]) {
    const nm = { ...unreadMap.value }
    delete nm[conv.userId]
    unreadMap.value = nm
    unreadTotal.value = Object.values(nm).reduce((a, b) => a + b, 0)
  }
  try {
    const data = await getMessages(conv.userId, { page: 1, size: 50 })
    messages.value = (data.records || []).map(m => ({ ...m, own: m.fromUserId === uState.user.id }))
  } catch (e) { messages.value = [] }
  // 通知后端标记已读
  markRead(conv.userId).catch(() => {})
  await nextTick(); scrollBottom()
}

function closeConversation() {
  activeUserId.value = null
  activeUser.value = null
  messages.value = []
  selectMode.value = false
  selectedMsgIds.value = new Set()
  showAvatarMenu.value = false
}

function scrollBottom() {
  nextTick(() => {
    if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight
  })
}

let sending = false
async function sendMsg() {
  const text = draft.value.trim()
  if (!text || !activeUserId.value || sending) return
  sending = true
  try {
    // 先发送成功再显示，失败不渲染假消息、不清空输入框
    await sendMessage(activeUserId.value, text)
    draft.value = ''
    if (inputRef.value) inputRef.value.style.height = 'auto'
    // 拉取最新消息
    const data = await getMessages(activeUserId.value, { page: 1, size: 50 })
    messages.value = (data.records || []).map(m => ({ ...m, own: Number(m.fromUserId) === Number(uState.user.id) }))
  } catch (e) {
    toast(e.message || t('chat.sendFail'), 'error')
  }
  sending = false
  scrollBottom()
  loadConversations()
}

// 手机端回车换行，PC 端 Enter 发送 / Shift+Enter 换行
function onInputKeydown(e) {
  const isMobile = /Android|iPhone|iPad|iPod|Mobile/i.test(navigator.userAgent)
  if (e.key === 'Enter' && !e.shiftKey && !isMobile) {
    e.preventDefault()
    sendMsg()
  }
}

// 由外部触发：从详情页私聊某人。
// 入参是留言/评论记录，其 userId 字段是作者的用户主键 id（不是记录本身的 id）。
function openChatWith(item) {
  open.value = true
  const uid = Number(item.userId)
  if (!uid || uid === Number(uState.user?.id)) return
  activeUser.value = {
    userId: uid,
    nickname: item.nickname,
    avatarUrl: item.avatarUrl,
    account: item.account,
    role: item.role
  }
  activeUserId.value = uid
  messages.value = []
  // 清除该会话未读红点
  if (unreadMap.value[uid]) {
    const nm = { ...unreadMap.value }
    delete nm[uid]
    unreadMap.value = nm
    unreadTotal.value = Object.values(nm).reduce((a, b) => a + b, 0)
  }
  loadConversations()
  markRead(uid).catch(() => {})
  getMessages(uid, { page: 1, size: 50 }).then(data => {
    messages.value = (data.records || []).map(m => ({ ...m, own: m.fromUserId === uState.user.id }))
    scrollBottom()
  }).catch(() => { messages.value = [] })
}

async function togglePin(c) {
  try {
    await request(`/chat/conversations/${c.userId}/pin`, { method: 'POST', body: { pinned: !c.pinned }, auth: true })
    c.pinned = !c.pinned
    loadConversations()
  } catch (e) { toast(e.message, 'error') }
}

async function removeConv(c) {
  const ok = await confirmDialog({ title: t('chat.delConv'), message: t('chat.delConvConfirm').replace('{name}', c.nickname) })
  if (!ok) return
  try {
    await request(`/chat/conversations/${c.userId}`, { method: 'DELETE', auth: true })
    conversations.value = conversations.value.filter(x => x.userId !== c.userId)
    if (activeUserId.value === c.userId) closeConversation()
    toast(t('chat.deleted'))
  } catch (e) { toast(e.message, 'error') }
}

async function clearThisChat() {
  showAvatarMenu.value = false
  if (!activeUser.value) return
  const ids = messages.value.map(m => m.id).filter(Boolean)
  if (!ids.length) { toast(t('chat.noSel')); return }
  const ok = await confirmDialog({ title: t('chat.delChat'), message: t('chat.delChatConfirm').replace('{name}', activeUser.value.nickname) })
  if (!ok) return
  try {
    await request('/chat/messages', { method: 'DELETE', body: { ids }, auth: true })
    messages.value = []
    loadConversations()
    toast(t('chat.deleted'))
  } catch (e) { toast(e.message, 'error') }
}

async function doBlockUser() {
  showAvatarMenu.value = false
  if (!activeUser.value) return
  const uid = activeUser.value.userId
  const isBlocked = blockedSet.value.has(uid)
  if (!isBlocked && activeUser.value.role === 'ADMIN') {
    toast(t('chat.blockUser'), 'error')
    return
  }
  const title = isBlocked ? t('chat.unblockUser') : t('chat.blockUser')
  const msg = isBlocked
    ? t('chat.unblockConfirm').replace('{name}', activeUser.value.nickname)
    : t('chat.blockConfirm').replace('{name}', activeUser.value.nickname)
  const ok = await confirmDialog({ title, message: msg })
  if (!ok) return
  try {
    if (isBlocked) {
      await unblockUser(uid)
      toast(t('chat.unblocked'))
    } else {
      await blockUser(uid)
      toast(t('chat.blocked'))
    }
    // 本地同步状态
    const bs = new Set(blockedSet.value)
    if (isBlocked) bs.delete(uid); else bs.add(uid)
    blockedSet.value = bs
    loadConversations()
  } catch (e) { toast(e.message, 'error') }
}

function toggleSelectMsg(m) {
  const id = m.id
  if (selectedMsgIds.value.has(id)) selectedMsgIds.value.delete(id)
  else selectedMsgIds.value.add(id)
}

function exitSelect() {
  selectMode.value = false
  selectedMsgIds.value = new Set()
}

async function deleteSelected() {
  if (!selectedMsgIds.value.size) return
  const ok = await confirmDialog({ title: t('chat.del'), message: t('chat.delMsgConfirm').replace('{n}', selectedMsgIds.value.size) })
  if (!ok) return
  try {
    await request('/chat/messages', { method: 'DELETE', body: { ids: Array.from(selectedMsgIds.value) }, auth: true })
    messages.value = messages.value.filter(m => !selectedMsgIds.value.has(m.id))
    exitSelect()
    loadConversations()
    toast(t('chat.deleted'))
  } catch (e) { toast(e.message, 'error') }
}

defineExpose({ openChatWith })
</script>

<template>
  <div class="chat-root">
    <!-- 气泡 -->
    <button v-if="!open" class="chat-bubble" @click="open = true" :title="t('chat.title')">
      <span>💬</span>
      <span v-if="unreadTotal > 0" class="badge">{{ unreadTotal > 99 ? '99+' : unreadTotal }}</span>
    </button>

    <!-- 面板 -->
    <div v-if="open" class="chat-panel">
      <div class="cp-head">
        <div class="cp-title">
          <template v-if="!activeUser">{{ t('chat.title') }}</template>
          <template v-else>
            <button class="avatar-btn" @click.stop="showAvatarMenu = !showAvatarMenu" :title="t('chat.title')">
              <AvatarBubble :nickname="activeUser.nickname" :avatar-url="activeUser.avatarUrl" :size="26" />
            </button>
            <b>{{ activeUser.nickname }}</b>
            <AdminBadge :role="activeUser.role" />
            <span v-if="blockedSet.has(activeUser.userId)" class="blocked-tag">{{ t('chat.blockedTag') }}</span>
            <button v-if="!selectMode" class="head-mini" @click="selectMode = true" :title="t('chat.select')">{{ t('chat.select') }}</button>
            <button v-else class="head-mini" @click="exitSelect">{{ t('chat.cancel') }}</button>
          </template>
        </div>
        <button class="cp-close" @click="activeUser ? closeConversation() : (open = false)">×</button>
      </div>
      <div v-if="showAvatarMenu && activeUser" class="avatar-menu">
        <button @click="clearThisChat">{{ t('chat.delChat') }}</button>
        <button v-if="activeUser.role !== 'ADMIN' || blockedSet.has(activeUser.userId)" @click="doBlockUser">
          {{ blockedSet.has(activeUser.userId) ? t('chat.unblockUser') : t('chat.blockUser') }}
        </button>
      </div>

      <!-- 会话列表 -->
      <div v-if="!activeUser" class="cp-body">
        <p v-if="!uState.user" class="muted empty">{{ t('chat.loginFirst') }}</p>
        <p v-else-if="!conversations.length" class="muted empty">{{ t('chat.empty') }}</p>
        <div v-else class="conv-list">
          <div v-for="c in conversations" :key="c.userId" class="conv-row" :class="{ pinned: c.pinned }" @click="openConversation(c)">
            <div class="conv-avatar-wrap">
              <AvatarBubble :nickname="c.nickname" :avatar-url="c.avatarUrl" :size="34" />
              <span v-if="unreadMap[c.userId]" class="badge sm">{{ unreadMap[c.userId] > 99 ? '99+' : unreadMap[c.userId] }}</span>
            </div>
            <div class="conv-info">
              <b>{{ c.nickname }}<AdminBadge :role="c.role" /><span v-if="c.pinned" class="pin-tag">{{ t('chat.pinnedTag') }}</span><span v-if="c.blockedByMe" class="blocked-tag">{{ t('chat.blockedTag') }}</span></b>
              <small>{{ c.lastMessage || '' }}</small>
            </div>
            <div class="conv-actions" @click.stop>
              <button class="conv-btn pin" @click="togglePin(c)">{{ c.pinned ? t('chat.unpin') : t('chat.pinnedTag') }}</button>
              <button class="conv-btn del" @click="removeConv(c)">{{ t('chat.del') }}</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 聊天窗口 -->
      <div v-else class="cp-body chat-body" ref="listRef">
        <p v-if="!messages.length" class="muted empty">{{ t('chat.noMsg') }}</p>
        <div v-for="m in messages" :key="m.id" class="msg-row" :class="{ own: m.own, selected: selectedMsgIds.has(m.id) }" @click="selectMode && toggleSelectMsg(m)">
          <label v-if="selectMode" class="msg-check"><input type="checkbox" :checked="selectedMsgIds.has(m.id)" @click.stop @change="toggleSelectMsg(m)" /></label>
          <div class="bubble">{{ m.content }}</div>
          <small>{{ fmtDateTime(m.createdAt) }}</small>
        </div>
      </div>

      <!-- 选择模式底部栏 -->
      <div v-if="selectMode" class="cp-select-bar">
        <span>{{ t('chat.selected').replace('{n}', selectedMsgIds.size) }}</span>
        <button class="del" @click="deleteSelected">{{ t('chat.del') }}</button>
      </div>

      <!-- 输入区 -->
      <div v-if="activeUser && uState.user" class="cp-input">
        <textarea ref="inputRef" v-model="draft" :placeholder="t('chat.inputPh')" rows="1" @keydown="onInputKeydown" @input="autoGrow"></textarea>
        <button class="cta" @click="sendMsg">{{ t('chat.send') }}</button>
      </div>
      <div v-else-if="activeUser" class="cp-input">
        <button class="outline" @click="$emit('need-login')">{{ t('chat.loginFirst') }}</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-root { position: fixed; right: 20px; bottom: 20px; z-index: 50; }
.chat-bubble {
  position: relative;
  width: 54px; height: 54px; border-radius: 50%;
  background: linear-gradient(135deg, #9dddd7, #71b9df);
  border: none; cursor: pointer; font-size: 22px; color: #fff;
  box-shadow: 0 10px 30px rgba(40, 75, 90, .25);
  transition: transform .2s;
}
.chat-bubble:hover { transform: translateY(-2px) scale(1.05); }
.badge {
  position: absolute; top: -4px; right: -4px;
  min-width: 18px; height: 18px; padding: 0 5px;
  border-radius: 9px; background: #e85d5d; color: #fff;
  font-size: 11px; line-height: 18px; text-align: center;
  font-weight: 600; box-shadow: 0 2px 6px rgba(200,60,60,.4);
}
.badge.sm { min-width: 14px; height: 14px; font-size: 10px; line-height: 14px; }
.conv-avatar-wrap { position: relative; flex-shrink: 0; }

.chat-panel {
  width: min(360px, 92vw); height: 480px; max-height: 70vh;
  background: #faf9f5; border: 1px solid #e0ebef; border-radius: 18px;
  display: flex; flex-direction: column; overflow: hidden;
  box-shadow: 0 20px 60px rgba(40, 75, 90, .25);
}
.cp-head {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 14px; border-bottom: 1px solid #e8efef;
}
.cp-title { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.cp-close { border: none; background: none; font-size: 20px; cursor: pointer; color: var(--muted); }
.avatar-btn { background: none; border: none; padding: 0; cursor: pointer; display: inline-flex; }
.head-mini { background: none; border: none; padding: 2px 6px; font-size: 11px; cursor: pointer; margin-left: 6px; color: var(--muted); }
.blocked-tag { font-size: 10px; padding: 1px 6px; border-radius: 8px; background: #fde2e0; color: #c0574f; margin-left: 6px; }
.conv-info .blocked-tag { position: relative; top: -1px; }
.avatar-menu {
  position: absolute; top: 50px; left: 12px; background: #fff; border: 1px solid var(--line);
  border-radius: 10px; padding: 4px; box-shadow: 0 10px 30px rgba(40,75,90,.15); z-index: 10;
}
.avatar-menu button { display: block; width: 100%; text-align: left; background: none; border: none; padding: 6px 12px; font-size: 12px; cursor: pointer; border-radius: 6px; color: #c0574f; }
.avatar-menu button:hover { background: #f5f7f7; }
.msg-check input { cursor: pointer; }
.msg-row.selected { background: rgba(157,221,215,.15); border-radius: 10px; }
.cp-select-bar { display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; border-top: 1px solid #e8efef; font-size: 12px; }
.cp-select-bar .del { background: #c0574f; color: #fff; border: none; padding: 5px 14px; border-radius: 8px; cursor: pointer; font-size: 12px; }
.cp-body { flex: 1; overflow-y: auto; padding: 12px; }
.empty { text-align: center; margin-top: 40px; font-size: 13px; }

.conv-list { display: flex; flex-direction: column; gap: 8px; }
.conv-row {
  display: flex; gap: 10px; align-items: center; padding: 8px;
  border-radius: 10px; cursor: pointer; transition: background .15s;
}
.conv-row:hover { background: rgba(157, 221, 215, .15); }
.conv-row.pinned { background: rgba(157,221,215,.1); }
.pin-tag { display: inline-block; margin-left: 6px; font-size: 10px; color: var(--blue2); }
.conv-actions { display: flex; gap: 4px; margin-left: auto; opacity: 0; transition: opacity .15s; flex-shrink: 0; }
.conv-row:hover .conv-actions { opacity: 1; }
.conv-btn {
  padding: 3px 8px; font-size: 11px; line-height: 1.3; border-radius: 8px; cursor: pointer;
  border: 1px solid var(--line); background: #fff; color: var(--ink); white-space: nowrap;
}
.conv-btn.del { color: #c0574f; border-color: #ecc7c2; }
.conv-btn.pin { color: var(--blue2); }
.conv-info { display: flex; flex-direction: column; min-width: 0; }
.conv-info b { font-size: 13px; }
.conv-info small { color: var(--muted); font-size: 11px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 200px; }

.chat-body { display: flex; flex-direction: column; gap: 10px; }
.msg-row { display: flex; flex-direction: column; max-width: 75%; width: fit-content; align-self: flex-start; align-items: flex-start; }
.msg-row.own { align-self: flex-end; align-items: flex-end; }
.msg-row .bubble {
  padding: 8px 12px; border-radius: 12px; font-size: 13px; line-height: 1.5;
  background: #fff; border: 1px solid #e0ebef; white-space: pre-wrap; word-break: break-word;
}
.msg-row.own .bubble { background: #9dddd7; border-color: #9dddd7; }
.msg-row small { font-size: 10px; color: var(--muted); margin-top: 3px; }

.cp-input { display: flex; gap: 8px; padding: 10px; border-top: 1px solid #e8efef; align-items: flex-end; }
.cp-input textarea {
  flex: 1 1 0; width: auto; min-width: 0; padding: 9px 12px; border: 1px solid var(--line); border-radius: 10px;
  background: #fff; font-size: 14px; outline: none; resize: none;
  font-family: inherit; line-height: 1.4; max-height: 80px; height: 38px;
}
.cp-input .cta { flex: 0 0 auto; width: auto; padding: 0 16px; font-size: 13px; height: 38px; white-space: nowrap; border-radius: 10px; }

@media (max-width: 720px) {
  .chat-root { right: 12px; bottom: 12px; }
  .chat-panel { height: 75vh; width: calc(100vw - 24px); right: 0; bottom: 0; border-radius: 16px; }
}
</style>
