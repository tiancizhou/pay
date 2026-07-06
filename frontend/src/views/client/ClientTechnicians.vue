<template>
  <MobileShell>
    <section class="client-tab-page">
      <section class="merchant-search">
        <button class="city-button" @click="refreshLocation">
          <strong>{{ displayCity }}</strong>
          <span>{{ displayDistrict }}</span>
        </button>
        <label>
          <span>⌕</span>
          <input placeholder="请输入商户姓名">
        </label>
      </section>

      <section v-if="showLocationGuide" class="location-guide">
        <button class="location-guide__close" @click="dismissGuide">×</button>
        <h2>未填写地址</h2>
        <p>请填写常用地址，方便预约</p>
        <div class="location-guide__row">
          <span>◎</span>
          <span>开启定位，到达时效更准确</span>
          <button :disabled="locating" @click="refreshLocation">{{ locating ? '定位中...' : '开启定位 ›' }}</button>
        </div>
        <button class="location-guide__fill" @click="router.push('/client/addresses?from=technicians')">去填写地址</button>
      </section>

      <section class="phone-strip">
        <div>
          <span>本地商家客服电话</span>
          <strong>{{ merchantServicePhone }}</strong>
        </div>
        <button @click="callMerchantService">拨打</button>
      </section>

      <section v-if="canShowTechnicians" class="technician-list">
        <TechnicianCard
          v-for="technician in technicians"
          :key="technician.id"
          :technician="technician"
          @reserve="reserve"
        />
      </section>

      <section v-else class="location-empty">
        <div class="map-pin">
          <span></span>
        </div>
        <p>未获取到定位请开启定位</p>
        <button :disabled="locating" @click="refreshLocation">{{ locating ? '定位中...' : '去开启' }}</button>
      </section>
    </section>
    <div v-if="toast" class="toast toast--inline">{{ toast }}</div>
  </MobileShell>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MobileShell from '../../components/MobileShell.vue'
import TechnicianCard from '../../components/TechnicianCard.vue'
import { api } from '../../services/api'
import { addressFieldsFromLocationLabel, cityFromRegion, locate, locationPartsFromRegion } from '../../services/location'
import { booking } from '../../state/booking'

const route = useRoute()
const router = useRouter()
const technicians = ref([])
const guideDismissed = ref(false)
const locating = ref(false)
const toast = ref('')
const merchantServicePhone = ref('18069906336')
const hasAddress = computed(() => Boolean(booking.address.region && booking.address.detail))
const hasLocation = computed(() => Boolean(booking.customerLocation))
const currentCity = computed(() => {
  if (booking.customerLocation?.label) return booking.customerLocation.label
  if (hasAddress.value) return cityFromRegion(booking.address.region)
  return '未知'
})
const displayCity = computed(() => locationPartsFromRegion(currentCity.value).city)
const displayDistrict = computed(() => locationPartsFromRegion(currentCity.value).district)
const showLocationGuide = computed(() => !guideDismissed.value && !hasAddress.value && !hasLocation.value)
const canShowTechnicians = computed(() => hasAddress.value || hasLocation.value)

onMounted(async () => {
  const [settings, services] = await Promise.all([
    api.clientSiteSettings(),
    api.listServices()
  ])
  merchantServicePhone.value = settings?.merchantServicePhone || merchantServicePhone.value
  booking.service = services.find((item) => item.id === route.params.serviceId) ?? services[0]
  technicians.value = await api.listTechnicians(route.params.serviceId, booking.customerLocation)
})

function reserve(technician) {
  booking.technician = technician
  router.push('/client/address?from=technicians')
}

async function refreshLocation() {
  locating.value = true
  toast.value = '正在获取定位，请允许浏览器定位权限'
  try {
    booking.customerLocation = await locate('当前位置')
    guideDismissed.value = true
    const fields = addressFieldsFromLocationLabel(booking.customerLocation.label)
    booking.address.region = fields.region
    if (fields.detail && !booking.address.detail) {
      booking.address.detail = fields.detail
    }
    technicians.value = await api.listTechnicians(route.params.serviceId, booking.customerLocation)
    toast.value = `定位成功：${booking.customerLocation.label}`
  } finally {
    locating.value = false
    window.setTimeout(() => {
      toast.value = ''
    }, 2200)
  }
}

function dismissGuide() {
  guideDismissed.value = true
}

function callMerchantService() {
  window.location.href = `tel:${merchantServicePhone.value.replace(/\s/g, '')}`
}
</script>
