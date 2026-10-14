// WebSocket 聊天连接单例
import { reactive } from 'vue'
import { getAccessToken } from '../api/request'

const state = reactive({
  ws: null,
  connected: false,
  listeners: new Set(),
  pending: []
})

function wsUrl() {
  const proto = location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${proto}//${location.host}/api/ws/chat`
}

let manualClose = false
let reconnectTimer = null

function connect() {
  if (state.ws && (state.ws.readyState === WebSocket.OPEN || state.ws.readyState === WebSocket.CONNECTING)) return
  const token = getAccessToken()
  if (!token) return
  manualClose = false
  const url = `${wsUrl()}?token=${encodeURIComponent(token)}`
  try {
    state.ws = new WebSocket(url)
  } catch (e) { return }
  state.ws.onopen = () => {
    state.connected = true
    state.pending.forEach(p => state.ws.send(JSON.stringify(p)))
    state.pending = []
  }
  state.ws.onmessage = (ev) => {
    try {
      const msg = JSON.parse(ev.data)
      state.listeners.forEach(fn => fn(msg))
    } catch (e) { /* ignore */ }
  }
  state.ws.onclose = (event) => {
    state.connected = false
    state.ws = null
    // 鉴权失败（token 无效/过期）：清空登录态并刷新，不重连
    if (event.code === 1008) {
      window.dispatchEvent(new CustomEvent('auth:logout'))
      return
    }
    // 非手动关闭时自动重连
    if (!manualClose && getAccessToken()) {
      clearTimeout(reconnectTimer)
      reconnectTimer = setTimeout(connect, 3000)
    }
  }
  state.ws.onerror = () => { state.connected = false }
}

function disconnect() {
  manualClose = true
  clearTimeout(reconnectTimer)
  if (state.ws) state.ws.close()
  state.ws = null
  state.connected = false
}

function send(obj) {
  if (state.connected && state.ws) state.ws.send(JSON.stringify(obj))
  else state.pending.push(obj)
}

function subscribe(fn) {
  state.listeners.add(fn)
  return () => state.listeners.delete(fn)
}

export function useChatSocket() {
  return { state, connect, disconnect, send, subscribe }
}
