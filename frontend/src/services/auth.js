const PORTAL_AUTH_USER_KEY = 'dao_home_portal_auth_user'
const ADMIN_AUTH_USER_KEY = 'dao_home_admin_auth_user'

function getStoredUser(key) {
  try {
    const text = window.localStorage.getItem(key)
    return text ? JSON.parse(text) : null
  } catch {
    return null
  }
}

function setStoredUser(key, user) {
  window.localStorage.setItem(key, JSON.stringify(user))
}

export function getPortalUser() {
  return getStoredUser(PORTAL_AUTH_USER_KEY)
}

export function setPortalUser(user) {
  setStoredUser(PORTAL_AUTH_USER_KEY, user)
}

export function clearPortalUser() {
  window.localStorage.removeItem(PORTAL_AUTH_USER_KEY)
}

export function getAdminUser() {
  return getStoredUser(ADMIN_AUTH_USER_KEY)
}

export function setAdminUser(user) {
  setStoredUser(ADMIN_AUTH_USER_KEY, user)
}

export function clearAdminUser() {
  window.localStorage.removeItem(ADMIN_AUTH_USER_KEY)
}

export function clearAllAuthUsers() {
  clearPortalUser()
  clearAdminUser()
}

export function roleHome(user) {
  if (user?.role === 'TECHNICIAN') return '/technician'
  return '/client'
}
