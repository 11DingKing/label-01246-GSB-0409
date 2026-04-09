import request from '@/utils/request'

// 获取配置列表
export function getConfigList(params) {
  return request.get('/config/list', { params })
}

// 修改单个配置
export function updateConfig(id, data) {
  return request.put(`/config/${id}`, data)
}

// 批量修改配置
export function batchUpdateConfig(data) {
  return request.put('/config/batch', data)
}
