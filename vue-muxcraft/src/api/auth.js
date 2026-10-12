import request from './request'

// 对应 API 文档 2. 认证模块
export function register(payload) {
  return request('/auth/register', { method: 'POST', body: payload })
}

export function login(payload) {
  return request('/auth/login', { method: 'POST', body: payload })
}

export function me() {
  return request('/auth/me', { auth: true })
}
