<script setup>
import { ref, reactive, onMounted, watch, onBeforeUnmount, inject, computed } from 'vue'
import { useI18n } from '../i18n/index'
import { useUser } from '../store/user'
import request, { getAccessToken } from '../api/request'
import { toast, confirmDialog } from '../composables/ui'
import { fmtDateTime } from '../utils/time'
const { t } = useI18n()
const { state: uState } = useUser()
const openChatWith = inject('openChatWith')

const tabs = [
  { key: 'settings', label: '站点 / 资料' },
  { key: 'projects', label: '项目' },
  { key: 'experiences', label: '经历' },
  { key: 'skills', label: '技术栈' },
  { key: 'articles', label: '文章 / 博客' },
  { key: 'achievements', label: '成就' },
  { key: 'users', label: '用户' },
  { key: 'comments', label: '评论' },
  { key: 'guestbook', label: '留言' },
  { key: 'suggestions', label: '建议' }
]
const active = ref('settings')

// 字段定义：每个资源在编辑表单里展示哪些字段
const schemas = {
  projects: [
    { k: 'name', l: '名称' }, { k: 'slug', l: '仓库 slug' },
    { k: 'summary', l: '简介', wide: true },
    { k: 'description', l: '详细描述', type: 'textarea', wide: true },
    { k: 'githubUrl', l: 'GitHub 仓库地址' },
    { k: 'demoUrl', l: 'GitHub Pages 地址' },
    { k: 'coverUrl', l: '封面图', type: 'image' },
    { k: 'techStack', l: '技术栈（多选）', type: 'multiselect' },
    { k: 'sortOrder', l: '排序', type: 'number', short: true }
  ],
  experiences: [
    { k: 'title', l: '标题' }, { k: 'organization', l: '组织' },
    { k: 'description', l: '描述', type: 'textarea', wide: true },
    { k: 'startDate', l: '开始日期', type: 'date' },
    { k: 'endDate', l: '结束日期', type: 'date' },
    { k: 'pageUrl', l: 'Hexo/GitHub Pages 地址', wide: true },
    { k: 'coverUrl', l: '封面图', type: 'image', wide: true }
  ],
  skills: [
    { k: 'name', l: '名称' },
    { k: 'category', l: '分类', type: 'singleselect', options: ['frontend', 'backend', 'tool'] },
    { k: 'description', l: '描述', type: 'textarea', wide: true },
    { k: 'iconUrl', l: '图标', type: 'image' },
    { k: 'rating', l: '熟练度(1-5)', type: 'number', short: true },
    { k: 'sortOrder', l: '排序', type: 'number', short: true }
  ],
  articles: [
    { k: 'title', l: '标题' },
    { k: 'slug', l: '静态页面链接（托管平台地址）', wide: true },
    { k: 'summary', l: '简介', type: 'textarea', wide: true },
    { k: 'coverUrl', l: '封面图', type: 'image', wide: true },
    { k: 'status', l: '发布状态', type: 'switch', options: ['DRAFT', 'PUBLISHED'] },
    { k: 'publishedAt', l: '发布时间', type: 'datetime-local' }
  ],
  achievements: [
    { k: 'year', l: '年份' }, { k: 'title', l: '标题' },
    { k: 'description', l: '描述', wide: true },
    { k: 'sortOrder', l: '排序', type: 'number' }
  ]
}

const list = ref([])
const loading = ref(false)
const err = ref('')
const editing = ref(null)       // 当前编辑对象
const isNew = ref(false)
const skillNames = ref([])
const skillOptions = ref([])  // [{id, name}]      // 技术栈名称列表（项目表多选用）
const techOpen = ref(false)
const singleOpenKey = ref('')

function toggleTech(key, name) {
  const cur = (editing.value[key] || '').split(',').filter(Boolean)
  const idx = cur.indexOf(name)
  if (idx >= 0) cur.splice(idx, 1)
  else cur.push(name)
  editing.value[key] = cur.join(',')
}

function toggleSkillId(id) {
  if (!Array.isArray(editing.value.skill_ids)) editing.value.skill_ids = []
  const idx = editing.value.skill_ids.indexOf(id)
  if (idx >= 0) editing.value.skill_ids.splice(idx, 1)
  else editing.value.skill_ids.push(id)
}

const selectedSkillNames = computed(() => {
  const ids = editing.value?.skill_ids || []
  return skillOptions.value.filter(o => ids.includes(o.id)).map(o => o.name).join(', ')
})
const savedMsg = ref(false)

// 用户管理
const userPage = reactive({ page: 1, size: 10, total: 0, totalPages: 1 })
const userSearch = reactive({ keyword: '', startDate: '', endDate: '' })
const userEditing = ref(null)   // 当前编辑的用户

// 评论/留言/建议 批量选择 + 详情
const selectedIds = ref(new Set())
const detailItem = ref(null)    // 详情弹窗当前项
const replyMode = ref(false)
const replyDraft = ref('')

function openPrivate() {
  if (!detailItem.value) return
  if (Number(detailItem.value.userId) === Number(uState.user?.id)) return toast('不能和自己聊天', 'error')
  openChatWith && openChatWith(detailItem.value)
  detailItem.value = null
}

async function submitReply() {
  const text = replyDraft.value.trim()
  if (!text) return toast('回复不能为空', 'error')
  try {
    if (active.value === 'guestbook') {
      await request('/guestbook', { method: 'POST', body: { content: text, replyToId: detailItem.value.id }, auth: true })
    } else if (active.value === 'comments') {
      await request(`/projects/${detailItem.value.projectId}/comments`, { method: 'POST', body: { content: text, replyToId: detailItem.value.id }, auth: true })
    }
    toast('回复已发布')
    replyDraft.value = ''
    replyMode.value = false
    detailItem.value = null
    loadList()
  } catch (e) { toast(e.message, 'error') }
}

function toggleSelect(id) {
  if (selectedIds.value.has(id)) selectedIds.value.delete(id)
  else selectedIds.value.add(id)
  selectedIds.value = new Set(selectedIds.value)
}
function toggleSelectAll() {
  if (selectedIds.value.size === list.value.length) selectedIds.value = new Set()
  else selectedIds.value = new Set(list.value.map(r => r.id))
}
async function batchDelete() {
  if (!selectedIds.value.size) { toast('请先选择要删除的项', 'error'); return }
  const ok = await confirmDialog({
    title: '批量删除',
    message: `确定删除选中的 ${selectedIds.value.size} 项吗？此操作不可恢复。`
  })
  if (!ok) return
  loading.value = true
  try {
    const ids = Array.from(selectedIds.value).join(',')
    await request(`/admin/${active.value}/batch?ids=${ids}`, { method: 'DELETE', auth: true })
    toast(`已删除 ${selectedIds.value.size} 项`)
    selectedIds.value = new Set()
    await loadList()
  } catch (e) { toast('批量删除失败：' + e.message, 'error') }
  finally { loading.value = false }
}

// 站点设置是单对象
const settings = reactive({
  siteTitle: '', siteDescription: '',
  profileName: '', profileTitle: '', shortIntro: '', fullIntro: '',
  location: '', photoUrl: '', githubUrl: '', websiteUrl: '',
  registrationEnabled: true, guestbookEnabled: true, commentEnabled: true
})

// 图片字段 → FormData 里文件字段名映射
// key: schema 里的字段名（xxxUrl），value: 后端接口接收的文件字段名
const IMG_FIELD_MAP = {
  photoUrl: 'photo',
  coverUrl: 'cover',
  iconUrl: 'icon',
  avatarUrl: 'avatar',
}

// 选图：不立即上传，只保存 File 对象 + 本地预览
const pendingFiles = ref({})   // { [field]: File }
function pickUpload(field, obj, e) {
  const f = e.target.files?.[0]
  if (!f) return
  pendingFiles.value[field] = f
  // 本地预览：用 objectURL 临时替换 URL 字段
  if (obj[field]) URL.revokeObjectURL(obj[field])
  obj[field] = URL.createObjectURL(f)
  e.target.value = ''
}

// 把业务对象组装成 FormData：data=JSON字符串 + 图片文件
function buildFormData(obj, fields) {
  const fd = new FormData()
  const dataPayload = {}
  fields.forEach(f => { if (f !== 'techStack') dataPayload[f] = obj[f] })
  // 也带上 id（PUT 时后端需要）
  if (obj.id != null) dataPayload.id = obj.id
  // 项目技术栈：id 数组
  if (active.value === 'projects') dataPayload.skill_ids = obj.skill_ids || []
  fd.append('data', JSON.stringify(dataPayload))
  // append 对应的图片文件
  Object.keys(IMG_FIELD_MAP).forEach(k => {
    if (pendingFiles.value[k]) fd.append(IMG_FIELD_MAP[k], pendingFiles.value[k])
  })
  return fd
}

async function loadSettings() {
  loading.value = true
  try {
    const s = await request('/admin/settings', { auth: true })
    Object.assign(settings, s)
  } catch (e) { err.value = e.message }
  finally { loading.value = false }
}

async function saveSettings() {
  loading.value = true
  try {
    const fields = ['siteTitle','siteDescription','profileName','profileTitle','shortIntro','fullIntro',
                     'location','photoUrl','githubUrl','websiteUrl',
                     'registrationEnabled','guestbookEnabled','commentEnabled']
    const fd = buildFormData(settings, fields)
    const res = await fetch('/api/admin/settings', {
      method: 'PUT',
      headers: { Authorization: 'Bearer ' + (getAccessToken() || '') },
      credentials: 'include',
      body: fd
    })
    const json = await res.json()
    if (json.code !== 200) throw new Error(json.message || '保存失败')
    pendingFiles.value = {}
    toast('设置已保存')
  } catch (e) { toast('保存失败：' + e.message, 'error') }
  finally { loading.value = false }
}

async function loadList() {
  if (active.value === 'settings') return loadSettings()
  loading.value = true; err.value = ''
  selectedIds.value = new Set()
  try {
    if (active.value === 'users') {
      const q = new URLSearchParams({
        page: userPage.page, size: userPage.size
      })
      if (userSearch.keyword) q.set('keyword', userSearch.keyword)
      if (userSearch.startDate) q.set('startTime', userSearch.startDate)
      if (userSearch.endDate) q.set('endTime', userSearch.endDate)
      const data = await request(`/admin/users?${q}`, { auth: true })
      list.value = data.records || []
      userPage.total = data.total || 0
      userPage.totalPages = data.pages || 1
    } else if (['comments', 'guestbook', 'suggestions'].includes(active.value)) {
      const data = await request(`/admin/${active.value}?page=1&size=100`, { auth: true })
      list.value = data.records || data || []
    } else {
      const data = await request(`/admin/${active.value}?page=1&size=100`, { auth: true })
      list.value = data.records || data || []
    }
  } catch (e) { err.value = e.message }
  finally { loading.value = false }
}

async function saveUserStatus() {
  if (!userEditing.value) return
  loading.value = true
  try {
    await request(`/admin/users/${userEditing.value.id}/status`, {
      method: 'PUT',
      body: { status: userEditing.value.status },
      auth: true
    })
    toast('用户状态已更新')
    userEditing.value = null
    await loadList()
  } catch (e) { toast('更新失败：' + e.message, 'error') }
  finally { loading.value = false }
}

async function deleteUser(row) {
  const ok = await confirmDialog({
    title: '删除用户',
    message: `确定删除用户「${row.nickname}（${row.userId}）」吗？此操作不可恢复。`
  })
  if (!ok) return
  loading.value = true
  try {
    await request(`/admin/users/${row.id}`, { method: 'DELETE', auth: true })
    toast('已删除')
    await loadList()
  } catch (e) { toast('删除失败：' + e.message, 'error') }
  finally { loading.value = false }
}

function newItem() {
  const blank = {}
  ;(schemas[active.value] || []).forEach(f => { blank[f.k] = f.type === 'number' ? 0 : '' })
  if (active.value === 'projects') blank.skill_ids = []
  editing.value = blank
  isNew.value = true
}

async function openEdit(row) {
  const copy = { ...row }
  try {
    const detail = await request(`/admin/${active.value}/${row.id}`, { auth: true })
    Object.assign(copy, detail || {})
  } catch (e) { /* ignore */ }
  const ids = copy.skill_ids || copy.skillIds || copy.techStack || []
  if (Array.isArray(ids)) copy.skill_ids = ids.map(Number).filter(Boolean)
  else if (typeof ids === 'string') copy.skill_ids = ids.split(',').map(Number).filter(Boolean)
  editing.value = copy
  isNew.value = false
}

async function saveItem() {
  loading.value = true
  try {
    const fields = (schemas[active.value] || []).map(f => f.k)
    const hasImage = (schemas[active.value] || []).some(f => f.type === 'image')

    // 无图片字段的资源（如成就）走 JSON，保持原接口
    if (!hasImage) {
      const body = {}
      fields.forEach(f => { if (f !== 'techStack') body[f] = editing.value[f] })
      if (editing.value.id != null) body.id = editing.value.id
      if (active.value === 'projects') body.skill_ids = editing.value.skill_ids || []
      await request(`/admin/${active.value}${isNew.value ? '' : '/' + editing.value.id}`, {
        method: isNew.value ? 'POST' : 'PUT',
        body, auth: true
      })
      toast(isNew.value ? '新增成功' : '修改成功')
      editing.value = null
      await loadList()
      return
    }

    // 有图片字段的走 multipart
    const fd = buildFormData(editing.value, fields)
    const url = isNew.value
      ? `/api/admin/${active.value}`
      : `/api/admin/${active.value}/${editing.value.id}`
    const res = await fetch(url, {
      method: isNew.value ? 'POST' : 'PUT',
      headers: { Authorization: 'Bearer ' + (getAccessToken() || '') },
      credentials: 'include',
      body: fd
    })
    const json = await res.json()
    if (json.code !== 200) throw new Error(json.message || '保存失败')
    pendingFiles.value = {}
    toast(isNew.value ? '新增成功' : '修改成功')
    editing.value = null
    await loadList()
  } catch (e) { toast('操作失败：' + e.message, 'error') }
  finally { loading.value = false }
}

async function askRemove(row) {
  const ok = await confirmDialog({
    title: '确认删除',
    message: `删除「${row.name || row.title || row.nickname || '#' + row.id}」后不可恢复。`
  })
  if (!ok) return
  loading.value = true
  try {
    await request(`/admin/${active.value}/${row.id}`, { method: 'DELETE', auth: true })
    toast('已删除')
    await loadList()
  } catch (e) { toast('删除失败：' + e.message, 'error') }
  finally { loading.value = false }
}

function switchTab(k) {
  active.value = k
  editing.value = null
  detailItem.value = null
  selectedIds.value = new Set()
  userPage.page = 1
  loadList()
}

watch(editing, (v) => {
  if (v) {
    document.body.style.overflow = 'hidden'
    document.body.classList.add('modal-open')
  } else {
    document.body.style.overflow = ''
    document.body.classList.remove('modal-open')
  }
})
onBeforeUnmount(() => {
  document.body.style.overflow = ''
  document.body.classList.remove('modal-open')
})

onMounted(async () => {
  loadList()
  try {
    const r = await request('/skills')
    const arr = Array.isArray(r) ? r : (r.data || r.records || [])
    const names = []
    const opts = []
    arr.forEach(g => {
      if (Array.isArray(g.skills)) g.skills.forEach(s => { names.push(s.name); opts.push({ id: s.id, name: s.name }) })
      else if (g.name) { names.push(g.name); opts.push({ id: g.id, name: g.name }) }
    })
    skillNames.value = names
    skillOptions.value = opts
  } catch (e) { /* ignore */ }
})
</script>

<template>
  <main class="page">
    <div class="page-head" v-reveal>
      <router-link to="/" class="back-link">{{ t('back') }}</router-link>
      <div class="eyebrow" style="margin-top:14px">ADMIN</div>
      <h2 class="title">{{ t('admin.title') }}</h2>
      <p class="lead" style="margin-top:10px">{{ t('admin.lead') }}</p>
    </div>

    <div v-if="uState.user && uState.user.role === 'ADMIN'" v-reveal style="margin-top:20px">
      <div class="tab-bar">
        <button
          v-for="tb in tabs" :key="tb.key"
          class="tab" :class="{ on: active === tb.key }"
          @click="switchTab(tb.key)"
        >{{ tb.label }}</button>
      </div>

      <p v-if="loading" class="muted" style="margin:14px 0">Loading…</p>
      <p v-if="err" class="form-err" style="margin:10px 0">{{ err }}</p>

      <!-- 站点/资料 -->
      <div v-if="active === 'settings'" class="panel" style="margin-top:14px;max-width:1000px">
        <div class="micro">SITE</div>
        <div class="form-row">
          <div class="field"><label>站点标题</label><input v-model="settings.siteTitle" /></div>
          <div class="field"><label>站点描述</label><input v-model="settings.siteDescription" /></div>
        </div>
        <div class="micro" style="margin-top:18px">PROFILE</div>
        <div class="form-row">
          <div class="field"><label>姓名</label><input v-model="settings.profileName" /></div>
          <div class="field"><label>头衔</label><input v-model="settings.profileTitle" /></div>
        </div>
        <div class="field"><label>短简介</label><textarea v-model="settings.shortIntro"></textarea></div>
        <div class="field"><label>完整介绍</label><textarea v-model="settings.fullIntro" style="min-height:120px"></textarea></div>
        <div class="form-row">
          <div class="field">
            <label>头像</label>
            <label v-if="settings.photoUrl" class="upload-tile has-img" style="width:80px;height:80px">
              <img :src="settings.photoUrl" />
              <div class="tile-mask">替换</div>
              <input type="file" accept="image/*" @change="pickUpload('photoUrl', settings, $event)" hidden />
            </label>
            <label v-else class="upload-tile empty" style="width:80px;height:80px">
              <span class="plus">+</span>
              <small>上传</small>
              <input type="file" accept="image/*" @change="pickUpload('photoUrl', settings, $event)" hidden />
            </label>
          </div>
          <div class="field"><label>位置</label><input v-model="settings.location" /></div>
        </div>
        <div class="form-row">
          <div class="field"><label>GitHub</label><input v-model="settings.githubUrl" /></div>
          <div class="field"><label>个人网站</label><input v-model="settings.websiteUrl" /></div>
        </div>
        <div class="micro" style="margin-top:18px">SWITCHES</div>
        <div class="switches-row">
          <div class="switch-row">
            <span>允许注册</span>
            <button class="switch" :class="{ on: settings.registrationEnabled }" @click="settings.registrationEnabled = !settings.registrationEnabled">
              <span class="knob"></span>
            </button>
          </div>
          <div class="switch-row">
            <span>开启留言</span>
            <button class="switch" :class="{ on: settings.guestbookEnabled }" @click="settings.guestbookEnabled = !settings.guestbookEnabled">
              <span class="knob"></span>
            </button>
          </div>
          <div class="switch-row">
            <span>开启评论</span>
            <button class="switch" :class="{ on: settings.commentEnabled }" @click="settings.commentEnabled = !settings.commentEnabled">
              <span class="knob"></span>
            </button>
          </div>
        </div>
        <button class="cta" style="margin-top:14px" @click="saveSettings">保存设置</button>
      </div>

      <!-- 用户管理 -->
      <div v-else-if="active === 'users'" class="panel" style="margin-top:14px;max-width:1100px">
        <div class="micro">USERS</div>
        <div class="search-row">
          <input v-model="userSearch.keyword" placeholder="搜索 ID / 昵称" @keyup.enter="userPage.page=1; loadList()" />
          <input type="date" v-model="userSearch.startDate" title="注册开始日期" />
          <input type="date" v-model="userSearch.endDate" title="注册结束日期" />
          <button class="cta" @click="userPage.page=1; loadList()">搜索</button>
        </div>

        <div class="table-wrap">
        <table v-if="list.length" class="admin-table">
          <thead><tr>
            <th>头像</th><th>用户 ID</th><th>昵称</th><th>角色</th><th>状态</th><th>注册时间</th><th>操作</th>
          </tr></thead>
          <tbody>
            <tr v-for="row in list" :key="row.id">
              <td>
                <div class="cell-avatar">
                  <img v-if="row.avatarUrl" :src="row.avatarUrl" />
                  <span v-else class="fallback">{{ (row.nickname||'?')[0] }}</span>
                </div>
              </td>
              <td><span class="cell-ellipsis">{{ row.userId }}</span></td>
              <td><span class="cell-ellipsis">{{ row.nickname }}</span></td>
              <td>{{ row.role }}</td>
              <td>
                <span class="badge" :class="row.status === 1 ? 'on' : 'off'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td class="nowrap">{{ row.createdAt ? fmtDateTime(row.createdAt) : '-' }}</td>
              <td class="ops">
                <button class="mini" @click="userEditing = { ...row }">编辑</button>
                <button class="mini danger" @click="deleteUser(row)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="muted">暂无数据</p>
        </div>

        <div v-if="userPage.totalPages > 1" class="pager">
          <button class="mini" :disabled="userPage.page <= 1" @click="userPage.page--; loadList()">上一页</button>
          <span>{{ userPage.page }} / {{ userPage.totalPages }}</span>
          <button class="mini" :disabled="userPage.page >= userPage.totalPages" @click="userPage.page++; loadList()">下一页</button>
        </div>
      </div>

      <!-- 评论 / 留言 / 建议 -->
      <div v-else-if="['comments','guestbook','suggestions'].includes(active)" class="panel" style="margin-top:14px;max-width:1100px">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px">
          <div class="micro">{{ active.toUpperCase() }}</div>
          <button v-if="selectedIds.size" class="mini danger" @click="batchDelete">删除选中 ({{ selectedIds.size }})</button>
        </div>

        <div class="table-wrap">
        <table v-if="list.length" class="admin-table">
          <thead><tr>
            <th style="width:36px">
              <input type="checkbox" :checked="selectedIds.size === list.length" @change="toggleSelectAll" />
            </th>
            <th>用户</th>
            <th v-if="active === 'comments'">项目</th>
            <th v-if="active === 'suggestions'">标题</th>
            <th>内容</th>
            <th>时间</th>
          </tr></thead>
          <tbody>
            <tr v-for="row in list" :key="row.id" class="clickable" @click="detailItem = row">
              <td @click.stop>
                <input type="checkbox" :checked="selectedIds.has(row.id)" @change="toggleSelect(row.id)" />
              </td>
              <td>
                <div class="user-cell">
                  <div class="cell-avatar sm">
                    <img v-if="row.avatarUrl" :src="row.avatarUrl" />
                    <span v-else class="fallback">{{ (row.nickname||'?')[0] }}</span>
                  </div>
                  <div>
                    <div class="cell-ellipsis">{{ row.nickname }}</div>
                    <small v-if="row.replyToNickname" class="reply-tag">回复 {{ row.replyToNickname }}</small>
                    <small class="muted">{{ row.account || row.userId }}</small>
                  </div>
                </div>
              </td>
              <td v-if="active === 'comments'"><span class="cell-ellipsis">{{ row.projectName || '#' + row.projectId }}</span></td>
              <td v-if="active === 'suggestions'"><span class="cell-ellipsis">{{ row.title }}</span></td>
              <td><span class="cell-ellipsis" :title="row.content">{{ row.content }}</span></td>
              <td class="nowrap">{{ row.createdAt ? fmtDateTime(row.createdAt) : '-' }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else class="muted">暂无数据</p>
        </div>
      </div>

      <!-- 其他资源（项目/经历/技术栈/文章/成就） -->
      <div v-else class="panel" style="margin-top:14px;max-width:1100px">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px">
          <div class="micro">{{ active.toUpperCase() }}</div>
          <button v-if="schemas[active]" class="cta add-btn" @click="newItem">+ 新增</button>
        </div>

        <div class="table-wrap">
        <table v-if="list.length" class="admin-table">
          <thead><tr>
            <th v-for="f in (schemas[active] || [{k:'id',l:'ID'},{k:'nickname',l:'昵称'},{k:'content',l:'内容'},{k:'createdAt',l:'时间'}]).filter(x => x.type !== 'image' && x.type !== 'multiselect')" :key="f.k">{{ f.l }}</th>
            <th>操作</th>
          </tr></thead>
          <tbody>
            <tr v-for="row in list" :key="row.id">
              <td v-for="f in (schemas[active] || [{k:'id'},{k:'nickname'},{k:'content'},{k:'createdAt'}]).filter(x => x.type !== 'image' && x.type !== 'multiselect')" :key="f.k">
                <span class="cell-ellipsis" :class="{ narrow: f.k === 'slug', xnarrow: f.k === 'sortOrder', desc: f.k === 'summary' || f.k === 'description' }">
                  {{ typeof row[f.k] === 'object' ? '' : (/_at$|At$/.test(f.k) && row[f.k] ? fmtDateTime(row[f.k]) : row[f.k]) }}
                  <span class="tooltip"><span class="tooltip-inner">{{ typeof row[f.k] === 'object' ? '' : (/_at$|At$/.test(f.k) && row[f.k] ? fmtDateTime(row[f.k]) : row[f.k]) }}</span></span>
                </span>
              </td>
              <td class="ops">
                <button class="mini" v-if="schemas[active]" @click="openEdit(row)">编辑</button>
                <button class="mini danger" @click="askRemove(row)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="muted">暂无数据</p>
        </div>

        <!-- 编辑弹层 -->
        <Teleport to="body">
          <div v-if="editing" class="edit-mask" @click.self="editing = null">
            <div class="edit-box">
              <h3>{{ isNew ? '新增' : '编辑' }} {{ active }}</h3>
              <div class="edit-grid">
                <div v-for="f in schemas[active]" :key="f.k" class="field" :class="{ span2: f.wide || f.type === 'textarea' }">
                  <label>{{ f.l }}</label>
                  <!-- 开关 -->
                  <div v-if="f.type === 'switch'" class="switch-inline">
                    <button class="switch" :class="{ on: editing[f.k] === f.options[1] }" @click="editing[f.k] = editing[f.k] === f.options[1] ? f.options[0] : f.options[1]">
                      <span class="knob"></span>
                    </button>
                    <small>{{ editing[f.k] === f.options[1] ? '已发布' : '草稿' }}</small>
                  </div>
                  <!-- 多选下拉（点击切换） -->
                  <div v-else-if="f.type === 'multiselect'" class="multi-select" :class="{ open: techOpen }">
                    <div class="ms-trigger" @click="techOpen = !techOpen">
                      <span v-if="(editing.skill_ids || []).length" class="ms-value">{{ selectedSkillNames }}</span>
                      <span v-else class="ms-placeholder">请选择</span>
                      <span class="ms-arrow">▾</span>
                    </div>
                    <div v-if="techOpen" class="ms-options" @click.stop>
                      <div v-for="o in skillOptions" :key="o.id"
                        class="ms-option" :class="{ on: (editing.skill_ids || []).includes(o.id) }"
                        @click="toggleSkillId(o.id)">
                        {{ o.name }}
                      </div>
                      <div v-if="!skillOptions.length" class="ms-empty">暂无技术栈</div>
                    </div>
                  </div>
                  <!-- 下拉 -->
                  <select v-else-if="f.type === 'select'" v-model="editing[f.k]">
                    <option v-for="o in f.options" :key="o" :value="o">{{ o }}</option>
                  </select>
                  <!-- 自定义单选下拉 -->
                  <div v-else-if="f.type === 'singleselect'" class="multi-select" :class="{ open: singleOpenKey === f.k }">
                    <div class="ms-trigger" @click="singleOpenKey = singleOpenKey === f.k ? '' : f.k">
                      <span v-if="editing[f.k]" class="ms-value">{{ editing[f.k] }}</span>
                      <span v-else class="ms-placeholder">请选择</span>
                      <span class="ms-arrow">▾</span>
                    </div>
                    <div v-if="singleOpenKey === f.k" class="ms-options" @click.stop>
                      <div v-for="o in f.options" :key="o"
                        class="ms-option" :class="{ on: editing[f.k] === o }"
                        @click="editing[f.k] = o; singleOpenKey = ''">
                        {{ o }}
                      </div>
                    </div>
                  </div>
                  <!-- 图片上传：方块 tile -->
                  <div v-else-if="f.type === 'image'" class="upload-tile-wrap">
                    <label v-if="editing[f.k]" class="upload-tile has-img">
                      <img :src="editing[f.k]" />
                      <div class="tile-mask">点击替换</div>
                      <input type="file" accept="image/*" @change="pickUpload(f.k, editing, $event)" hidden />
                    </label>
                    <label v-else class="upload-tile empty">
                      <span class="plus">+</span>
                      <small>点击上传</small>
                      <input type="file" accept="image/*" @change="pickUpload(f.k, editing, $event)" hidden />
                    </label>
                  </div>
                  <!-- 文本域 -->
                  <textarea v-else-if="f.type === 'textarea'" v-model="editing[f.k]"></textarea>
                  <!-- 默认输入 -->
                  <input v-else :type="f.type || 'text'" v-model="editing[f.k]" :class="{ 'short-input': f.short }" />
                </div>
              </div>
              <div class="edit-actions">
                <button class="outline" @click="editing = null">取消</button>
                <button class="cta" @click="saveItem">保存</button>
              </div>
            </div>
          </div>
        </Teleport>
      </div>

      <!-- 用户编辑弹窗 -->
      <Teleport to="body">
        <div v-if="userEditing" class="edit-mask" @click.self="userEditing = null">
          <div class="edit-box" style="width:min(480px,94vw)">
            <h3>编辑用户</h3>
            <div class="user-edit-grid">
              <div class="ue-avatar">
                <img v-if="userEditing.avatarUrl" :src="userEditing.avatarUrl" />
                <span v-else class="fallback">{{ (userEditing.nickname||'?')[0] }}</span>
              </div>
              <div class="ue-info">
                <div class="ue-row"><label>用户 ID</label><span>{{ userEditing.userId }}</span></div>
                <div class="ue-row"><label>昵称</label><span>{{ userEditing.nickname }}</span></div>
                <div class="ue-row"><label>角色</label><span>{{ userEditing.role }}</span></div>
                <div class="ue-row"><label>注册时间</label><span>{{ userEditing.createdAt ? fmtDateTime(userEditing.createdAt) : '-' }}</span></div>
                <div class="ue-row"><label>更新时间</label><span>{{ userEditing.updatedAt ? fmtDateTime(userEditing.updatedAt) : '-' }}</span></div>
              </div>
            </div>
            <div class="ue-status">
              <label>账号状态</label>
              <div class="status-btns">
                <button class="mini" :class="{ on: userEditing.status === 1 }" @click="userEditing.status = 1">启用</button>
                <button class="mini danger" :class="{ on: userEditing.status === 0 }" @click="userEditing.status = 0">禁用</button>
              </div>
            </div>
            <div class="edit-actions">
              <button class="outline" @click="userEditing = null">取消</button>
              <button class="mini danger" style="margin-right:auto" @click="deleteUser(userEditing); userEditing = null">删除用户</button>
              <button class="cta" @click="saveUserStatus">保存</button>
            </div>
          </div>
        </div>
      </Teleport>

      <!-- 评论/留言/建议 详情弹窗 -->
      <Teleport to="body">
        <div v-if="detailItem" class="edit-mask" @click.self="detailItem = null">
          <div class="detail-box">
            <button class="close-x" @click="detailItem = null">×</button>
            <div class="detail-head">
              <div class="detail-avatar">
                <img v-if="detailItem.avatarUrl" :src="detailItem.avatarUrl" />
                <span v-else class="fallback">{{ (detailItem.nickname||'?')[0] }}</span>
              </div>
              <div>
                <div class="detail-name">{{ detailItem.nickname }}</div>
                <small v-if="detailItem.replyToNickname" class="reply-tag">回复 {{ detailItem.replyToNickname }}</small>
                <small class="muted">@{{ detailItem.account || detailItem.userId }} · {{ detailItem.createdAt ? fmtDateTime(detailItem.createdAt) : '' }}</small>
              </div>
            </div>
            <div v-if="active === 'comments' && detailItem.projectName" class="detail-project">
              <span class="muted">来自项目：</span>
              <router-link :to="'/projects/' + detailItem.projectId" class="proj-link">{{ detailItem.projectName }}</router-link>
            </div>
            <div v-if="active === 'suggestions' && detailItem.title" class="detail-title">{{ detailItem.title }}</div>
            <div class="detail-body">{{ detailItem.content }}</div>

            <div v-if="!replyMode" class="dm-actions">
              <button v-if="active !== 'suggestions'" class="dm-btn" @click="replyMode = true">回复</button>
              <button v-if="Number(detailItem.userId) !== Number(uState.user?.id)" class="dm-btn" @click="openPrivate">私聊</button>
            </div>
            <div v-if="replyMode" class="dm-reply">
              <textarea v-model="replyDraft" placeholder="写回复…" rows="3"></textarea>
              <div class="dm-reply-ops">
                <button class="dm-btn ghost" @click="replyMode = false; replyDraft = ''">取消</button>
                <button class="dm-btn primary" @click="submitReply">发送回复</button>
              </div>
            </div>
          </div>
        </div>
      </Teleport>
    </div>

    <div v-else class="panel" v-reveal style="margin-top:20px;max-width:760px">
      <p class="muted">{{ t('admin.denied') }}</p>
    </div>
  </main>
</template>

<style scoped>
.tab-bar { display:flex; flex-wrap:wrap; gap:8px; }
.tab {
  padding:8px 14px; border:1px solid var(--line); border-radius:999px;
  background:rgba(255,255,255,.6); font-size:13px; cursor:pointer; transition:.2s;
}
.tab.on { background:var(--ink); color:#fff; border-color:var(--ink); }
.admin-table { width:100%; border-collapse:collapse; font-size:13px; margin-top:8px; }
.admin-table th, .admin-table td { padding:12px 10px; text-align:left; vertical-align:middle; border-bottom:none; }
.admin-table th { color:var(--muted); font-weight:600; font-size:12px; white-space:nowrap; }
.admin-table .ops { white-space:nowrap; text-align:right; width:1%; }
.admin-table .num { white-space:nowrap; text-align:right; width:1%; }

.table-wrap { overflow-x: auto; }
.table-wrap table { width: 100%; }
.mini { padding:4px 10px; border:1px solid var(--line); background:#fff; border-radius:8px; cursor:pointer; font-size:12px; margin-right:4px; }
.mini.danger { color:#c0574f; border-color:#ecc7c2; }
.edit-mask {
  position: fixed; inset: 0;
  background: rgba(245, 247, 245, .55);
  backdrop-filter: blur(6px);
  display: flex; align-items: flex-start; justify-content: center;
  z-index: 300; overflow-y: auto; padding: 40px 16px;
}
.edit-box {
  width: min(780px, 94vw);
  background: #faf9f5;
  border: 1px solid #e0ebef;
  border-radius: 20px; padding: 28px;
  box-shadow: 0 24px 70px rgba(40, 75, 90, .18);
}
.edit-box h3 { margin-bottom:14px; }
.edit-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 14px 18px;
}
.edit-grid .span2 { grid-column: span 3; }
.edit-grid .field:has(.short-input) input { max-width: 90px; }
.edit-grid .field label { display: block; margin-bottom: 6px; }
.multi-select .ms-trigger { min-height: 40px; }
.multi-select { position: relative; margin-top: 10px; }
.multi-select .ms-trigger {
  display: flex; align-items: center; justify-content: space-between;
  padding: 9px 12px; border: 1px solid var(--line); border-radius: 10px;
  background: #fff; cursor: pointer; font-size: 13px; min-height: 40px;
}
.multi-select .ms-placeholder { color: #9aa; }
.multi-select .ms-arrow { color: #9aa; font-size: 12px; }
.multi-select .ms-options {
  position: absolute; left: 0; right: 0; top: calc(100% + 4px); z-index: 20;
  background: #fff; border: 1px solid var(--line); border-radius: 10px;
  max-height: 180px; overflow-y: auto; box-shadow: 0 10px 30px rgba(0,0,0,.1);
}
.multi-select .ms-option {
  padding: 8px 12px; font-size: 13px; cursor: pointer;
}
.multi-select .ms-option:hover { background: rgba(157,221,215,.2); }
.multi-select .ms-option.on { background: rgba(157,221,215,.35); color: var(--ink); font-weight: 600; }
.multi-select .ms-empty { padding: 12px; color: var(--muted); font-size: 12px; text-align: center; }
@media (max-width: 720px) {
  .edit-grid { grid-template-columns: 1fr; }
  .edit-grid .span2 { grid-column: span 1; }
}
.switches-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 14px;
  margin-top: 10px;
}
.switch-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 18px; border: 1px solid var(--line); border-radius: 16px;
  background: rgba(255,255,255,.6); font-size: 14px; color: var(--ink);
}
.switch {
  width: 64px; height: 34px; border-radius: 999px;
  background: #c9d8dd; border: none; cursor: pointer; position: relative;
  transition: background .25s; padding: 0; flex-shrink: 0;
}
.switch .knob {
  position: absolute; top: 4px; left: 4px; width: 26px; height: 26px;
  border-radius: 50%; background: #fff; box-shadow: 0 2px 6px rgba(0,0,0,.18);
  transition: transform .25s;
}
.switch.on { background: #71b9df; }
.switch.on .knob { transform: translateX(30px); }
.upload-tile-wrap { margin-top: 4px; }
.upload-tile {
  position: relative; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 4px;
  width: 110px; height: 110px; border-radius: 14px; cursor: pointer;
  overflow: hidden; transition: .2s;
}
.upload-tile.empty {
  border: 1.5px dashed #b7dce8; background: rgba(255,255,255,.5); color: #6d9aab;
}
.upload-tile.empty:hover { border-color: #71b9df; background: #f0f8fb; }
.upload-tile .plus { font-size: 28px; font-weight: 300; line-height: 1; }
.upload-tile small { font-size: 11px; letter-spacing: .05em; }
.upload-tile.has-img {
  border: 1px solid var(--line); padding: 0;
}
.upload-tile.has-img img { width: 100%; height: 100%; object-fit: cover; }
.upload-tile .tile-mask {
  position: absolute; inset: 0; background: rgba(36,52,61,.55);
  color: #fff; font-size: 12px; display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: .2s;
}
.upload-tile.has-img:hover .tile-mask { opacity: 1; }
.upload-row { display:flex; gap:8px; align-items:center; }
.upload-row input { flex:1; }
.upload-btn {
  padding: 8px 12px; border:1px solid var(--line); border-radius:10px;
  background:#fff; font-size:12px; cursor:pointer; white-space:nowrap;
}
.upload-btn:hover { border-color:#b7dce8; }
.upload-preview { margin-top:8px; max-width:140px; max-height:90px; border-radius:10px; object-fit:cover; border:1px solid var(--line); }
.switch-inline { display:flex; align-items:center; gap:10px; padding: 12px 0; }
.switch-inline small { color:var(--muted); font-size:12px; }
select {
  width: 100%; padding: 12px; border:1px solid var(--line); border-radius:12px;
  background:#fff; font: inherit; color: var(--ink); margin: 6px 0 14px;
}
select:focus { outline:none; border-color: var(--blue2); box-shadow: 0 0 0 3px rgba(113,185,223,.15); }
.edit-actions { margin-top:16px; display:flex; gap:10px; justify-content:flex-end; }
@media (max-width: 720px) {
  .edit-actions { flex-direction: row; justify-content: flex-end; }
  .edit-actions .cta, .edit-actions .outline { width: auto; margin-left: 8px; padding: 9px 18px; font-size: 13px; }
  .switches-row { grid-template-columns: 1fr 1fr 1fr; gap: 8px; }
  .switch-row { padding: 10px 8px; font-size: 11px; flex-direction: column; gap: 8px; text-align: center; }
  .switch { width: 44px; height: 24px; }
  .switch .knob { width: 18px; height: 18px; top: 3px; left: 3px; }
  .switch.on .knob { transform: translateX(20px); }
  .panel .form-row { grid-template-columns: 1fr 1fr; gap: 10px; }
  .panel .field label { font-size: 11px; }
  .panel input, .panel textarea { padding: 9px 11px; font-size: 13px; }
  .edit-grid { grid-template-columns: 1fr; gap: 10px 12px; }
  .edit-grid .span2 { grid-column: span 1; }
  .edit-box { padding: 18px; border-radius: 16px; }
  .edit-box label { font-size: 11px; }
  .edit-box input, .edit-box textarea { padding: 9px 11px; font-size: 13px; }
  .upload-tile { width: 84px; height: 84px; }
  .upload-tile .plus { font-size: 22px; }
  .save-main { width: 100%; }

  /* 移动端表格：缩小内边距、允许横向滚动、缩短省略号宽度 */
  .admin-table th, .admin-table td { padding: 8px 6px; font-size: 12px; }
  .admin-table th { font-size: 11px; }
  .admin-table .cell-ellipsis { max-width: 80px; }
  .admin-table .cell-ellipsis.narrow { max-width: 60px; }
  .admin-table .cell-ellipsis.xnarrow { max-width: 36px; }
  .admin-table .cell-ellipsis.desc { max-width: 60px; }
  .admin-table .cell-ellipsis .tooltip { max-width: 200px; font-size: 11px; }
  .cell-avatar { width: 26px; height: 26px; font-size: 11px; }
  .cell-avatar.sm { width: 24px; height: 24px; }
  .user-cell { gap: 6px; }
  .search-row input { min-width: 100px; padding: 8px 10px; font-size: 13px; }
  .search-row .cta { padding: 8px 14px; font-size: 13px; }
  .detail-box { padding: 18px; }
  .user-edit-grid { flex-direction: column; }
  .edit-mask { padding: 16px 8px; }
}

/* ===== 新增样式 ===== */
.search-row { display: flex; gap: 8px; flex-wrap: wrap; margin: 12px 0; }
.search-row input {
  flex: 1; min-width: 140px; padding: 9px 12px;
  border: 1px solid var(--line); border-radius: 10px; background: #fff; font: inherit;
}
.search-row .cta { padding: 9px 18px; }
.add-btn { width: auto !important; padding: 8px 16px !important; font-size: 12px !important; flex-shrink: 0; }

.admin-table .cell-ellipsis {
  position: relative;
  display: inline-block; max-width: 200px; overflow: hidden;
  text-overflow: ellipsis; white-space: nowrap; vertical-align: middle;
  cursor: default;
}
.admin-table .cell-ellipsis .tooltip {
  display: none;
  position: absolute; left: 0; top: calc(100% + 6px); z-index: 50;
  background: #24343d; color: #fff; padding: 6px 10px; border-radius: 8px;
  font-size: 12px; white-space: nowrap; max-width: 320px; overflow: hidden;
  box-shadow: 0 6px 20px rgba(0,0,0,.2); pointer-events: none;
}
.admin-table .cell-ellipsis:hover .tooltip { display: block; }
.admin-table .cell-ellipsis .tooltip .tooltip-inner {
  display: inline-block; padding-right: 40px;
  animation: scroll-left 8s linear infinite;
}
@keyframes scroll-left {
  0% { transform: translateX(0); }
  100% { transform: translateX(calc(-100% + 280px)); }
}
.admin-table .cell-ellipsis.short .tooltip .tooltip-inner { animation: none; }

/* 特定字段更窄 */
.admin-table .cell-ellipsis.narrow { max-width: 140px; }
.admin-table .cell-ellipsis.xnarrow { max-width: 90px; }
.admin-table .cell-ellipsis.desc { max-width: 100px; }
.admin-table .nowrap { white-space: nowrap; font-size: 12px; color: var(--muted); }

.admin-table .clickable { cursor: pointer; transition: background .15s; border-radius: 8px; }
.admin-table .clickable:hover { background: rgba(161,219,215,.15); }

.cell-avatar {
  width: 32px; height: 32px; border-radius: 50%; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg,#a9d8f5,#71b9df); color: #fff; font-size: 13px;
}
.cell-avatar img { width: 100%; height: 100%; object-fit: cover; }
.cell-avatar.sm { width: 28px; height: 28px; font-size: 11px; }

.user-cell { display: flex; align-items: center; gap: 8px; }
.user-cell small { display: block; font-size: 11px; }
.admin-table td input[type="checkbox"] { margin: 0; vertical-align: middle; cursor: pointer; }

.badge {
  display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 11px;
}
.badge.on { background: #d6efe8; color: #3a8a7a; }
.badge.off { background: #f3d6d2; color: #c0574f; }

.pager { display: flex; justify-content: center; align-items: center; gap: 12px; margin-top: 14px; }
.pager .mini:disabled { opacity: .4; cursor: not-allowed; }

.user-edit-grid { display: flex; gap: 16px; align-items: flex-start; margin: 10px 0 16px; }
.ue-avatar {
  width: 64px; height: 64px; border-radius: 50%; overflow: hidden; flex-shrink: 0;
  background: linear-gradient(135deg,#a9d8f5,#71b9df); color: #fff;
  display: flex; align-items: center; justify-content: center; font-size: 26px;
}
.ue-avatar img { width: 100%; height: 100%; object-fit: cover; }
.ue-info { flex: 1; display: flex; flex-direction: column; gap: 6px; }
.ue-row { display: flex; justify-content: space-between; font-size: 13px; }
.ue-row label { color: var(--muted); }
.ue-status { display: flex; align-items: center; justify-content: space-between; padding: 12px 0; border-top: 1px solid var(--line); }
.status-btns { display: flex; gap: 8px; }
.status-btns .mini.on { background: var(--ink); color: #fff; border-color: var(--ink); }

.detail-box {
  width: min(640px, 94vw); background: #faf9f5;
  border-radius: 20px; padding: 28px; position: relative;
  box-shadow: 0 24px 70px rgba(40,75,90,.18);
}
.detail-box .close-x {
  position: absolute; top: 14px; right: 16px; background: none; border: none;
  font-size: 22px; cursor: pointer; color: var(--muted);
}
.detail-head { display: flex; gap: 12px; align-items: center; margin-bottom: 14px; }
.detail-avatar {
  width: 48px; height: 48px; border-radius: 50%; overflow: hidden;
  background: linear-gradient(135deg,#a9d8f5,#71b9df); color: #fff;
  display: flex; align-items: center; justify-content: center; font-size: 20px;
}
.detail-avatar img { width: 100%; height: 100%; object-fit: cover; }
.detail-name { font-weight: 600; }
.detail-project { margin-bottom: 10px; font-size: 13px; }
.proj-link { color: var(--blue2); text-decoration: underline; }
.detail-title { font-weight: 600; margin-bottom: 8px; }
.detail-body {
  padding: 16px; background: rgba(255,255,255,.6); border-radius: 12px;
  line-height: 1.7; white-space: pre-wrap;
}
.dm-actions { display: flex; gap: 8px; margin-top: 14px; flex-wrap: wrap; }
.dm-btn {
  padding: 7px 16px; border-radius: 10px; font-size: 12px; cursor: pointer;
  border: 1px solid var(--line); background: #fff; color: var(--ink);
}
.dm-btn:hover { border-color: #b7dce8; }
.dm-btn.ghost { color: var(--muted); }
.dm-btn.primary { background: var(--ink); color: #fff; border-color: var(--ink); }
.dm-reply { margin-top: 14px; }
.dm-reply textarea {
  width: 100%; padding: 10px 12px; border: 1px solid var(--line);
  border-radius: 10px; background: #fff; font-size: 13px; resize: vertical;
  font-family: inherit;
}
.dm-reply-ops { display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px; }
.reply-tag { color: var(--blue2); font-size: 11px; margin: 0 4px; }


</style>
