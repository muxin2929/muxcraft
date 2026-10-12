<script setup>
import { ref, computed, inject, onMounted } from 'vue'
import { useI18n } from '../i18n/index'
import { useUser } from '../store/user'
import { getGuestbook, postGuestbook } from '../api/content'
import { toast } from '../composables/ui'
import AvatarBubble from '../components/AvatarBubble.vue'
import AdminBadge from '../components/AdminBadge.vue'
import MessageDetailModal from '../components/MessageDetailModal.vue'
import { fmtDateTime } from '../utils/time'

const { t } = useI18n()
const { state: uState } = useUser()
const openLogin = inject('openLogin')
const openChatWith = inject('openChatWith')

const records = ref([])
const draft = ref('')
const loading = ref(true)
const err = ref('')
const detail = ref(null)

// 屏蔽用户的留言前端过滤不渲染
const visibleRecords = computed(() => records.value.filter(m => !m.blockedByMe))

async function load() {
  loading.value = true
  try {
    const data = await getGuestbook({ page: 1, size: 50 })
    records.value = data.records || []
  } catch (e) { err.value = e.message }
  finally { loading.value = false }
}
onMounted(load)

async function post() {
  if (!uState.user) return openLogin()
  if (!draft.value.trim()) return
  try {
    await postGuestbook(draft.value.trim())
    toast('留言已发布')
    draft.value = ''
    load()
  } catch (e) { toast('发表失败：' + e.message, 'error') }
}
</script>

<template>
  <main class="page">
    <div class="page-head" v-reveal>
      <router-link to="/" class="back-link">{{ t('back') }}</router-link>
      <div class="eyebrow" style="margin-top:14px">{{ t('guestbook.eyebrow') }}</div>
      <h2 class="title">{{ t('guestbook.title') }}</h2>
      <p class="lead" style="margin-top:10px">{{ t('guestbook.lead') }}</p>
    </div>

    <div class="panel" v-reveal style="margin-top:20px">
      <p v-if="loading" class="muted">Loading…</p>
      <p v-else-if="err" class="muted">{{ err }}</p>
      <template v-else>
        <div class="comment-list">
          <div class="comment-row clickable" v-for="m in visibleRecords" :key="m.id" style="margin-bottom:16px" @click="detail = m">
            <AvatarBubble :nickname="m.nickname" :avatar-url="m.avatarUrl" :size="40" />
            <div class="c-body">
              <div class="c-head">
                <b>{{ m.nickname }}<AdminBadge :role="m.role" /></b>
                <small v-if="m.replyToNickname" class="reply-tag">回复 {{ m.replyToNickname }}</small>
                <small>{{ fmtDateTime(m.createdAt) }}</small>
              </div>
              <p class="muted">{{ m.content }}</p>
            </div>
          </div>
        </div>
        <template v-if="uState.user">
          <div class="field" style="margin-top:14px">
            <textarea v-model="draft" :placeholder="t('guestbook.placeholder')"></textarea>
          </div>
          <button class="outline" @click="post">{{ t('guestbook.submit') }}</button>
        </template>
        <button v-else class="outline" style="margin-top:15px" @click="openLogin">{{ t('guestbook.loginFirst') }}</button>
      </template>
    </div>

    <MessageDetailModal
      :item="detail"
      type="guestbook"
      @close="detail = null"
      @reply-sent="detail = null; load()"
      @block="detail = null; load()"
      @private-chat="(u) => { detail = null; openChatWith(u) }"
    />
  </main>
</template>

<style scoped>
.clickable { cursor: pointer; transition: background .15s; border-radius: 10px; padding: 6px; }
.clickable:hover { background: rgba(157,221,215,.12); }
.reply-tag { color: var(--blue2); font-size: 11px; margin: 0 4px; }
</style>
