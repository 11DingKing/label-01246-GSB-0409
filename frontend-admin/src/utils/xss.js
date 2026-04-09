/**
 * XSS 防护工具
 * 对用户输入进行 HTML 实体转义，防止 XSS 攻击
 */

const escapeMap = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#x27;',
  '/': '&#x2F;'
}

/**
 * HTML 实体转义
 * @param {string} str - 需要转义的字符串
 * @returns {string} 转义后的字符串
 */
export function escapeHtml(str) {
  if (!str || typeof str !== 'string') return str
  return str.replace(/[&<>"'/]/g, (char) => escapeMap[char] || char)
}

/**
 * 清除所有 HTML 标签
 * @param {string} str - 需要清理的字符串
 * @returns {string} 清理后的字符串
 */
export function stripTags(str) {
  if (!str || typeof str !== 'string') return str
  return str.replace(/<[^>]*>/g, '')
}

/**
 * 对对象中的所有字符串值进行 XSS 转义
 * @param {object} obj - 需要处理的对象
 * @returns {object} 处理后的对象
 */
export function sanitizeObject(obj) {
  if (!obj || typeof obj !== 'object') return obj
  const result = Array.isArray(obj) ? [] : {}
  for (const key of Object.keys(obj)) {
    const value = obj[key]
    if (typeof value === 'string') {
      result[key] = escapeHtml(value)
    } else if (typeof value === 'object' && value !== null) {
      result[key] = sanitizeObject(value)
    } else {
      result[key] = value
    }
  }
  return result
}

/**
 * 检测字符串是否包含潜在的 XSS 攻击内容
 * @param {string} str - 需要检测的字符串
 * @returns {boolean}
 */
export function containsXss(str) {
  if (!str || typeof str !== 'string') return false
  const lower = str.toLowerCase()
  const patterns = [
    '<script', 'javascript:', 'onerror', 'onload', 'onclick',
    'onmouseover', 'onfocus', 'eval(', 'expression(', 'vbscript:'
  ]
  return patterns.some(p => lower.includes(p))
}
