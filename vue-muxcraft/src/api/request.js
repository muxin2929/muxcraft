// 统一 fetch 封装
const BASE = '/api'

async function request(path, { method = 'GET', body, auth = false } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  if (auth) {
    const token = localStorage.getItem('mux-token')
    if (token) headers.Authorization = `Bearer ${token}`
  }
  let res
  try {
    res = await fetch(BASE + path, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined
    })
  } catch (e) {
    throw new Error('网络错误，请稍后重试')
  }
  const json = await res.json().catch(() => ({ code: res.status, message: res.statusText, data: null }))
  // token 过期或未登录：清 localStorage + 弹出登录框
  if (res.status === 401 || json.code === 401) {
    localStorage.removeItem('mux-token')
    localStorage.removeItem('mux-user')
    window.dispatchEvent(new CustomEvent('auth:logout'))
    window.dispatchEvent(new CustomEvent('auth:required'))
    throw new Error(json.message || '请先登录')
  }
  // HTTP 4xx/5xx 或业务码非 200，都抛错并带上后端 message
  if (!res.ok || json.code !== 200) {
    const err = new Error(json.message || '请求失败')
    err.code = json.code || res.status
    err.status = res.status
    throw err
  }
  return json.data
}

export default request
