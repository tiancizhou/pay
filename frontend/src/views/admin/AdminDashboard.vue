<template>
  <el-container class="admin-pro">
    <el-aside width="240px" class="admin-pro__aside">
      <div class="admin-pro__brand">
        <strong>温暖到家</strong>
        <span>Merchant Console</span>
      </div>
      <el-menu :default-active="activeMenu" background-color="#172033" text-color="#cbd5e1" active-text-color="#ffffff" @select="activeMenu = $event">
        <el-menu-item v-for="item in menus" :key="item.key" :index="item.key">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-pro__header">
        <div>
          <h1>{{ activeTitle }}</h1>
          <p>商户经营管理、服务维护、订单履约与售后处理</p>
        </div>
        <el-button type="primary" :icon="Refresh" @click="load">刷新数据</el-button>
      </el-header>

      <el-main class="admin-pro__main">
        <el-row :gutter="16" class="admin-pro__metrics">
          <el-col :span="6"><el-card shadow="never"><el-statistic title="今日订单" :value="dashboard.todayOrders" /></el-card></el-col>
          <el-col :span="6"><el-card shadow="never"><el-statistic title="今日收入" :value="Number(dashboard.todayIncome)" prefix="¥" :precision="2" /></el-card></el-col>
          <el-col :span="6"><el-card shadow="never"><el-statistic title="待处理" :value="dashboard.pendingTasks" /></el-card></el-col>
          <el-col :span="6"><el-card shadow="never"><el-statistic title="满意度" :value="dashboard.satisfactionRate" /></el-card></el-col>
        </el-row>

        <el-card v-if="activeMenu === 'overview'" shadow="never">
          <template #header>运营概览</template>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-descriptions title="订单状态" :column="1" border>
                <el-descriptions-item v-for="item in statusSummary" :key="item.label" :label="item.label">{{ item.count }}</el-descriptions-item>
              </el-descriptions>
            </el-col>
            <el-col :span="12">
              <el-space wrap>
                <el-button type="primary" @click="activeMenu = 'services'">维护服务项目</el-button>
                <el-button @click="activeMenu = 'orders'">查看订单</el-button>
                <el-button @click="activeMenu = 'refunds'">处理售后</el-button>
              </el-space>
            </el-col>
          </el-row>
        </el-card>

        <el-card v-if="activeMenu === 'orders'" shadow="never">
          <template #header>订单管理</template>
          <el-table :data="orders" stripe border>
            <el-table-column prop="id" label="订单号" width="130" :formatter="(_, __, value) => shortId(value)" />
            <el-table-column label="服务" width="140">
              <template #default="{ row }">{{ serviceName(row.serviceId) }}</template>
            </el-table-column>
            <el-table-column label="客户" min-width="180">
              <template #default="{ row }">{{ row.address?.contactName }} {{ row.address?.phone }}</template>
            </el-table-column>
            <el-table-column label="金额" width="120">
              <template #default="{ row }">¥ {{ Number(row.payableAmount).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="150">
              <template #default="{ row }"><el-tag>{{ statusText[row.status] ?? row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="创建时间" min-width="180">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card v-if="activeMenu === 'services'" shadow="never">
          <template #header>服务项目管理</template>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-table :data="services" highlight-current-row border @current-change="(row) => row && selectService(row.id)">
                <el-table-column label="项目">
                  <template #default="{ row }">
                    <el-space>
                      <el-avatar shape="square" :src="row.imageUrl" />
                      <span>{{ row.name }}</span>
                    </el-space>
                  </template>
                </el-table-column>
                <el-table-column label="价格" width="100">
                  <template #default="{ row }">¥ {{ Number(row.price).toFixed(0) }}</template>
                </el-table-column>
              </el-table>
            </el-col>
            <el-col :span="16">
              <el-form label-position="top" class="admin-pro__form">
                <el-form-item v-for="field in detailFields" :key="field.key" :label="field.label">
                  <el-input v-model="detailForm[field.key]" type="textarea" :rows="3" :placeholder="field.placeholder" />
                </el-form-item>
                <el-button type="primary" @click="saveDetail">保存商品介绍</el-button>
              </el-form>
            </el-col>
          </el-row>
        </el-card>

        <el-card v-if="activeMenu === 'technicians'" shadow="never">
          <template #header>技师管理</template>
          <el-table :data="technicians" stripe border>
            <el-table-column prop="name" label="姓名" />
            <el-table-column prop="level" label="等级" />
            <el-table-column prop="servedOrders" label="已服务单数" />
            <el-table-column prop="nextAvailableTime" label="最近可约" />
            <el-table-column label="当前位置" min-width="220">
              <template #default="{ row }">{{ row.location?.label ?? '未设置' }}</template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card v-if="activeMenu === 'refunds'" shadow="never">
          <template #header>退款售后</template>
          <el-table :data="refundOrders" stripe border empty-text="暂无退款售后">
            <el-table-column prop="id" label="订单号" :formatter="(_, __, value) => shortId(value)" />
            <el-table-column label="客户"><template #default="{ row }">{{ row.address?.contactName }}</template></el-table-column>
            <el-table-column label="金额"><template #default="{ row }">¥ {{ Number(row.payableAmount).toFixed(2) }}</template></el-table-column>
            <el-table-column label="状态"><template #default="{ row }"><el-tag type="warning">{{ statusText[row.status] ?? row.status }}</el-tag></template></el-table-column>
            <el-table-column label="操作">
              <template #default="{ row }">
                <el-button v-if="row.status === 'REFUND_REQUESTED'" type="primary" link @click="approveRefund(row)">同意退款</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { DataAnalysis, Goods, List, Refresh, Service, Tickets } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { api } from '../../services/api'
import { statusText } from '../../services/status'

const menus = [
  { key: 'overview', label: '运营概览', icon: DataAnalysis },
  { key: 'orders', label: '订单管理', icon: List },
  { key: 'services', label: '服务项目', icon: Goods },
  { key: 'technicians', label: '技师管理', icon: Service },
  { key: 'refunds', label: '退款售后', icon: Tickets }
]
const activeMenu = ref('overview')
const dashboard = reactive({ todayOrders: 0, todayIncome: '0.00', pendingTasks: 0, satisfactionRate: '0%' })
const orders = ref([])
const services = ref([])
const technicians = ref([])
const editingServiceId = ref('')
const detailForm = reactive({ highlights: '', process: '', materials: '', notices: '', contraindications: '', reasons: '' })
const detailFields = [
  { key: 'highlights', label: '项目亮点', placeholder: '每行一个亮点' },
  { key: 'process', label: '服务流程', placeholder: '每行一个步骤' },
  { key: 'materials', label: '服务物料', placeholder: '每行一个物料' },
  { key: 'notices', label: '注意事项', placeholder: '每行一条说明' },
  { key: 'contraindications', label: '服务禁忌', placeholder: '每行一条禁忌' },
  { key: 'reasons', label: '选择理由', placeholder: '每行一条理由' }
]

const activeTitle = computed(() => menus.find((item) => item.key === activeMenu.value)?.label)
const refundOrders = computed(() => orders.value.filter((order) => ['REFUND_REQUESTED', 'REFUNDED'].includes(order.status)))
const statusSummary = computed(() => Object.entries(orders.value.reduce((summary, order) => {
  const label = statusText[order.status] ?? order.status
  summary[label] = (summary[label] ?? 0) + 1
  return summary
}, {})).map(([label, count]) => ({ label, count })))

onMounted(load)

async function load() {
  Object.assign(dashboard, await api.merchantDashboard())
  orders.value = await api.listOrders()
  services.value = await api.listServices()
  technicians.value = await api.listTechnicians(services.value[0]?.id ?? 'tuina')
  if (!editingServiceId.value) editingServiceId.value = services.value[0]?.id ?? ''
  loadDetailForm()
}

function selectService(serviceId) {
  editingServiceId.value = serviceId
  loadDetailForm()
}

function loadDetailForm() {
  const service = services.value.find((item) => item.id === editingServiceId.value)
  if (!service?.detail) return
  for (const field of detailFields) detailForm[field.key] = service.detail[field.key].join('\n')
}

async function saveDetail() {
  const payload = {}
  for (const field of detailFields) payload[field.key] = detailForm[field.key].split('\n').map((item) => item.trim()).filter(Boolean)
  const updated = await api.updateServiceDetail(editingServiceId.value, payload)
  const index = services.value.findIndex((item) => item.id === updated.id)
  if (index >= 0) services.value[index] = updated
  ElMessage.success('商品介绍已保存')
}

async function approveRefund(order) {
  await api.approveRefund(order.id)
  await load()
  ElMessage.success('已同意退款')
}

function serviceName(serviceId) {
  return services.value.find((item) => item.id === serviceId)?.name ?? serviceId
}

function shortId(id) {
  return id?.length > 8 ? id.slice(0, 8) : id
}

function formatTime(value) {
  if (!value) return ''
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}
</script>
