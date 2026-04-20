import request from './request'

export function fetchDepartments() {
  return request.get('/department/list')
}
