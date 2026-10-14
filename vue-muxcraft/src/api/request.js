// 统一 fetch 封装 — 支持 refresh token 自动刷新
const BASE = '/api'

// access token 内存存储（刷新页面后丢失，靠 /auth/refresh 恢复）
let accessToken = null
let isRefreshing = false
let pendingQueue = []

export function setAccessToken(t) { accessToken = t }
export function getAccessToken() { return accessToken }

async function doRefresh() {
  const res = await fetch(BASE + '/auth/refresh', {
    method: 'POST',
    credentials: 'include'
  })
  const json = await res.json().catch(() => ({}))
  if (!res.ok || json.code !== 200) throw new Error('refresh failed')
  accessToken = json.data.token
  return json.data
}

async function request(path, { method = 'GET', body, auth = false } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  if (auth && accessToken) headers.Authorization = `Bearer ${accessToken}`

  async function exec() {
    return fetch(BASE + path, {
      method,
      headers,
      credentials: 'include',
      body: body ? JSON.stringify(body) : undefined
    })
  }

  let res = await exec()
  let json = await res.json().catch(() => ({ code: res.status, message: res.statusText, data: null }))

  // 401：尝试刷新 token
  if (res.status === 401 && auth && path !== '/auth/refresh') {
    if (isRefreshing) {
      // 正在刷新，排队等待
      await new Promise(resolve => pendingQueue.push(resolve))
      if (accessToken) headers.Authorization = `Bearer ${accessToken}`
      res = await exec()
      json = await res.json().catch(() => ({ code: res.status, message: res.statusText, data: null }))
    } else {
      isRefreshing = true
      try {
        const data = await doRefresh()
        accessToken = data.token
        // 通知排队的请求继续
        pendingQueue.forEach(resolve => resolve())
        pendingQueue = []
        // 重试当前请求
        headers.Authorization = `Bearer ${accessToken}`
        res = await exec()
        json = await res.json().catch(() => ({ code: res.status, message: res.statusText, data: null }))
      } catch (e) {
        // 刷新失败：清除登录态
        accessToken = null
        pendingQueue.forEach(resolve => resolve())
        pendingQueue = []
        window.dispatchEvent(new CustomEvent('auth:logout'))
        window.dispatchEvent(new CustomEvent('auth:required'))
        throw new Error(json.message || '登录状态已过期，请重新登录')
      } finally {
        isRefreshing = false
      }
    }
  }

  // 非 401 的其他错误
  if (res.status === 401 && !auth) {
    throw new Error(json.message || '请先登录')
  }
  if (res.status === 403 && json.message === '请先完成安全验证') {
    // 验证已过期，弹出全站验证门（不刷新页面）
    window.dispatchEvent(new CustomEvent('sitegate:show'))
    throw new Error('请先完成安全验证')
  }
  if (!res.ok || json.code !== 200) {
    const err = new Error(json.message || '请求失败')
    err.code = json.code || res.status
    err.status = res.status
    throw err
  }
  return json.data
}

export default request
