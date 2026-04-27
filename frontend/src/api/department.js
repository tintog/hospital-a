import request from './request'

export function fetchDepartments() {
  return request.get('/department/list')
}

export function createDepartment(data) {
  return request.post('/department/create', data)
}

export function updateDepartment(id, data) {
  return request.put(`/department/${id}`, data)
}
