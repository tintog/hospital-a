import request from './request'

export function patientLogin(data) {
  return request.post('/auth/login', data)
}

export function patientRegister(data) {
  return request.post('/auth/register', data)
}

export function sysLogin(data) {
  return request.post('/auth/sys-login', data)
}

export function realNameAuth(data) {
  return request.post('/auth/realname', data)
}
