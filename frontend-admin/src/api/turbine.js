import request from '@/utils/request'

// 仪表盘统计数据
export function getDashboardStats() {
  return request.get('/turbine/dashboard-stats')
}

// 设备列表（分页）
export function getTurbinePage(params) {
  return request.get('/turbine/page', { params })
}

// 设备列表（全量）
export function getTurbineList() {
  return request.get('/turbine/list')
}

// 设备详情
export function getTurbineDetail(id) {
  return request.get(`/turbine/${id}`)
}

// 实时监控数据
export function getMonitorData(params) {
  return request.get('/turbine/monitor', { params })
}
