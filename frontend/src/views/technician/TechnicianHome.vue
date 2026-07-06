<template>
  <MobileShell :tabbar="false">
    <section class="console-hero technician-hero">
      <p>技师端</p>
      <h1>接单履约台</h1>
      <span>接单、联系客户、出发、上钟、下钟、完成服务</span>
    </section>

    <section class="metric-grid">
      <article><span>今日服务</span><strong>{{ dashboard.todayOrders }}</strong></article>
      <article><span>预估收入</span><strong>¥ {{ dashboard.todayIncome }}</strong></article>
      <article><span>待处理</span><strong>{{ pendingOrders.length }}</strong></article>
      <article><span>服务中</span><strong>{{ servingOrders.length }}</strong></article>
    </section>

    <section class="console-card technician-location-card">
      <h2>我的位置</h2>
      <p>{{ technicianLocation?.label ?? '未设置技师位置' }}</p>
      <button @click="setBaseLocation">设置/更新我的位置</button>
    </section>

    <section class="technician-tabs">
      <button :class="{ active: activeTab === 'pending' }" @click="activeTab = 'pending'">待接单</button>
      <button :class="{ active: activeTab === 'serving' }" @click="activeTab = 'serving'">待服务</button>
      <button :class="{ active: activeTab === 'finished' }" @click="activeTab = 'finished'">已完成</button>
    </section>

    <section class="technician-order-list">
      <article v-for="item in visibleOrders" :key="item.id" class="technician-order-card">
        <header>
          <strong>{{ serviceName(item.serviceId) }}</strong>
          <span>{{ statusText[item.status] ?? item.status }}</span>
        </header>
        <p>预约时间：{{ item.serviceTime }}</p>
        <p>客户：{{ item.address?.contactName }} {{ item.address?.phone }}</p>
        <p>地址：{{ item.address?.region }} {{ item.address?.detail }}</p>
        <p>服务时长：{{ serviceDuration(item.serviceId) }}分钟</p>
        <p v-if="clockInTime(item)">上钟时间：{{ formatTime(clockInTime(item)) }}</p>
        <p v-if="expectedClockOutTime(item)">预计下钟：{{ formatTime(expectedClockOutTime(item)) }}</p>
        <p v-if="item.customerLocation" class="location-line">客户定位：{{ item.customerLocation.label }}</p>
        <p v-if="item.technicianLocation" class="location-line">我的定位：{{ item.technicianLocation.label }}</p>
        <ol class="technician-flow">
          <li v-for="step in flowSteps" :key="step.status" :class="{ done: isDone(item, step.status), current: item.status === step.status }">
            {{ step.label }}
          </li>
        </ol>
        <section class="technician-timeline">
          <strong>状态记录</strong>
          <p v-for="record in item.timeline" :key="record.happenedAt">
            {{ formatTime(record.happenedAt) }} · {{ statusText[record.status] ?? record.status }} · {{ record.message }}
          </p>
        </section>
        <footer>
          <button class="secondary" @click="syncLocation(item)">更新定位</button>
          <button class="secondary" @click="callCustomer(item)">联系客户</button>
          <button v-for="action in availableActions(item)" :key="action.value" @click="doAction(item, action.value)">
            {{ action.label }}
          </button>
        </footer>
      </article>
      <section v-if="!visibleOrders.length" class="empty-orders">
        <div class="empty-illustration">▤</div>
        <p>暂无订单</p>
      </section>
    </section>

    <div v-if="toast" class="toast">{{ toast }}</div>
  </MobileShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import MobileShell from '../../components/MobileShell.vue'
import { api } from '../../services/api'
import { locate } from '../../services/location'
import { statusText } from '../../services/status'

const dashboard = reactive({
  todayOrders: 0,
  todayIncome: '0.00',
  pendingTasks: 0,
  satisfactionRate: '0%'
})
const services = ref([])
const orders = ref([])
const activeTab = ref('pending')
const toast = ref('')
const technicianLocation = ref(null)

const pendingStatuses = ['PAID_TO_MERCHANT', 'WAITING_TECHNICIAN_ACCEPT']
const servingStatuses = ['TECHNICIAN_ACCEPTED', 'CONTACTED_CUSTOMER', 'ON_THE_WAY', 'ARRIVED', 'CLOCKED_IN', 'CLOCKED_OUT']
const finishedStatuses = ['FINISHED']
const flowSteps = [
  { status: 'TECHNICIAN_ACCEPTED', label: '接单' },
  { status: 'CONTACTED_CUSTOMER', label: '联系' },
  { status: 'ON_THE_WAY', label: '出发' },
  { status: 'ARRIVED', label: '到达' },
  { status: 'CLOCKED_IN', label: '上钟' },
  { status: 'CLOCKED_OUT', label: '下钟' },
  { status: 'FINISHED', label: '完成' }
]
const actionMap = {
  PAID_TO_MERCHANT: [{ value: 'ACCEPT', label: '接单' }],
  WAITING_TECHNICIAN_ACCEPT: [{ value: 'ACCEPT', label: '接单' }],
  TECHNICIAN_ACCEPTED: [{ value: 'CONTACT_CUSTOMER', label: '已联系客户' }, { value: 'DEPART', label: '出发上门' }],
  CONTACTED_CUSTOMER: [{ value: 'DEPART', label: '出发上门' }],
  ON_THE_WAY: [{ value: 'ARRIVE', label: '到达客户地址' }],
  ARRIVED: [{ value: 'CLOCK_IN', label: '开始上钟' }],
  CLOCKED_IN: [{ value: 'CLOCK_OUT', label: '结束下钟' }],
  CLOCKED_OUT: [{ value: 'FINISH', label: '完成订单' }]
}

const pendingOrders = computed(() => orders.value.filter((item) => pendingStatuses.includes(item.status)))
const servingOrders = computed(() => orders.value.filter((item) => servingStatuses.includes(item.status)))
const finishedOrders = computed(() => orders.value.filter((item) => finishedStatuses.includes(item.status)))
const visibleOrders = computed(() => {
  if (activeTab.value === 'pending') return pendingOrders.value
  if (activeTab.value === 'serving') return servingOrders.value
  return finishedOrders.value
})

onMounted(load)

async function load() {
  Object.assign(dashboard, await api.technicianDashboard())
  services.value = await api.listServices()
  orders.value = await api.listOrders()
  if (!orders.value.length) {
    orders.value = [await api.currentTechnicianOrder()]
  }
  technicianLocation.value = orders.value.find((item) => item.technicianLocation)?.technicianLocation ?? technicianLocation.value
}

function serviceName(serviceId) {
  return services.value.find((item) => item.id === serviceId)?.name ?? '服务项目'
}

function maskedPhone(phone = '') {
  return phone.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
}

function availableActions(order) {
  return actionMap[order.status] ?? []
}

function isDone(order, status) {
  return order.timeline?.some((item) => item.status === status)
}

async function syncLocation(order) {
  const location = await locate('技师当前位置')
  await api.updateTechnicianBaseLocation(order.technicianId, location)
  await api.updateTechnicianLocation(order.id, location)
  await load()
  flash('技师定位已更新')
}

async function setBaseLocation() {
  const technicianId = orders.value[0]?.technicianId ?? 'sha'
  const location = await locate('技师当前位置')
  technicianLocation.value = location
  await api.updateTechnicianBaseLocation(technicianId, location)
  flash('我的位置已更新，客户端将按新位置计算距离')
}

function callCustomer(order) {
  const phone = order.address?.phone
  if (!phone) {
    flash('客户手机号为空')
    return
  }
  window.location.href = `tel:${phone}`
  flash(`正在联系客户：${phone}`)
}

async function doAction(order, action) {
  try {
    await api.technicianAction(order.id, action)
    await load()
    flash('订单状态已更新')
  } catch (error) {
    flash(error.message)
  }
}

function flash(message) {
  toast.value = message
  window.setTimeout(() => {
    toast.value = ''
  }, 1800)
}

function serviceDuration(serviceId) {
  return services.value.find((item) => item.id === serviceId)?.durationMinutes ?? 0
}

function clockInTime(order) {
  return order.timeline?.filter((item) => item.status === 'CLOCKED_IN').at(-1)?.happenedAt
}

function expectedClockOutTime(order) {
  const startedAt = clockInTime(order)
  const duration = serviceDuration(order.serviceId)
  if (!startedAt || !duration) return ''
  return new Date(new Date(startedAt).getTime() + duration * 60 * 1000).toISOString()
}

function formatTime(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (number) => String(number).padStart(2, '0')
  return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}
</script>
