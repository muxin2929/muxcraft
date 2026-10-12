// 统一时间显示：yy-mm-dd HH:mm（UTC+8）
export function fmtDateTime(v) {
  if (!v) return ''
  const d = new Date(v)
  if (isNaN(d.getTime())) return String(v)
  // 转 UTC+8
  const t = new Date(d.getTime() + 8 * 3600 * 1000 - d.getTimezoneOffset() * 60000)
  const yy = String(t.getUTCFullYear()).slice(2)
  const mm = String(t.getUTCMonth() + 1).padStart(2, '0')
  const dd = String(t.getUTCDate()).padStart(2, '0')
  const hh = String(t.getUTCHours()).padStart(2, '0')
  const mi = String(t.getUTCMinutes()).padStart(2, '0')
  return `${yy}-${mm}-${dd} ${hh}:${mi}`
}

export function fmtDate(v) {
  if (!v) return ''
  const d = new Date(v)
  if (isNaN(d.getTime())) return String(v)
  const t = new Date(d.getTime() + 8 * 3600 * 1000 - d.getTimezoneOffset() * 60000)
  const yy = String(t.getUTCFullYear()).slice(2)
  const mm = String(t.getUTCMonth() + 1).padStart(2, '0')
  const dd = String(t.getUTCDate()).padStart(2, '0')
  return `${yy}-${mm}-${dd}`
}
