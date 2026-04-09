import request from '@/utils/request'

export function getRolePage(params) {
  return request.get('/role/page', { params })
}

export function getRoleList() {
  return request.get('/role/list')
}

export function getRoleById(id) {
  return request.get(`/role/${id}`)
}

export function createRole(data) {
  return request.post('/role/', data)
}

export function updateRole(id, data) {
  return request.put(`/role/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/role/${id}`)
}

export function assignRolePermissions(id, permIds) {
  return request.put(`/role/${id}/permissions`, { permIds })
}

export function getRolePermissionIds(id) {
  return request.get(`/role/${id}/permissions`)
}

export function updateRoleDataScope(id, scopeType) {
  return request.put(`/role/${id}/data-scope`, { scopeType })
}

export function getRoleDataScope(id) {
  return request.get(`/role/${id}/data-scope`)
}
