import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('../pages/HomePage.vue') },
  { path: '/register', name: 'register', component: () => import('../pages/RegisterPage.vue') },
  { path: '/profile', name: 'profile', component: () => import('../pages/ProfilePage.vue') },
  { path: '/experience', name: 'experience', component: () => import('../pages/ExperiencePage.vue') },
  { path: '/skills', name: 'skills', component: () => import('../pages/SkillsPage.vue') },
  { path: '/projects', name: 'projects', component: () => import('../pages/ProjectsPage.vue') },
  { path: '/projects/:id', name: 'project-detail', component: () => import('../pages/ProjectDetailPage.vue') },
  { path: '/achievements', name: 'achievements', component: () => import('../pages/AchievementsPage.vue') },
  { path: '/blog', name: 'blog', component: () => import('../pages/BlogPage.vue') },
  { path: '/guestbook', name: 'guestbook', component: () => import('../pages/GuestbookPage.vue') },
  { path: '/contact', name: 'contact', component: () => import('../pages/ContactPage.vue') },
  { path: '/admin', name: 'admin', component: () => import('../pages/AdminPage.vue') },
  { path: '/about', name: 'about', component: () => import('../pages/AboutPage.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, saved) {
    // 刷新/直接进入时不恢复历史滚动位置，直接回到顶部（即时，避免自动下滑）
    if (saved) return { ...saved, behavior: 'smooth' }
    return { top: 0, behavior: 'auto' }
  }
})

// 禁用浏览器刷新后的滚动位置记忆
if (typeof history !== 'undefined' && 'scrollRestoration' in history) {
  history.scrollRestoration = 'manual'
}

export default router
