import request from './request'

export function fetchSlotsByDate(params) {
  return request.get('/slot/calendar', { params })
}

export function createAppointment(data) {
  return request.post('/appointment/create', data)
}

export function fetchMyAppointments(params) {
  return request.get('/appointment/list', { params })
}

export function cancelAppointment(id, reason) {
  return request.post(`/appointment/cancel/${id}`, null, { params: { reason } })
}

export function mockPaymentCallback(data) {
  return request.post('/payment/mock-callback', data)
}
