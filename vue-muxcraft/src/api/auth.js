import request, { setAccessToken } from './request'

export function register(payload) {
  return request('/auth/register', { method: 'POST', body: payload }).then(data => {
    setAccessToken(data.token)
    return data
  })
}

export function login(payload) {
  return request('/auth/login', { method: 'POST', body: payload }).then(data => {
    setAccessToken(data.token)
    return data
  })
}

export function logout() {
  return request('/auth/logout', { method: 'POST', auth: true }).finally(() => {
    setAccessToken(null)
  })
}

// 页面加载时尝试用 cookie 刷新 access token
export async function tryRestoreSession() {
  try {
    const data = await request('/auth/refresh', { method: 'POST' })
    setAccessToken(data.token)
    return data
  } catch {
    return null
  }
}
