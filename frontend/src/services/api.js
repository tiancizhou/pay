const jsonHeaders = { 'Content-Type': 'application/json' }

async function request(path, options) {
  const response = await fetch(path, options)
  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || `接口请求失败: ${response.status}`)
  }
  return response.json()
}

export const api = {
  listServices: () => request('/api/client/services'),
  listTechnicians: (serviceId, location) => {
    const query = location ? `?latitude=${location.latitude}&longitude=${location.longitude}` : ''
    return request(`/api/client/services/${serviceId}/technicians${query}`)
  },
  createOrder: (payload) => request('/api/client/orders', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  }),
  latestClientOrder: () => request('/api/client/orders/latest'),
  payOrder: (orderId) => request(`/api/client/orders/${orderId}/pay`, { method: 'POST' }),
  cancelOrder: (orderId) => request(`/api/client/orders/${orderId}/cancel`, { method: 'POST' }),
  requestRefund: (orderId) => request(`/api/client/orders/${orderId}/refund`, { method: 'POST' }),
  updateCustomerLocation: (orderId, payload) => request(`/api/client/orders/${orderId}/customer-location`, {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  }),
  merchantDashboard: () => request('/api/merchant/dashboard'),
  technicianDashboard: () => request('/api/technician/dashboard'),
  currentTechnicianOrder: () => request('/api/technician/orders/current'),
  technicianAction: (orderId, action) => request(`/api/technician/orders/${orderId}/actions/${action}`, {
    method: 'POST'
  }),
  approveRefund: (orderId) => request(`/api/merchant/orders/${orderId}/refund/approve`, { method: 'POST' }),
  updateServiceDetail: (serviceId, payload) => request(`/api/merchant/services/${serviceId}/detail`, {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  }),
  updateTechnicianLocation: (orderId, payload) => request(`/api/technician/orders/${orderId}/location`, {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  }),
  updateTechnicianBaseLocation: (technicianId, payload) => request(`/api/technician/${technicianId}/location`, {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  }),
  listOrders: () => request('/api/orders')
}
