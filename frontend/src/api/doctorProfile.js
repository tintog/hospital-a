import request from './request'

export function fetchDoctorProfile() {
  return request.get('/doctor/profile')
}

export function updateDoctorPhone(data) {
  return request.put('/doctor/profile/phone', data)
}

export function updateDoctorPassword(data) {
  return request.put('/doctor/profile/password', data)
}
