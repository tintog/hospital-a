import request from './request'

// 患者管理
export function fetchAdminPatients(params) {
  return request.get('/admin/patient/list', { params })
}

export function fetchAdminPatientDetail(id) {
  return request.get(`/admin/patient/${id}`)
}

export function blacklistPatient(id, days) {
  return request.put(`/admin/patient/${id}/blacklist`, { days })
}

export function unblacklistPatient(id) {
  return request.put(`/admin/patient/${id}/unblacklist`)
}

export function deletePatient(id) {
  return request.delete(`/admin/patient/${id}`)
}

// 医生管理
export function fetchAdminDoctors(params) {
  return request.get('/admin/doctor/list', { params })
}

export function fetchAdminDoctorDetail(id) {
  return request.get(`/admin/doctor/${id}`)
}

export function createAdminDoctor(data) {
  return request.post('/admin/doctor', data)
}

export function updateAdminDoctor(id, data) {
  return request.put(`/admin/doctor/${id}`, data)
}

export function deleteAdminDoctor(id) {
  return request.delete(`/admin/doctor/${id}`)
}
