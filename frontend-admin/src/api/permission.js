import request from '@/utils/request'

export function getPermissionTree() {
  return request.get('/permission/tree')
}

export function createPermission(data) {
  return request.post('/permission/', data)
}

export function updatePermission(id, data) {
  return request.put(`/permission/${id}`, data)
}

export function deletePermission(id) {
  return request.delete(`/permission/${id}`)
}
