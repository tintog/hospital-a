import request from './request'

export function createSchedule(data) {
  return request.post('/admin/schedule/create', data)
}

export function updateSchedule(id, data) {
  return request.put(`/admin/schedule/${id}`, data)
}

export function cancelSchedule(id) {
  return request.post(`/admin/schedule/${id}/cancel`)
}

export function fetchSchedules(params) {
  return request.get('/admin/schedule/list', { params })
}

export function revokeSlot(id) {
  return request.put(`/admin/slot/${id}/revoke`)
}

export function fetchAdminAppointments(params) {
  return request.get('/admin/appointment/list', { params })
}

export function adminCancelAppointment(id, reason) {
  return request.post(`/admin/appointment/${id}/cancel`, null, { params: { reason } })
}

export function fetchDashboardStats() {
  return request.get('/admin/statistics/dashboard')
}
