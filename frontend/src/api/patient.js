import request from './request'

export function getProfile() {
  return request.get('/patient/profile')
}

export function updatePatientPhone(data) {
  return request.put('/patient/profile/phone', data)
}

export function updatePatientPassword(data) {
  return request.put('/patient/profile/password', data)
}

export function fetchMembers() {
  return request.get('/patient/members')
}

export function addMember(data) {
  return request.post('/patient/members', data)
}

export function deleteMember(id) {
  return request.delete(`/patient/members/${id}`)
}
