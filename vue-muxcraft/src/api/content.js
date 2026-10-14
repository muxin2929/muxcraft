import request from './request'

export const getSite = () => request('/site')
export const getProfile = () => request('/profile')
export const getExperiences = (params = {}) => {
  const q = new URLSearchParams(params).toString()
  return request('/experiences?' + q)
}
export const getSkills = () => request('/skills')
export const getProjects = (params = {}) => {
  const q = new URLSearchParams(params).toString()
  return request('/projects?' + q)
}
export const getProject = (id) => request('/projects/' + id)
export const getComments = (projectId, params = {}) => {
  const q = new URLSearchParams(params).toString()
  return request(`/projects/${projectId}/comments?` + q, { auth: true })
}
export const postComment = (projectId, content, replyToId, cfToken) =>
  request(`/projects/${projectId}/comments`, { method: 'POST', body: { content, replyToId, 'cf-turnstile-response': cfToken }, auth: true })
export const getGuestbook = (params = {}) => {
  const q = new URLSearchParams(params).toString()
  return request('/guestbook?' + q, { auth: true })
}
export const postGuestbook = (content, replyToId, cfToken) => request('/guestbook', { method: 'POST', body: { content, replyToId, 'cf-turnstile-response': cfToken }, auth: true })
export const postSuggestion = (payload) => request('/suggestions', { method: 'POST', body: payload, auth: true })
export const getArticles = (params = {}) => {
  const q = new URLSearchParams(params).toString()
  return request('/articles?' + q)
}
export const getArticle = (id) => request('/articles/' + id)
export const getAchievements = () => request('/achievements')
