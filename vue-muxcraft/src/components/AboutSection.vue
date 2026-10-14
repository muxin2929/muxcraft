<script setup>
import { onMounted, ref } from 'vue'
import { useI18n } from '../i18n/index'
import { getProjects, getExperiences } from '../api/content'

const { t, tArr } = useI18n()

// 对应 API 6.1 GET /api/site 的 stats；后端未就绪时用同形 mock
const stats = ref({ projects: 12, experiences: 8, skills: 17, articles: 24 })

// 精选项目：与项目模块同一个接口 GET /api/projects，只取前 3 条
const works = ref([])
const worksErr = ref('')

// 经历时间轴：与经历页同一个接口 GET /api/experiences，后端已按结束时间倒序
const journey = ref([])

onMounted(async () => {
  try {
    const res = await fetch('/api/site')
    const j = await res.json()
    if (j?.code === 200) stats.value = j.data.stats
  } catch (e) { /* mock 保留 */ }

  try {
    const data = await getProjects({ page: 1, size: 3 })
    works.value = data.records || []
  } catch (e) {
    worksErr.value = e.message
  }

  try {
    const d = await getExperiences({ page: 1, size: 3 })
    journey.value = d.records || []
  } catch (e) { /* 时间轴取不到就不渲染，不打扰访客 */ }
})

// 时间轴上的时间：2023.06 — 至今
function expTime(n) {
  const f = s => (s || '').slice(0, 7).replace('-', '.')
  const a = f(n.startDate)
  const b = n.endDate ? f(n.endDate) : t('home.now')
  return [a, b].filter(Boolean).join(' — ')
}

// 「查看我的项目」滚动到下一个区域（精选项目），不跳转页面
function scrollToWorks() {
  document.getElementById('works')?.scrollIntoView({ behavior: 'smooth' })
}

// 精选项目区底部的 NEXT：滚到 Explore 区
function scrollToExplore() {
  document.getElementById('explore')?.scrollIntoView({ behavior: 'smooth' })
}

function toTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <section id="about" class="about" v-reveal>
    <div class="deco-ring" aria-hidden="true"></div>

    <!-- ===== 区域 1 · 介绍 + 身份浮卡 ===== -->
    <div class="blk blk-1">
      <!-- A3｜压在首屏与介绍区交界处的描边水印，只做呼吸感 -->
      <div class="wm" aria-hidden="true">MUXCRAFT</div>

      <div class="intro">
      <div class="intro-l">
        <div class="greet">{{ t('about.greet') }}</div>
        <h2 class="intro-t">{{ t('about.introTitle') }}</h2>
        <p class="intro-p">{{ t('about.lead') }}</p>

        <div class="acts">
          <button type="button" class="btn-dark" @click="scrollToWorks">
            {{ t('about.viewProjects') }}
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 12h14" /><path d="M12 6l6 6-6 6" /></svg>
          </button>
          <router-link class="btn-soft" to="/contact">{{ t('about.contactMe') }}</router-link>
        </div>

        <div class="mini">
          <div class="m"><b>{{ stats.projects }}</b><small>{{ t('stat.projects') }}</small></div>
          <div class="m"><b>{{ String(stats.experiences).padStart(2, '0') }}</b><small>{{ t('stat.experiences') }}</small></div>
          <div class="m"><b>{{ String(stats.skills).padStart(2, '0') }}</b><small>{{ t('stat.skills') }}</small></div>
          <div class="m"><b>{{ stats.articles }}</b><small>{{ t('stat.articles') }}</small></div>
        </div>
      </div>

      <div class="intro-r">
        <div class="building"><i></i>{{ t('about.building') }}</div>
        <div class="focus-area">
          <svg class="orbit" viewBox="0 0 430 370" fill="none" aria-hidden="true">
            <ellipse cx="215" cy="185" rx="196" ry="130" transform="rotate(-30 215 185)" stroke="#d3e2e6" stroke-width="1" />
            <circle cx="92" cy="92" r="5.5" fill="#7dc9b8" />
            <circle cx="112" cy="292" r="4.5" fill="#5cbb92" />
            <circle cx="352" cy="212" r="3.5" fill="#a8ddd2" />
          </svg>
          <div class="focus">
            <div class="micro">{{ t('about.focusLabel') }}</div>
            <h3>MuX <span class="plus">+</span> Craft</h3>
            <p>{{ t('about.identity.desc') }}</p>
            <div class="chips"><span v-for="c in tArr('about.chips')" :key="c">{{ c }}</span></div>
          </div>
        </div>
      </div>
      </div>

      <!-- B2｜经历时间轴（与经历页同一接口，后端已按结束时间倒序）+ E1 下一块指示 -->
      <div class="blk-foot">
        <div v-if="journey.length" class="journey">
          <div class="jhead">
            <span class="eyebrow">{{ t('home.journey') }}</span>
            <router-link class="more" to="/experience">{{ t('home.journeyMore') }} ↗</router-link>
          </div>
          <ol class="jlist">
            <li v-for="n in journey" :key="n.id">
              <router-link class="jnode" to="/experience">
                <span class="jtime"><i></i>{{ expTime(n) }}</span>
                <b>{{ n.title }}</b>
                <small>{{ n.organization }}</small>
              </router-link>
            </li>
          </ol>
        </div>

        <button type="button" class="next" @click="scrollToWorks">
          <span>{{ t('home.next') }} · {{ t('works.title') }}</span>
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14" /><path d="M6 13l6 6 6-6" /></svg>
        </button>
      </div>
    </div>

    <!-- ===== 区域 2 · 精选项目（接口与项目模块一致） ===== -->
    <div id="works" class="blk blk-2">
      <div class="works">
      <div class="works-head">
        <div>
          <div class="eyebrow">{{ t('works.eyebrow') }}</div>
          <h2 class="title">{{ t('works.title') }}</h2>
        </div>
        <router-link class="more" to="/projects">
          {{ t('works.viewAll') }}
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M7 17 17 7" /><path d="M9 7h8v8" /></svg>
        </router-link>
      </div>

      <p v-if="worksErr" class="muted">{{ worksErr }}</p>
      <div v-else-if="!works.length" class="panel"><p class="muted">—</p></div>

      <div v-else class="works-grid">
        <router-link
          v-for="(p, i) in works"
          :key="p.id"
          class="work"
          :class="{ 'work-lg': i === 0 }"
          :to="`/projects/${p.id}`"
        >
          <div
            class="cover"
            :class="{ 'cover-dark': i === 0 }"
            :style="p.coverUrl ? { backgroundImage: `url(${p.coverUrl})` } : null"
          >
            <template v-if="!p.coverUrl">
              <div v-if="i === 0" class="win"><i></i><i></i><i></i></div>
              <span v-else-if="i === 1" class="mock-phone"></span>
              <span v-else class="mock-lap"></span>
            </template>
          </div>

          <template v-if="i === 0">
            <span class="feat">{{ p.skills?.[0]?.name || 'PROJECT' }}</span>
            <div class="lg-body">
              <h3>{{ p.name }}</h3>
              <p>{{ p.description || p.summary }}</p>
              <div class="tags"><span v-for="s in (p.skills || [])" :key="s.id">{{ s.name }}</span></div>
            </div>
            <span class="go"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M7 17 17 7" /><path d="M9 7h8v8" /></svg></span>
          </template>

          <div v-else class="wk-body">
            <h3>{{ p.name }}</h3>
            <p>{{ p.description || p.summary }}</p>
            <div class="wk-foot">
              <div class="tags"><span v-for="s in (p.skills || [])" :key="s.id">{{ s.name }}</span></div>
              <span class="go"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M7 17 17 7" /><path d="M9 7h8v8" /></svg></span>
            </div>
          </div>
        </router-link>
      </div>
      </div>

      <!-- C2｜点阵装饰带 + E1 下一块指示 -->
      <div class="blk-foot">
        <div class="dotband" aria-hidden="true"></div>
        <button type="button" class="next" @click="scrollToExplore">
          <span>{{ t('home.next') }} · {{ t('explore.title') }}</span>
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14" /><path d="M6 13l6 6 6-6" /></svg>
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* 参考图配色：薄荷绿点缀，只作用于本组件 */
.about {
  --accent: #7dc9b8;
  --accent-2: #a8ddd2;
  --accent-line: #d2e9e3;
}

/* ---------- 区域容器：每块自占一屏，顶部留装饰位、底部留时间轴与指示 ---------- */
.blk { min-height: 100vh; display: flex; flex-direction: column; position: relative; }
.blk-1 { padding: 92px 0 26px; }
.blk-2 { padding: 46px 0 26px; }

/* A3｜超大描边水印：占住首屏与介绍区之间的空档，不参与阅读 */
.wm {
  flex: 0 0 auto;
  font-size: clamp(40px, 9vw, 132px);
  font-weight: 800;
  letter-spacing: .04em;
  line-height: 1;
  text-align: center;
  color: transparent;
  -webkit-text-stroke: 1.5px rgba(113, 185, 223, .34);
  user-select: none;
  pointer-events: none;
  -webkit-mask-image: linear-gradient(180deg, rgba(0, 0, 0, .16), #000 58%, rgba(0, 0, 0, .3));
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, .16), #000 58%, rgba(0, 0, 0, .3));
}

/* 块底部：时间轴 / 装饰带 / NEXT 指示统一放这里 */
.blk-foot { flex: 0 0 auto; display: flex; flex-direction: column; gap: 16px; }

/* B2｜经历时间轴（横向 3 个节点，点击进经历页） */
.journey { border-top: 1px solid var(--line); padding-top: 16px; }
.jhead { display: flex; justify-content: space-between; align-items: center; gap: 18px; margin-bottom: 14px; }
.jlist { display: flex; list-style: none; margin: 0; padding: 0; }
.jlist li { flex: 1 1 0; min-width: 0; }
.jlist li + li { border-left: 1px solid var(--line); }
.jnode { display: block; padding: 0 26px; color: inherit; transition: transform .25s; }
.jlist li:first-child .jnode { padding-left: 0; }
.jnode:hover { transform: translateY(-2px); }
.jtime { display: inline-flex; align-items: center; gap: 8px; font-size: 10px; letter-spacing: .18em; color: #6995a6; }
.jtime i { width: 6px; height: 6px; border-radius: 50%; background: var(--accent); box-shadow: 0 0 0 3px rgba(125, 201, 184, .18); }
.jnode b { display: block; margin: 7px 0 4px; font-size: 15px; font-weight: 700; letter-spacing: -.01em; }
.jnode small { display: block; font-size: 12.5px; color: var(--muted); }

/* C2｜点阵装饰带 */
.dotband {
  height: 54px;
  background-image: radial-gradient(rgba(113, 185, 223, .28) 1px, transparent 1px);
  background-size: 16px 16px;
  -webkit-mask-image: linear-gradient(90deg, transparent, #000 22%, #000 78%, transparent);
  mask-image: linear-gradient(90deg, transparent, #000 22%, #000 78%, transparent);
}

/* E1｜下一块指示 */
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
.next:hover svg { transform: translateY(3px); }

/* ---------- 区域 1 ---------- */
/* 区域 1 主内容：吃满块内剩余高度，内容垂直居中 */
.intro {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  align-items: start;
  align-content: center;
  flex: 1 1 auto;
  min-height: 0;
}

.greet {
  display: flex;
  align-items: center;
  gap: 11px;
  font-size: 12.5px;
  color: var(--muted);
}
.greet::before,
.greet::after { content: ""; width: 22px; height: 1px; background: var(--accent-2); }

.intro-t {
  font-size: clamp(38px, 5.2vw, 66px);
  font-weight: 800;
  letter-spacing: -.045em;
  line-height: 1.03;
  margin: 18px 0 20px;
  max-width: 9.5em;
}
.intro-p { font-size: 14.5px; color: var(--muted); line-height: 1.85; max-width: 32em; margin: 0; }

.acts { display: flex; flex-wrap: wrap; gap: 12px; margin: 28px 0 38px; }
.btn-dark,
.btn-soft {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  padding: 13px 24px;
  border: 0;
  border-radius: 999px;
  font: inherit;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: transform .25s, background .25s, box-shadow .25s;
}
.btn-dark { background: var(--ink); color: #fff; box-shadow: 0 12px 26px rgba(36, 52, 61, .2); }
.btn-dark:hover { transform: translateY(-2px); box-shadow: 0 16px 34px rgba(36, 52, 61, .26); }
.btn-soft { background: #eef2f3; color: var(--ink); }
.btn-soft:hover { background: #e4ebed; }

.mini { display: flex; align-items: flex-start; }
.mini .m { padding: 0 30px; border-left: 1px solid var(--line); }
.mini .m:first-child { padding-left: 0; border-left: 0; }
.mini b { display: block; font-size: 27px; font-weight: 800; letter-spacing: -.03em; line-height: 1.1; }
.mini small { display: block; margin-top: 5px; font-size: 12.5px; color: var(--muted); }

.intro-r { position: relative; display: flex; flex-direction: column; align-items: flex-end; gap: 30px; padding-top: 4px; }
.building { display: inline-flex; align-items: center; gap: 9px; font-size: 12.5px; color: var(--muted); }
.building i { width: 7px; height: 7px; border-radius: 50%; background: #5cbb92; box-shadow: 0 0 0 4px rgba(92, 187, 146, .15); }

.focus-area { position: relative; width: 100%; display: flex; justify-content: flex-end; }
.focus-area::before {
  content: "";
  position: absolute;
  right: 70px;
  top: -90px;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(157, 221, 215, .5), transparent 66%);
  filter: blur(34px);
  pointer-events: none;
}
.orbit { position: absolute; right: 18px; top: -62px; width: 440px; height: 370px; pointer-events: none; z-index: 1; opacity: .9; }

.focus {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 392px;
  padding: 30px 32px;
  border: 1px solid var(--line);
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 26px 62px rgba(52, 93, 105, .14);
}
.focus .micro { margin-bottom: 0; }
.focus h3 { font-size: 29px; font-weight: 800; letter-spacing: -.035em; line-height: 1.16; margin: 15px 0 12px; }
.focus h3 .plus { color: var(--accent); font-weight: 500; }
.focus p { font-size: 13.5px; color: var(--muted); line-height: 1.78; margin: 0; }
.chips { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 22px; }
.chips span { font-size: 12px; padding: 7px 14px; border-radius: 999px; background: #eef3f4; color: #4e6570; }

/* ---------- 区域 2 · 精选项目 ---------- */
/* 区域 2 主内容：吃满块内剩余高度，内容垂直居中 */
.works {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1 1 auto;
  min-height: 0;
}
.works-head { display: flex; justify-content: space-between; align-items: flex-end; gap: 30px; margin-bottom: 24px; }
.more { display: inline-flex; align-items: center; gap: 7px; font-size: 12.5px; color: var(--muted); cursor: pointer; transition: color .25s; }
.more:hover { color: var(--ink); }
.works-grid { display: grid; grid-template-columns: 2.05fr 1fr 1fr; gap: 18px; align-items: stretch; }

.work {
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 18px;
  background: rgba(255, 255, 255, .8);
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: transform .3s cubic-bezier(.2, .7, .2, 1), box-shadow .3s, border-color .3s;
}
.work:hover { transform: translateY(-4px); border-color: var(--accent-line); box-shadow: 0 24px 58px rgba(52, 93, 105, .16); }

.work .cover {
  position: relative;
  height: 168px;
  overflow: hidden;
  flex: 0 0 auto;
  background-color: #eef4f4;
  background-image: linear-gradient(152deg, #e7eeee 0%, #f8fbfb 58%, #ecf4f1 100%);
  background-size: cover;
  background-position: center;
}

.wk-body { padding: 20px 22px 22px; display: flex; flex-direction: column; gap: 10px; flex: 1 1 auto; }
.wk-body h3 { font-size: 17.5px; font-weight: 800; letter-spacing: -.02em; line-height: 1.3; margin: 0; }
.wk-body p { font-size: 12.8px; color: var(--muted); line-height: 1.7; margin: 0; }
.wk-foot { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-top: auto; padding-top: 14px; }

.tags { display: flex; flex-wrap: wrap; gap: 7px; }
.tags span { font-size: 11px; padding: 5px 11px; border-radius: 999px; background: #f1f5f6; color: #4e6570; white-space: nowrap; }

.go {
  flex: 0 0 auto;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  border: 1px solid var(--line);
  color: var(--muted);
  transition: color .25s, border-color .25s, transform .25s;
}
.work:hover .go { color: var(--accent); border-color: var(--accent); transform: translate(2px, -2px); }

/* 大卡：封面铺满，文字叠在底部 */
.work-lg { min-height: 360px; border-color: transparent; }
.work-lg .cover { position: absolute; inset: 0; height: auto; }
.work-lg::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 64%;
  z-index: 1;
  pointer-events: none;
  background: linear-gradient(180deg, rgba(12, 22, 28, 0) 0%, rgba(12, 22, 28, .52) 52%, rgba(12, 22, 28, .82) 100%);
}
.cover-dark {
  background-color: #1d323c;
  background-image:
    radial-gradient(115% 78% at 12% 12%, rgba(96, 140, 150, .5), transparent 62%),
    radial-gradient(90% 70% at 88% 92%, rgba(52, 86, 96, .55), transparent 60%),
    linear-gradient(158deg, #15242d 0%, #1d323c 46%, #294851 100%);
}
.win {
  position: absolute;
  right: 24px;
  top: 30px;
  width: 52%;
  height: 56%;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, .16);
  background: linear-gradient(180deg, rgba(255, 255, 255, .12), rgba(255, 255, 255, .035));
  display: flex;
  flex-direction: column;
  gap: 11px;
  padding: 16px;
}
.win i { display: block; height: 9px; border-radius: 5px; background: rgba(255, 255, 255, .14); }
.win i:nth-child(1) { width: 64%; }
.win i:nth-child(2) { width: 88%; }
.win i:nth-child(3) { width: 46%; }

.feat {
  position: absolute;
  left: 24px;
  top: 22px;
  z-index: 2;
  font-size: 11.5px;
  padding: 6px 13px;
  border-radius: 999px;
  background: rgba(255, 255, 255, .16);
  color: #eaf4f2;
  border: 1px solid rgba(255, 255, 255, .2);
}
.lg-body { position: relative; z-index: 2; margin-top: auto; padding: 80px 26px 26px; color: #fff; }
.lg-body h3 { font-size: 26px; font-weight: 800; letter-spacing: -.03em; line-height: 1.2; margin: 0; }
.lg-body p { margin: 11px 0 0; font-size: 12.8px; line-height: 1.7; color: rgba(255, 255, 255, .78); max-width: 26em; }
.lg-body .tags { margin-top: 18px; }
.lg-body .tags span { background: rgba(255, 255, 255, .14); color: #eaf1f2; border: 1px solid rgba(255, 255, 255, .14); }
.work-lg .go {
  position: absolute;
  right: 24px;
  bottom: 26px;
  z-index: 2;
  background: rgba(255, 255, 255, .14);
  border-color: rgba(255, 255, 255, .22);
  color: #f2f7f7;
}
.work-lg:hover .go { background: rgba(255, 255, 255, .24); border-color: rgba(255, 255, 255, .4); color: #fff; }

/* 无封面时的占位示意 */
.mock-phone {
  position: absolute;
  left: 50%;
  bottom: -10px;
  transform: translateX(-50%);
  width: 82px;
  height: 122px;
  border-radius: 14px 14px 0 0;
  background: #fff;
  border: 1px solid #e2eaec;
  box-shadow: 0 12px 28px rgba(52, 93, 105, .14);
}
.mock-phone::before { content: ""; position: absolute; left: 12px; right: 12px; top: 16px; height: 7px; border-radius: 4px; background: #eef3f4; }
.mock-phone::after { content: ""; position: absolute; left: 12px; right: 24px; top: 31px; height: 7px; border-radius: 4px; background: #f4f8f9; }
.mock-lap {
  position: absolute;
  left: 50%;
  bottom: 16px;
  transform: translateX(-50%);
  width: 150px;
  height: 92px;
  border-radius: 8px 8px 3px 3px;
  background: #fff;
  border: 1px solid #e2eaec;
  box-shadow: 0 12px 28px rgba(52, 93, 105, .14);
}
.mock-lap::before { content: ""; position: absolute; left: 14px; right: 14px; top: 14px; height: 8px; border-radius: 4px; background: #eef3f4; }
.mock-lap::after { content: ""; position: absolute; left: 14px; right: 44px; top: 30px; height: 8px; border-radius: 4px; background: #f4f8f9; }

/* ---------- 响应式 ---------- */
@media (max-width: 1000px) {
  .intro { grid-template-columns: 1fr; gap: 44px; }
  .intro-r { align-items: flex-start; }
  .focus-area { justify-content: flex-start; }
  .focus { max-width: 100%; }
  .orbit { display: none; }
  .focus-area::before { right: auto; left: -40px; top: -80px; width: 320px; height: 320px; }
  .works-grid { grid-template-columns: 1fr 1fr; }
  .work-lg { grid-column: 1 / -1; }
}

@media (max-width: 768px) {
  /* 手机/平板：每个区域仍要保住一屏，所以整体收紧内容高度 */
  .blk-1 { padding: 68px 0 18px; }
  .blk-2 { padding: 30px 0 18px; }
  .wm { font-size: clamp(38px, 11vw, 64px); letter-spacing: .02em; }
  .jlist { flex-direction: column; }
  .jlist li + li { border-left: 0; border-top: 1px solid var(--line); }
  .jnode { padding: 12px 0; }
  .jlist li:first-child .jnode { padding-top: 0; }
  .jlist li:last-child .jnode { padding-bottom: 0; }
  .jnode b { font-size: 14.5px; }
  .dotband { height: 38px; background-size: 14px 14px; }
  .intro { gap: 30px; }
  .intro-t { font-size: 34px; max-width: none; }
  .intro-p { font-size: 13.5px; }
  .acts { margin: 22px 0 28px; }
  .mini { display: grid; grid-template-columns: 1fr 1fr; gap: 16px 0; }
  .mini .m { padding: 0 16px; border-left: 0; }
  .mini .m:nth-child(2n) { border-left: 1px solid var(--line); }
  .mini .m:nth-child(2n+1) { padding-left: 0; }
  .focus { padding: 20px 20px; border-radius: 16px; }
  .focus h3 { font-size: 23px; }
  .works-head { display: block; margin-bottom: 20px; }
  .more { margin-top: 12px; }
  .work-lg { min-height: 300px; }
  .work .cover { height: 132px; }
  .wk-body { padding: 16px 18px 18px; }
  .lg-body { padding: 78px 20px 22px; }
  .lg-body h3 { font-size: 21px; }
}

@media (max-width: 560px) {
  .intro-t { font-size: 30px; }
  .acts { margin: 18px 0 24px; }
  .btn-dark,
  .btn-soft { padding: 12px 20px; font-size: 13px; }
  .works-grid { grid-template-columns: 1fr; }
  .work-lg { grid-column: auto; min-height: 300px; }
}
</style>
