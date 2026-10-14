<script setup>
import { onMounted, ref } from 'vue'
import { useI18n } from '../i18n/index'
import request from '../api/request'

const { t } = useI18n()

// 图标沿用线性 24 视框、1.6 描边、currentColor
const ICONS = {
  profile: { circles: [[12, 8, 4]], paths: ['M4.6 21v-1.1A5.9 5.9 0 0 1 10.5 14h3a5.9 5.9 0 0 1 5.9 5.9V21'] },
  experience: { rects: [[3, 7, 18, 13, 2.5]], paths: ['M8.5 7V5.6A1.6 1.6 0 0 1 10.1 4h3.8a1.6 1.6 0 0 1 1.6 1.6V7', 'M3 12.6h18'] },
  technology: { paths: ['M8.6 17.6 3.4 12l5.2-5.6', 'M15.4 6.4 20.6 12l-5.2 5.6', 'M13.7 4.4 10.3 19.6'] },
  projects: { rects: [[3.5, 3.5, 7, 7, 2], [13.5, 3.5, 7, 7, 2], [3.5, 13.5, 7, 7, 2], [13.5, 13.5, 7, 7, 2]] },
  achievements: { paths: ['M8 4h8v6a4 4 0 0 1-8 0V4z', 'M16 5.6h2.2a2.2 2.2 0 0 1 0 4.4H16', 'M8 5.6H5.8a2.2 2.2 0 0 0 0 4.4H8', 'M12 14v6.5', 'M8.5 20.5h7'] },
  blog: { rects: [[4.6, 3, 14.8, 18, 2.6]], paths: ['M8.4 8.5h7.2', 'M8.4 12.5h7.2', 'M8.4 16.5h4.4'] },
  guestbook: { rects: [[3.2, 4.8, 17.6, 12.4, 2.8]], paths: ['M8 17.2v3.9l4.2-3.9', 'M9 11h6'] },
  contact: { rects: [[3, 5.4, 18, 13.2, 2.6]], paths: ['M3.9 7.2 12 13.1l8.1-5.9'] }
}

// 跳转路径与原实现完全一致，未做任何改动
const cards = [
  { to: '/profile', title: 'explore.t01', label: 'explore.s01', icon: 'profile' },
  { to: '/experience', title: 'explore.t02', label: 'explore.s02', icon: 'experience' },
  { to: '/skills', title: 'explore.t03', label: 'explore.s03', icon: 'technology' },
  { to: '/projects', title: 'explore.t04', label: 'explore.s04', icon: 'projects' },
  { to: '/achievements', title: 'explore.t05', label: 'explore.s05', icon: 'achievements' },
  { to: '/blog', title: 'explore.t06', label: 'explore.s06', icon: 'blog' },
  { to: '/guestbook', title: 'explore.t07', label: 'explore.s07', icon: 'guestbook' },
  { to: '/contact', title: 'explore.t08', label: 'explore.s08', icon: 'contact' }
]

// 最新留言：与经历时间轴同构的一行（正文 + 署名·日期）。
// 走公开的 GET /api/guestbook；刻意不用 content.js 的 getGuestbook（它带 auth:true），
// 否则未登录访客进首页会触发登录弹窗。
const notes = ref([])
onMounted(async () => {
  try {
    const d = await request('/guestbook?page=1&size=3')
    notes.value = (d.records || []).slice(0, 3)
  } catch (e) { /* 取不到就不渲染这一行，不打扰访客 */ }
})

function fmtDay(s) {
  return (s || '').slice(0, 10).replace(/-/g, '.')
}

function who(n) {
  return n.nickname || n.account || t('home.guest')
}

function toTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <section id="explore" class="explore-sec" v-reveal>
    <div class="ex-main">
      <div class="section-head">
        <div>
          <div class="eyebrow">{{ t('explore.eyebrow') }}</div>
          <h2 class="title">{{ t('explore.title') }}</h2>
        </div>
        <p class="lead">{{ t('explore.lead') }}</p>
      </div>
      <div class="explore">
        <router-link v-for="c in cards" :key="c.to" class="ex-card" :to="c.to" v-tilt>
          <span class="ico">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
              <circle v-for="(ci, k) in (ICONS[c.icon].circles || [])" :key="'c' + k" :cx="ci[0]" :cy="ci[1]" :r="ci[2]" />
              <rect
                v-for="(r, k) in (ICONS[c.icon].rects || [])"
                :key="'r' + k"
                :x="r[0]" :y="r[1]" :width="r[2]" :height="r[3]" :rx="r[4] || 0"
              />
              <path v-for="(d, k) in (ICONS[c.icon].paths || [])" :key="'p' + k" :d="d" />
            </svg>
          </span>
          <span class="bwrap">
            <b>{{ t(c.title) }}</b>
            <small>{{ t(c.label) }}</small>
          </span>
          <span class="barrow">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 12h15" /><path d="M13 6l6 6-6 6" /></svg>
          </span>
        </router-link>
      </div>
    </div>

    <!-- 最新留言（与经历时间轴同构：竖线分隔的纯文字节点）+ 回到顶部 -->
    <div class="ex-foot">
      <div v-if="notes.length" class="notes">
        <div class="nhead">
          <span class="eyebrow">{{ t('home.wall') }}</span>
          <router-link class="more" to="/guestbook">{{ t('home.wallMore') }} ↗</router-link>
        </div>
        <ul class="nlist">
          <li v-for="n in notes" :key="n.id">
            <router-link class="nnode" to="/guestbook">
              <p><i class="dot"></i><span class="txt" :title="n.content">{{ n.content }}</span></p>
              <span>{{ who(n) }} · {{ fmtDay(n.createdAt) }}</span>
            </router-link>
          </li>
        </ul>
      </div>

      <button type="button" class="next" @click="toTop">
        <span>{{ t('home.top') }}</span>
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 19V5" /><path d="M6 11l6-6 6 6" /></svg>
      </button>
    </div>
  </section>
</template>

<style scoped>
/* 区域 3：整组（标题 + 8 卡 + 留言行 + 回到顶部）在屏内居中；
   留言行紧贴卡组下方 26px，不再钉屏底（原来那里留了 298px 空洞） */
.explore-sec {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 26px;
  padding: 0;
}

.ex-main { flex: 0 0 auto; display: flex; flex-direction: column; }
.ex-foot { flex: 0 0 auto; display: flex; flex-direction: column; gap: 16px; }

.ex-card {
  position: relative;
  overflow: hidden;
  padding: 21px 22px;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 15px;
  border: 1px solid var(--line);
  border-radius: 16px;
  background: rgba(255, 255, 255, .8);
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: transform .3s cubic-bezier(.2, .7, .2, 1), border-color .3s, box-shadow .3s;
}

/* hover 时左侧展开的薄荷色竖条 */
.ex-card::after {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, #7dc9b8, #a8ddd2);
  transform: scaleY(0);
  transform-origin: top;
  transition: transform .3s;
}

.ex-card:hover {
  transform: translateY(-3px);
  border-color: #d2e9e3;
  box-shadow: 0 24px 58px rgba(52, 93, 105, .16);
}
.ex-card:hover::after { transform: scaleY(1); }

.ico { position: relative; display: grid; place-items: center; flex: 0 0 auto; width: 22px; height: 22px; color: var(--ink); }
.ico svg { display: block; overflow: visible; }

.bwrap { position: relative; display: flex; flex-direction: column; gap: 1px; flex: 1 1 auto; min-width: 0; }
.bwrap b { font-size: 16px; font-weight: 800; letter-spacing: -.02em; line-height: 1.3; color: var(--ink); }
.bwrap small { font-size: 12.5px; color: var(--muted); line-height: 1.45; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.barrow {
  position: relative;
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  color: var(--muted);
  transition: color .3s, transform .3s;
}
.ex-card:hover .barrow { color: #7dc9b8; transform: translateX(4px); }

/* 最新留言：与经历时间轴同一套排版（眉标 + 竖线分隔的三栏纯文字节点） */
.notes { border-top: 1px solid var(--line); padding-top: 16px; }
.nhead { display: flex; justify-content: space-between; align-items: center; gap: 18px; margin-bottom: 14px; }
.more { display: inline-flex; align-items: center; gap: 7px; font-size: 12.5px; color: var(--muted); transition: color .25s; }
.more:hover { color: var(--ink); }
.nlist { display: flex; list-style: none; margin: 0; padding: 0; }
.nlist li { flex: 1 1 0; min-width: 0; }
.nlist li + li { border-left: 1px solid var(--line); }
.nnode { display: block; padding: 0 26px; color: inherit; transition: transform .25s; }
.nlist li:first-child .nnode { padding-left: 0; }
.nnode:hover { transform: translateY(-2px); }
.nnode p { display: flex; align-items: center; margin: 0; font-size: 13.5px; line-height: 1.7; color: #3d525c; }
.nnode .dot {
  flex: 0 0 auto;
  width: 6px;
  height: 6px;
  margin-right: 9px;
  border-radius: 50%;
  background: #7dc9b8;
  box-shadow: 0 0 0 3px rgba(125, 201, 184, .18);
}
.nnode .txt { min-width: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.nnode span { display: block; margin-top: 7px; padding-left: 15px; font-size: 11.5px; color: var(--muted); }

/* 回到顶部 */
.next {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  align-self: center;
  padding: 8px 4px;
  border: 0;
  background: none;
  font: inherit;
  font-size: 10px;
  letter-spacing: .22em;
  text-transform: uppercase;
  color: var(--muted);
  cursor: pointer;
  transition: color .25s;
}
.next:hover { color: var(--ink); }
.next svg { transition: transform .25s; }
.next:hover svg { transform: translateY(-3px); }

@media (max-width: 768px) {
  .ex-card { padding: 17px 18px; }
  .bwrap b { font-size: 15px; }
  .bwrap small { font-size: 12px; }
  .nlist { flex-direction: column; }
  .nlist li + li { border-left: 0; border-top: 1px solid var(--line); }
  .nnode { padding: 12px 0; }
  .nlist li:first-child .nnode { padding-top: 0; }
  .nlist li:last-child .nnode { padding-bottom: 0; }
  .nnode p { font-size: 12.5px; }
}

/* 窄屏：8 张卡单列排布，收紧行高让整块仍占一屏 */
@media (max-width: 560px) {
  .ex-card { padding: 10px 15px; }
  .explore { gap: 10px; }
}
</style>
