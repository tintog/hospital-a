import request from './request'

export function fetchDoctors(params) {
  return request.get('/doctor/list', { params })
}

export function fetchDoctorDetail(id) {
  return request.get(`/doctor/${id}`)
}
