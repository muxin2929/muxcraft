// 聊天/私信相关 REST 接口
import request from './request'

// ===== 会话列表 =====
export const getConversations = () => request('/chat/conversations', { auth: true })

// ===== 某会话消息列表 =====
export const getMessages = (userId, params = {}) => {
  const q = new URLSearchParams(params).toString()
  return request(`/chat/messages/${userId}?${q}`, { auth: true })
}

// ===== 发送私信（REST 兜底；实时走 WebSocket） =====
export const sendMessage = (toUserId, content) =>
  request('/chat/messages', { method: 'POST', body: { toUserId, content }, auth: true })

// ===== 屏蔽/取消屏蔽用户 =====
export const blockUser = (userId) =>
  request(`/chat/block/${userId}`, { method: 'POST', auth: true })
export const unblockUser = (userId) =>
  request(`/chat/block/${userId}`, { method: 'DELETE', auth: true })
export const getBlockedList = () =>
  request('/chat/blocked', { auth: true })
export const getBlockStatus = (userId) =>
  request(`/chat/block/${userId}`, { auth: true })

// ===== 删除某条留言/评论（用户自己的） =====
export const deleteGuestbook = (id) =>
  request(`/guestbook/${id}`, { method: 'DELETE', auth: true })
export const deleteComment = (id) =>
  request(`/comments/${id}`, { method: 'DELETE', auth: true })

// ===== 标记会话已读 =====
export const markRead = (userId) =>
  request(`/chat/read/${userId}`, { method: 'POST', auth: true })
