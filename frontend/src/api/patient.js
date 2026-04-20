import request from './request'

export function getProfile() {
  return request.get('/patient/profile')
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
