import request from '@/utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function resendVerifyEmail(data) {
  return request.post('/auth/resend-verify-email', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function getUserInfo() {
  return request.get('/auth/info')
}

export function changePassword(data) {
  return request.put('/auth/password', data)
}

export function resetPassword(data) {
  return request.post('/auth/reset-password', data)
}
