import request from '@/utils/request'

// 故障记录分页
export function getFaultPage(params) {
  return request.get('/fault/page', { params })
}

// 故障详情
export function getFaultDetail(id) {
  return request.get(`/fault/${id}`)
}

// 故障统计
export function getFaultStatistics() {
  return request.get('/fault/statistics')
}

// 更新故障处理状态
export function updateFaultStatus(id, data) {
  return request.put(`/fault/${id}/status`, data)
}
