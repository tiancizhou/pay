<template>
  <MobileShell :tabbar="false" class="tech-shell">
    <main class="tech-app">
      <header class="tech-header">
        <div>
          <span>{{ headerMeta }}</span>
          <h1>{{ headerTitle }}</h1>
        </div>
        <button class="tech-status-toggle" type="button" :class="{ active: online }" @click="online = !online">
          <span></span>
          {{ online ? '接单中' : '已休息' }}
        </button>
      </header>

      <section :class="pageClass('workbench')">
        <section class="tech-summary">
          <article>
            <span>今日服务</span>
            <strong>{{ dashboard.todayOrders }}</strong>
          </article>
          <article>
            <span>待接单</span>
            <strong>{{ pendingOrders.length }}</strong>
          </article>
          <article>
            <span>服务中</span>
            <strong>{{ servingOrders.length }}</strong>
          </article>
        </section>

        <section class="tech-location-strip">
          <div>
            <span>当前位置</span>
            <strong>{{ technicianLocation?.label ?? '未设置技师位置' }}</strong>
          </div>
          <div class="tech-location-actions">
            <button type="button" @click="setBaseLocation">定位</button>
            <button type="button" class="secondary" @click="openManualLocation">添加</button>
          </div>
        </section>

        <section class="tech-section-head">
          <div>
            <span>当前任务</span>
            <h2>{{ currentOrder ? serviceName(currentOrder.serviceId) : '暂无进行中订单' }}</h2>
          </div>
          <van-tag v-if="currentOrder" color="#e8f6f2" text-color="#0b7f6b">
            {{ statusText[currentOrder.status] ?? currentOrder.status }}
          </van-tag>
        </section>

        <OrderTaskCard
          v-if="currentOrder"
          :order="currentOrder"
          primary
          @sync-location="syncLocation"
          @call-customer="callCustomer"
          @action="doAction"
        />

        <section v-else class="tech-empty-panel">
          <van-icon name="orders-o" />
          <strong>当前没有进行中的订单</strong>
          <span>新订单会进入订单页的待接单列表</span>
        </section>
      </section>

      <section :class="pageClass('orders')">
        <section class="tech-summary">
          <article>
            <span>待接单</span>
            <strong>{{ pendingOrders.length }}</strong>
          </article>
          <article>
            <span>服务中</span>
            <strong>{{ servingOrders.length }}</strong>
          </article>
          <article>
            <span>已完成</span>
            <strong>{{ finishedOrders.length }}</strong>
          </article>
        </section>

        <van-tabs v-model:active="orderTab" color="#159f86" title-active-color="#0b7f6b">
          <van-tab title="待接单" name="pending" />
          <van-tab title="服务中" name="serving" />
          <van-tab title="已完成" name="finished" />
        </van-tabs>

        <section class="tech-order-list">
          <OrderTaskCard
            v-for="item in visibleOrders"
            :key="item.id"
            :order="item"
            @sync-location="syncLocation"
            @call-customer="callCustomer"
            @action="doAction"
          />
          <section v-if="!visibleOrders.length" class="tech-empty-panel">
            <van-icon name="completed-o" />
            <strong>暂无{{ orderTabLabel }}订单</strong>
            <span>订单状态变化后会自动归类到这里</span>
          </section>
        </section>
      </section>

      <section :class="pageClass('profile')">
        <section class="tech-profile-card">
          <label class="tech-avatar-uploader" :class="{ loading: avatarUploading }">
            <van-image round width="64" height="64" :src="profileAvatar">
              <template #error>
                <div class="tech-avatar-fallback">{{ technicianInitial }}</div>
              </template>
            </van-image>
            <input type="file" accept="image/jpeg,image/png,image/webp" @change="uploadProfileAvatar">
            <span>{{ avatarUploading ? '上传中' : '更换' }}</span>
          </label>
          <div>
            <strong>{{ technicianName }}</strong>
            <span>{{ online ? '当前可接单' : '休息中' }}</span>
          </div>
        </section>

        <van-cell-group inset>
          <van-cell title="我的位置" :label="technicianLocation?.label ?? '未设置'" is-link @click="openManualLocation" />
          <van-cell title="定位更新" label="使用浏览器定位刷新当前位置" is-link @click="setBaseLocation" />
          <van-cell title="联系电话" :label="currentOrder?.address?.phone || '暂无当前客户'" />
          <van-cell title="服务范围" :label="serviceScopeText" />
          <van-cell title="刷新数据" is-link @click="load" />
          <van-cell title="退出登录" is-link @click="logout" />
        </van-cell-group>
      </section>

      <van-popup
        v-model:show="manualLocationVisible"
        class="tech-location-popup"
        round
        position="bottom"
        safe-area-inset-bottom
      >
        <section class="tech-location-editor">
          <header>
            <strong>位置校准</strong>
            <button type="button" @click="manualLocationVisible = false">关闭</button>
          </header>
          <section class="tech-location-preview">
            <van-icon name="location-o" />
            <div>
              <span>当前定位</span>
              <strong>{{ draftLocation?.label ?? '点击自动定位获取当前位置' }}</strong>
              <small v-if="draftLocation">
                {{ draftLocation.latitude }}, {{ draftLocation.longitude }}
              </small>
            </div>
          </section>
          <button class="tech-location-editor__locate" type="button" :disabled="locationLoading" @click="refreshDraftLocation">
            {{ locationLoading ? '定位中...' : '自动定位' }}
          </button>
          <van-field
            v-model="manualLocationForm.label"
            label="位置名称"
            placeholder="可选，例如 武进区门店"
            clearable
          />
          <p>坐标由浏览器定位自动获取，位置名称只用于后台和订单页展示，不再手填经纬度。</p>
          <button class="tech-location-editor__submit" type="button" :disabled="!draftLocation" @click="saveManualLocation">保存定位</button>
        </section>
      </van-popup>

      <van-tabbar v-model="activeTab" safe-area-inset-bottom active-color="#159f86" inactive-color="#7a8691">
        <van-tabbar-item name="workbench" icon="wap-home-o">工作台</van-tabbar-item>
        <van-tabbar-item name="orders" icon="orders-o" :badge="pendingOrders.length || ''">订单</van-tabbar-item>
        <van-tabbar-item name="profile" icon="manager-o">我的</van-tabbar-item>
      </van-tabbar>

      <div v-if="toast" class="toast">{{ toast }}</div>
    </main>
  </MobileShell>
</template>

<script setup>
import { computed, defineComponent, h, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import MobileShell from '../../components/MobileShell.vue'
import { api } from '../../services/api'
import { clearPortalUser, getPortalUser } from '../../services/auth'
import { locate } from '../../services/location'
import { statusText } from '../../services/status'

const router = useRouter()
const dashboard = reactive({
  todayOrders: 0
})
const services = ref([])
const orders = ref([])
const technicians = ref([])
const currentUser = ref(null)
const activeTab = ref('workbench')
const orderTab = ref('pending')
const toast = ref('')
const technicianLocation = ref(null)
const online = ref(true)
const manualLocationVisible = ref(false)
const manualLocationForm = reactive({
  label: ''
})
const draftLocation = ref(null)
const locationLoading = ref(false)
const avatarUploading = ref(false)

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
const currentOrder = computed(() => servingOrders.value[0] ?? pendingOrders.value[0] ?? null)
const visibleOrders = computed(() => {
  if (orderTab.value === 'pending') return pendingOrders.value
  if (orderTab.value === 'serving') return servingOrders.value
  return finishedOrders.value
})
const headerTitle = computed(() => {
  if (activeTab.value === 'orders') return '订单中心'
  if (activeTab.value === 'profile') return '我的'
  return '技师工作台'
})
const headerMeta = computed(() => online.value ? '今日保持在线，注意及时处理订单' : '休息中，开启后可继续接单')
const orderTabLabel = computed(() => ({ pending: '待接单', serving: '服务中', finished: '已完成' }[orderTab.value]))
const currentTechnicianId = computed(() => currentUser.value?.technicianId ?? currentOrder.value?.technicianId ?? '')
const currentTechnician = computed(() => technicians.value.find((item) => item.id === currentTechnicianId.value) ?? null)
const technicianName = computed(() => currentTechnician.value?.name ?? technicianNameById(currentTechnicianId.value))
const technicianInitial = computed(() => technicianName.value.slice(0, 1) || '技')
const profileAvatar = computed(() => currentTechnician.value?.portraitUrl ?? '')
const serviceScopeText = computed(() => services.value.map((item) => item.name).slice(0, 3).join('、') || '暂无服务范围')

const OrderTaskCard = defineComponent({
  name: 'OrderTaskCard',
  props: {
    order: { type: Object, required: true },
    primary: { type: Boolean, default: false }
  },
  emits: ['sync-location', 'call-customer', 'action'],
  setup(props, { emit }) {
    return () => h('article', { class: ['tech-task-card', { primary: props.primary }] }, [
      h('header', [
        h('div', [
          h('span', '服务项目'),
          h('strong', serviceName(props.order.serviceId))
        ]),
        h('em', statusText[props.order.status] ?? props.order.status)
      ]),
      h('section', { class: 'tech-task-info' }, [
        h('p', [h('span', '预约'), h('strong', props.order.serviceTime || '-')]),
        h('p', [h('span', '客户'), h('strong', `${props.order.address?.contactName ?? '-'} ${props.order.address?.phone ?? ''}`.trim())]),
        h('p', [h('span', '地址'), h('strong', `${props.order.address?.region ?? ''} ${props.order.address?.detail ?? ''}`.trim() || '-')]),
        h('p', [h('span', '时长'), h('strong', `${serviceDuration(props.order.serviceId)} 分钟`)])
      ]),
      h('ol', { class: 'tech-flow' }, flowSteps.map((step) => h('li', {
        class: { done: isDone(props.order, step.status), current: props.order.status === step.status }
      }, step.label))),
      h('section', { class: 'tech-time-panel' }, [
        h('p', [h('span', '上钟时间'), h('strong', clockInTime(props.order) ? formatTime(clockInTime(props.order)) : '待上钟')]),
        h('p', [h('span', '预计下钟'), h('strong', expectedClockOutTime(props.order) ? formatTime(expectedClockOutTime(props.order)) : '-')])
      ]),
      props.order.customerLocation ? h('p', { class: 'tech-location-line' }, `客户定位：${props.order.customerLocation.label}`) : null,
      props.order.technicianLocation ? h('p', { class: 'tech-location-line' }, `我的定位：${props.order.technicianLocation.label}`) : null,
      h('footer', [
        h('button', { class: 'secondary', type: 'button', onClick: () => emit('sync-location', props.order) }, '更新定位'),
        h('button', { class: 'secondary', type: 'button', onClick: () => emit('call-customer', props.order) }, '联系客户'),
        ...availableActions(props.order).map((action) => {
          const disabled = isActionDisabled(props.order, action.value)
          return h('button', {
            type: 'button',
            disabled,
            title: disabled ? clockOutHint(props.order) : '',
            onClick: () => {
              if (disabled) return
              emit('action', props.order, action.value)
            }
          }, disabled ? clockOutHint(props.order) : action.label)
        })
      ])
    ])
  }
})

onMounted(load)

watch(activeTab, async () => {
  await nextTick()
  document.querySelector('.tech-page.active')?.scrollTo({ top: 0 })
  window.scrollTo({ top: 0 })
})

function pageClass(name) {
  return ['tech-page', { active: activeTab.value === name }]
}

async function load() {
  const nextUser = getPortalUser()
  currentUser.value = nextUser
  const technicianId = nextUser?.technicianId
  if (!technicianId) {
    flash('当前账号未绑定技师资料')
    return
  }
  const [nextDashboard, nextServices, nextOrders, nextTechnicians] = await Promise.all([
    api.technicianDashboard(),
    api.listServices(),
    api.listTechnicianOrders(technicianId),
    api.listAllTechnicians()
  ])
  Object.assign(dashboard, nextDashboard)
  services.value = nextServices
  orders.value = nextOrders
  technicians.value = nextTechnicians
  technicianLocation.value = orders.value.find((item) => item.technicianLocation)?.technicianLocation ?? technicianLocation.value
}

function serviceName(serviceId) {
  return services.value.find((item) => item.id === serviceId)?.name ?? '服务项目'
}

function technicianNameById(technicianId) {
  return technicianId === 'sha' ? '沙鑫鑫' : technicianId
}

function availableActions(order) {
  return actionMap[order.status] ?? []
}

function isActionDisabled(order, action) {
  if (action !== 'CLOCK_OUT') return false
  const expected = expectedClockOutTime(order)
  return Boolean(expected && Date.now() < new Date(expected).getTime())
}

function clockOutHint(order) {
  const expected = expectedClockOutTime(order)
  return expected ? `${formatTime(expected)} 后下钟` : '未到下钟时间'
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
  const technicianId = currentTechnicianId.value
  const location = await locate('技师当前位置')
  technicianLocation.value = location
  await api.updateTechnicianBaseLocation(technicianId, location)
  flash('我的位置已更新')
}

async function openManualLocation() {
  manualLocationForm.label = technicianLocation.value?.label ?? ''
  draftLocation.value = technicianLocation.value
  manualLocationVisible.value = true
  if (!draftLocation.value) {
    await refreshDraftLocation()
  }
}

async function refreshDraftLocation() {
  locationLoading.value = true
  try {
    draftLocation.value = await locate('技师当前位置')
    if (!manualLocationForm.label) {
      manualLocationForm.label = draftLocation.value.label
    }
  } finally {
    locationLoading.value = false
  }
}

async function saveManualLocation() {
  if (!draftLocation.value) {
    flash('请先完成自动定位')
    return
  }
  const label = manualLocationForm.label.trim()
  const location = { ...draftLocation.value, label: label || draftLocation.value.label }
  const technicianId = currentTechnicianId.value
  technicianLocation.value = location
  await api.updateTechnicianBaseLocation(technicianId, location)
  manualLocationVisible.value = false
  flash('地址已保存')
}

async function uploadProfileAvatar(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  avatarUploading.value = true
  try {
    const updated = await api.updateTechnicianAvatar(currentTechnicianId.value, file)
    technicians.value = technicians.value.map((item) => item.id === updated.id ? updated : item)
    if (!technicians.value.some((item) => item.id === updated.id)) {
      technicians.value.push(updated)
    }
    flash('头像已更新')
  } catch (error) {
    flash(error.message)
  } finally {
    avatarUploading.value = false
  }
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

function logout() {
  clearPortalUser()
  router.replace('/')
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
