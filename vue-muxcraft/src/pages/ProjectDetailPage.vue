<script setup>
import { ref, computed, inject, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from '../i18n/index'
import { useUser } from '../store/user'
import { getProject, getComments, postComment } from '../api/content'
import { toast } from '../composables/ui'
import AvatarBubble from '../components/AvatarBubble.vue'
import AdminBadge from '../components/AdminBadge.vue'
import MessageDetailModal from '../components/MessageDetailModal.vue'
import { fmtDateTime } from '../utils/time'

const route = useRoute()
const { t } = useI18n()
const { state: uState } = useUser()
const openLogin = inject('openLogin')
const openChatWith = inject('openChatWith')

const project = ref(null)
const comments = ref([])
const draft = ref('')
const loading = ref(true)
const err = ref('')

// 屏蔽用户的评论前端过滤不渲染
const visibleComments = computed(() => comments.value.filter(c => !c.blockedByMe))
const detail = ref(null)

async function load() {
  loading.value = true
  try {
    project.value = await getProject(route.params.id)
    const c = await getComments(route.params.id, { page: 1, size: 50 })
    comments.value = c.records || []
  } catch (e) { err.value = e.message }
  finally { loading.value = false }
}
onMounted(load)

async function post() {
  if (!uState.user) return openLogin()
  if (!draft.value.trim()) return
  try {
    await postComment(route.params.id, draft.value.trim())
    toast('评论已发布')
    draft.value = ''
    load()
  } catch (e) { toast('发表失败：' + e.message, 'error') }
}
</script>

<template>
  <main class="page">
    <div class="page-head" v-reveal>
      <router-link to="/projects" class="back-link">{{ t('back') }}</router-link>
      <div class="eyebrow" style="margin-top:14px">{{ t('detail.eyebrow') }}</div>
      <h2 class="title">{{ project?.name || t('detail.title') }}</h2>
      <p class="lead" style="margin-top:10px">{{ t('detail.lead') }}</p>
      <div v-if="project?.createdAt" style="margin-top:10px;color:var(--muted);font-size:13px">发布于 {{ fmtDateTime(project.createdAt) }}</div>
    </div>

    <p v-if="loading" class="muted" style="margin-top:20px">Loading…</p>
    <p v-else-if="err" class="muted" style="margin-top:20px">{{ err }}</p>

    <template v-else>
      <div class="panel" v-reveal style="margin-top:20px">
        <div class="feature" :style="project.coverUrl ? {background:`center/cover url(${project.coverUrl})`,height:'260px'}:{height:'260px'}"></div>
        <div class="about-grid" style="margin-top:20px">
          <div>
            <div class="micro">ABOUT</div>
            <p class="muted">{{ project.description || project.summary }}</p>
            <div class="proj-actions">
              <a v-if="project.githubUrl" class="cta" :href="project.githubUrl" target="_blank" rel="noopener">GITHUB ↗</a>
              <a v-if="project.demoUrl" class="outline" :href="project.demoUrl" target="_blank" rel="noopener">DEMO ↗</a>
            </div>
          </div>
          <div>
            <div class="micro">TECHNOLOGY</div>
            <div v-if="(project.skills||[]).length" style="display:flex;flex-wrap:wrap;gap:8px;margin-top:8px">
              <span v-for="s in project.skills" :key="s.id" class="tag">{{ s.name }}</span>
            </div>
            <p v-else class="muted">—</p>
            <div class="tag" style="margin-top:14px">{{ visibleComments.length }} COMMENTS</div>
          </div>
        </div>
      </div>

      <div class="panel" v-reveal style="margin-top:18px">
        <div class="micro">DISCUSSION</div>
        <h3 style="margin:8px 0 16px">{{ t('comment.title') }}</h3>
        <div class="comment-list">
          <div class="comment-row clickable" v-for="c in visibleComments" :key="c.id" style="margin-bottom:14px" @click="detail = { ...c, projectName: project.name }">
            <AvatarBubble :nickname="c.nickname" :avatar-url="c.avatarUrl" :size="38" />
            <div class="c-body">
              <div class="c-head">
                <b>{{ c.nickname }}<AdminBadge :role="c.role" /></b>
                <small v-if="c.replyToNickname" class="reply-tag">回复 {{ c.replyToNickname }}</small>
                <small>{{ fmtDateTime(c.createdAt) }}</small>
              </div>
              <p class="muted">{{ c.content }}</p>
            </div>
          </div>
        </div>
        <template v-if="uState.user">
          <div class="field" style="margin-top:14px">
            <textarea v-model="draft" :placeholder="t('guestbook.placeholder')"></textarea>
          </div>
          <button class="outline" @click="post">{{ t('guestbook.submit') }}</button>
        </template>
        <button v-else class="outline" style="margin-top:15px" @click="openLogin">{{ t('comment.loginFirst') }}</button>
      </div>
    </template>

    <MessageDetailModal
      :item="detail"
      type="comment"
      :project-id="project?.id"
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
.proj-actions { display: flex; gap: 10px; margin-top: 18px; flex-wrap: wrap; }
.proj-actions .cta, .proj-actions .outline { flex: 1 1 auto; padding: 10px 18px; text-align: center; text-decoration: none; }
@media (max-width: 640px) {
  .proj-actions .cta, .proj-actions .outline { flex: 1 1 40%; font-size: 13px; padding: 9px 12px; }
}
</style>
