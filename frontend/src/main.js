import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import Vant from 'vant'
import 'element-plus/dist/index.css'
import 'vant/lib/index.css'
import App from './App.vue'
import ClientHome from './views/client/ClientHome.vue'
import ClientTechnicians from './views/client/ClientTechnicians.vue'
import ClientServiceDetail from './views/client/ClientServiceDetail.vue'
import ClientAddress from './views/client/ClientAddress.vue'
import ClientAddressList from './views/client/ClientAddressList.vue'
import ClientCheckout from './views/client/ClientCheckout.vue'
import ClientOrders from './views/client/ClientOrders.vue'
import ClientProfile from './views/client/ClientProfile.vue'
import AdminDashboard from './views/admin/AdminDashboard.vue'
import TechnicianHome from './views/technician/TechnicianHome.vue'
import RoleEntry from './views/RoleEntry.vue'
import AdminLogin from './views/admin/AdminLogin.vue'
import { api } from './services/api'
import { getAdminUser, getPortalUser, roleHome, setPortalUser } from './services/auth'
import './styles/main.css'

const routes = [
  { path: '/', component: RoleEntry },
  { path: '/client', component: ClientHome },
  { path: '/client/services/:serviceId', component: ClientServiceDetail },
  { path: '/client/services/:serviceId/technicians', component: ClientTechnicians },
  { path: '/client/checkout', component: ClientCheckout },
  { path: '/client/orders', component: ClientOrders },
  { path: '/client/profile', component: ClientProfile },
  { path: '/client/addresses', component: ClientAddressList },
  { path: '/client/address', component: ClientAddress },
  { path: '/admin/login', component: AdminLogin },
  { path: '/admin', component: AdminDashboard },
  { path: '/technician', component: TechnicianHome }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach(async (to) => {
  if (to.path === '/') return true
  if (to.path === '/admin/login') return true

  if (to.path.startsWith('/admin')) {
    const admin = getAdminUser()
    return admin?.role === 'ADMIN' ? true : '/admin/login'
  }

  let user = getPortalUser()
  if (!user?.role && to.path.startsWith('/client')) {
    try {
      user = await api.wechatSession()
      setPortalUser(user)
    } catch {
      return '/'
    }
  }
  if (!user?.role) return '/'
  if (to.path.startsWith('/technician') && user.role !== 'TECHNICIAN') return roleHome(user)
  if (to.path.startsWith('/client') && user.role !== 'CLIENT') return roleHome(user)
  return true
})

createApp(App).use(router).use(ElementPlus, { locale: zhCn }).use(Vant).mount('#app')
