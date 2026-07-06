const jsonHeaders = { 'Content-Type': 'application/json' }

async function request(path, options) {
  const response = await fetch(path, options)
  if (!response.ok) {
    const text = await response.text()
    let message = text
    try {
      message = JSON.parse(text).message || text
    } catch {
      message = text
    }
    throw new Error(message || `接口请求失败: ${response.status}`)
  }
  if (response.status === 204) return null
  const text = await response.text()
  return text ? JSON.parse(text) : null
}

export const api = {
  login: (payload) => request('/api/auth/login', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  }),
  currentUser: (userId = '') => request(`/api/client/me${userId ? `?userId=${encodeURIComponent(userId)}` : ''}`),
  clientSiteSettings: () => request('/api/client/site-settings'),
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
  listTechnicianOrders: (technicianId) => request(`/api/technician/${technicianId}/orders`),
  currentTechnicianOrderById: (technicianId) => request(`/api/technician/${technicianId}/orders/current`),
  technicianAction: (orderId, action) => request(`/api/technician/orders/${orderId}/actions/${action}`, {
    method: 'POST'
  }),
  approveRefund: (orderId) => request(`/api/merchant/orders/${orderId}/refund/approve`, { method: 'POST' }),
  merchantSiteSettings: () => request('/api/merchant/site-settings'),
  saveMerchantSiteSettings: (payload) => request('/api/merchant/site-settings', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  }),
  listAllTechnicians: () => request('/api/technicians'),
  listUsers: () => request('/api/users'),
  createUser: (payload) => request('/api/users', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  }),
  updateUserRole: (userId, payload) => request(`/api/users/${userId}/role`, {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  }),
  resetUserPassword: (userId, payload) => request(`/api/users/${userId}/password`, {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  }),
  saveService: (payload) => request('/api/merchant/services', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  }),
  uploadServiceCover: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request('/api/merchant/uploads/service-cover', {
      method: 'POST',
      body: formData
    })
  },
  deleteService: (serviceId) => request(`/api/merchant/services/${serviceId}`, { method: 'DELETE' }),
  saveTechnician: (payload) => request('/api/merchant/technicians', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  }),
  deleteTechnician: (technicianId) => request(`/api/merchant/technicians/${technicianId}`, { method: 'DELETE' }),
  uploadTechnicianAvatar: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request('/api/merchant/uploads/technician-avatar', {
      method: 'POST',
      body: formData
    })
  },
  updateTechnicianAvatar: (technicianId, file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request(`/api/technician/${technicianId}/avatar`, {
      method: 'POST',
      body: formData
    })
  },
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
