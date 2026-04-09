import request from '@/utils/request'

export function getUserPage(params) {
  return request.get('/user/page', { params })
}

export function getUserById(id) {
  return request.get(`/user/${id}`)
}

export function createUser(data) {
  return request.post('/user/', data)
}

export function updateUser(id, data) {
  return request.put(`/user/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}

export function updateUserStatus(id, status) {
  return request.put(`/user/${id}/status`, { status })
}

export function updateUserLock(id, locked) {
  return request.put(`/user/${id}/lock`, { locked })
}

export function assignUserRoles(id, roleIds) {
  return request.put(`/user/${id}/roles`, { roleIds })
}

export function getUserRoleIds(id) {
  return request.get(`/user/${id}/roles`)
}
