<template>
  <MobileShell>
    <section class="client-tabs">
      <button
        v-for="tab in orderTabs"
        :key="tab.key"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </section>

    <section v-if="filteredOrders.length" class="order-list">
      <article v-for="order in filteredOrders" :key="order.id" class="order-card">
        <header>
          <strong>温暖到家 · {{ statusText[order.status] ?? order.status }}</strong>
          <span>¥ {{ Number(order.payableAmount).toFixed(2) }}</span>
        </header>
        <p>{{ order.address.region }} {{ order.address.detail }}</p>
        <p>预约时间：{{ order.serviceTime }} · 技师：{{ order.technicianId }}</p>
        <div class="order-progress">
          <span
            v-for="node in flowNodes"
            :key="node.status"
            :class="{ done: isDone(order, node.status), current: order.status === node.status }"
          >
            {{ node.label }}
          </span>
        </div>
        <p v-if="order.customerLocation" class="location-line">
          用户定位：{{ order.customerLocation.label }}
        </p>
        <p v-if="order.technicianLocation" class="location-line">
          技师定位：{{ order.technicianLocation.label }}
        </p>
        <footer class="order-actions">
          <button v-if="order.status === 'WAITING_PAYMENT'" @click="pay(order)">模拟支付</button>
          <button v-if="canCancel(order)" class="secondary" @click="cancel(order)">取消订单</button>
          <button v-if="canRefund(order)" class="secondary" @click="refund(order)">申请退款</button>
          <button @click="refreshLocation(order)">更新定位</button>
          <button class="secondary" @click="showTimeline(order)">查看进度</button>
        </footer>
      </article>
    </section>

    <section v-else class="empty-orders">
      <div class="empty-illustration">▱</div>
      <p>暂无订单</p>
      <button @click="router.push('/client')">去预约服务</button>
    </section>

    <div v-if="toast" class="toast">{{ toast }}</div>
  </MobileShell>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import MobileShell from '../../components/MobileShell.vue'
import { api } from '../../services/api'
import { locate } from '../../services/location'
import { orderTabs, statusText } from '../../services/status'

const router = useRouter()
const orders = ref([])
const activeTab = ref('ALL')
const toast = ref('')
const flowNodes = [
  { status: 'WAITING_TECHNICIAN_ACCEPT', label: '接单' },
  { status: 'CONTACTED_CUSTOMER', label: '联系' },
  { status: 'ON_THE_WAY', label: '出发' },
  { status: 'ARRIVED', label: '到达' },
  { status: 'CLOCKED_IN', label: '上钟' },
  { status: 'CLOCKED_OUT', label: '下钟' },
  { status: 'FINISHED', label: '完成' }
]

const filteredOrders = computed(() => {
  const tab = orderTabs.find((item) => item.key === activeTab.value)
  if (!tab || tab.statuses.length === 0) return orders.value
  return orders.value.filter((order) => tab.statuses.includes(order.status))
})

onMounted(loadOrders)

async function loadOrders() {
  orders.value = await api.listOrders()
}

async function pay(order) {
  await api.payOrder(order.id)
  await loadOrders()
  flash('模拟支付成功，资金进入商户账户')
}

async function cancel(order) {
  await api.cancelOrder(order.id)
  await loadOrders()
  flash('订单已取消')
}

async function refund(order) {
  await api.requestRefund(order.id)
  await loadOrders()
  flash('已提交退款申请')
}

async function refreshLocation(order) {
  const location = await locate('用户定位')
  await api.updateCustomerLocation(order.id, location)
  await loadOrders()
  flash('用户定位已更新')
}

function showTimeline(order) {
  const text = order.timeline.map((item) => `${statusText[item.status] ?? item.status}：${item.message}`).join('\n')
  flash(text || '暂无进度')
}

function canCancel(order) {
  return ['WAITING_PAYMENT', 'WAITING_TECHNICIAN_ACCEPT', 'TECHNICIAN_ACCEPTED'].includes(order.status)
}

function canRefund(order) {
  return ['PAID_TO_MERCHANT', 'WAITING_TECHNICIAN_ACCEPT', 'TECHNICIAN_ACCEPTED', 'CONTACTED_CUSTOMER', 'ON_THE_WAY'].includes(order.status)
}

function isDone(order, status) {
  return order.timeline.some((item) => item.status === status)
}

function flash(message) {
  toast.value = message
  window.setTimeout(() => {
    toast.value = ''
  }, 2200)
}
</script>
