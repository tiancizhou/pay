<template>
  <MobileShell :tabbar="false">
    <section class="console-hero merchant-hero">
      <p>商户端</p>
      <h1>门店履约中心</h1>
      <span>订单、服务、技师排班统一管理</span>
    </section>
    <section class="metric-grid">
      <article><span>今日订单</span><strong>{{ dashboard.todayOrders }}</strong></article>
      <article><span>今日收入</span><strong>¥ {{ dashboard.todayIncome }}</strong></article>
      <article><span>待处理</span><strong>{{ dashboard.pendingTasks }}</strong></article>
      <article><span>满意度</span><strong>{{ dashboard.satisfactionRate }}</strong></article>
    </section>
    <section class="console-card">
      <h2>商户工作台</h2>
      <button @click="activePanel = 'services'">服务项目管理</button>
      <button @click="activePanel = 'schedule'">技师排班管理</button>
      <button @click="activePanel = 'orders'">订单核销</button>
      <button @click="activePanel = 'refund'">评价与售后</button>
      <div class="console-panel">
        <strong>{{ panelTitle }}</strong>
        <p>{{ panelContent }}</p>
        <button v-if="refundableOrder" class="secondary" @click="approveRefund">模拟同意退款</button>
      </div>
    </section>
    <section v-if="activePanel === 'services'" class="console-card detail-editor">
      <h2>商品介绍编辑</h2>
      <label>
        服务项目
        <select v-model="editingServiceId" @change="loadDetailForm">
          <option v-for="service in services" :key="service.id" :value="service.id">{{ service.name }}</option>
        </select>
      </label>
      <label v-for="field in detailFields" :key="field.key">
        {{ field.label }}
        <textarea v-model="detailForm[field.key]" rows="3" :placeholder="field.placeholder"></textarea>
      </label>
      <button @click="saveDetail">保存商品介绍</button>
    </section>
    <div v-if="toast" class="toast">{{ toast }}</div>
  </MobileShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import MobileShell from '../../components/MobileShell.vue'
import { api } from '../../services/api'
import { statusText } from '../../services/status'

const dashboard = reactive({
  todayOrders: 0,
  todayIncome: '0.00',
  pendingTasks: 0,
  satisfactionRate: '0%'
})
const activePanel = ref('services')
const orders = ref([])
const toast = ref('')
const services = ref([])
const editingServiceId = ref('')
const detailForm = reactive({
  highlights: '',
  process: '',
  materials: '',
  notices: '',
  contraindications: '',
  reasons: ''
})
const detailFields = [
  { key: 'highlights', label: '项目亮点', placeholder: '每行一个亮点' },
  { key: 'process', label: '服务流程', placeholder: '每行一个步骤' },
  { key: 'materials', label: '服务物料', placeholder: '每行一个物料' },
  { key: 'notices', label: '注意事项', placeholder: '每行一条说明' },
  { key: 'contraindications', label: '服务禁忌', placeholder: '每行一条禁忌' },
  { key: 'reasons', label: '选择理由', placeholder: '每行一条理由' }
]
const refundableOrder = computed(() => orders.value.find((order) => order.status === 'REFUND_REQUESTED'))
const panelTitle = computed(() => ({
  services: '服务项目',
  schedule: '技师排班',
  orders: '订单核销',
  refund: '售后退款'
})[activePanel.value])
const panelContent = computed(() => {
  if (activePanel.value === 'services') return '可维护服务名称、时长、售价、上下架状态。当前原型已接入服务列表。'
  if (activePanel.value === 'schedule') return '可查看技师在线状态、接单状态和当前位置。'
  if (activePanel.value === 'orders') return orders.value.length ? `共 ${orders.value.length} 单，最新状态：${statusText[orders.value[0].status]}` : '暂无可核销订单。'
  return refundableOrder.value ? `订单 ${refundableOrder.value.id} 正在申请退款。` : '暂无退款申请。'
})

onMounted(async () => {
  Object.assign(dashboard, await api.merchantDashboard())
  orders.value = await api.listOrders()
  services.value = await api.listServices()
  editingServiceId.value = services.value[0]?.id ?? ''
  loadDetailForm()
})

async function approveRefund() {
  if (!refundableOrder.value) return
  await api.approveRefund(refundableOrder.value.id)
  orders.value = await api.listOrders()
  flash('已模拟同意退款')
}

function flash(message) {
  toast.value = message
  window.setTimeout(() => {
    toast.value = ''
  }, 1800)
}

function loadDetailForm() {
  const service = services.value.find((item) => item.id === editingServiceId.value)
  if (!service?.detail) return
  for (const field of detailFields) {
    detailForm[field.key] = service.detail[field.key].join('\n')
  }
}

async function saveDetail() {
  const payload = {}
  for (const field of detailFields) {
    payload[field.key] = detailForm[field.key].split('\n').map((item) => item.trim()).filter(Boolean)
  }
  const updated = await api.updateServiceDetail(editingServiceId.value, payload)
  const index = services.value.findIndex((item) => item.id === updated.id)
  if (index >= 0) services.value[index] = updated
  flash('商品介绍已保存')
}
</script>
