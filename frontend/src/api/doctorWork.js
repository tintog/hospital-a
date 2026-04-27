import request from './request'

export function fetchTodayPatients(params) {
  return request.get('/doctor/today-patients', { params })
}

export function updateVisitStatus(id, visitStatus) {
  return request.put(`/doctor/appointments/${id}/visit-status`, { visitStatus })
}
