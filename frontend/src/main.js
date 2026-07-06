import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
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
import './styles/main.css'

const routes = [
  { path: '/', redirect: '/client' },
  { path: '/client', component: ClientHome },
  { path: '/client/services/:serviceId', component: ClientServiceDetail },
  { path: '/client/services/:serviceId/technicians', component: ClientTechnicians },
  { path: '/client/checkout', component: ClientCheckout },
  { path: '/client/orders', component: ClientOrders },
  { path: '/client/profile', component: ClientProfile },
  { path: '/client/addresses', component: ClientAddressList },
  { path: '/client/address', component: ClientAddress },
  { path: '/admin', component: AdminDashboard },
  { path: '/technician', component: TechnicianHome }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

createApp(App).use(router).use(ElementPlus).mount('#app')
