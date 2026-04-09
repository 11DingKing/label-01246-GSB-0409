import request from '@/utils/request'

export function getOperationLogPage(params) {
  return request.get('/log/operation/page', { params })
}

export function getLoginLogPage(params) {
  return request.get('/log/login/page', { params })
}
