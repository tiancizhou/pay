<template>
  <MobileShell>
    <LocationBar />
    <section class="sort-bar">
      <button :class="{ 'sort-bar__active': sortBy === 'default' }" @click="applySort('default')">综合排序</button>
      <button :class="{ 'sort-bar__active': sortBy === 'price' }" @click="applySort('price')">价格 {{ priceAsc ? '↑' : '↓' }}</button>
      <button :class="{ 'sort-bar__active': sortBy === 'sold' }" @click="applySort('sold')">销量 {{ soldAsc ? '↑' : '↓' }}</button>
    </section>

    <section class="service-list">
      <ServiceCard
        v-for="service in services"
        :key="service.id"
        :service="service"
        @select="selectService"
        @view="viewService"
      />
    </section>
  </MobileShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import MobileShell from '../../components/MobileShell.vue'
import LocationBar from '../../components/LocationBar.vue'
import ServiceCard from '../../components/ServiceCard.vue'
import { api } from '../../services/api'
import { booking } from '../../state/booking'

const router = useRouter()
const services = ref([])
const originalServices = ref([])
const sortBy = ref('default')
const priceAsc = ref(true)
const soldAsc = ref(true)

onMounted(async () => {
  originalServices.value = await api.listServices()
  services.value = [...originalServices.value]
})

function selectService(service) {
  booking.service = service
  router.push(`/client/services/${service.id}/technicians`)
}

function viewService(service) {
  booking.service = service
  router.push(`/client/services/${service.id}`)
}

function applySort(type) {
  if (type === 'price' && sortBy.value === 'price') {
    priceAsc.value = !priceAsc.value
  }
  if (type === 'sold' && sortBy.value === 'sold') {
    soldAsc.value = !soldAsc.value
  }
  sortBy.value = type
  const next = [...originalServices.value]
  if (type === 'price') {
    next.sort((a, b) => priceAsc.value ? Number(a.price) - Number(b.price) : Number(b.price) - Number(a.price))
  }
  if (type === 'sold') {
    next.sort((a, b) => soldAsc.value ? a.soldCount - b.soldCount : b.soldCount - a.soldCount)
  }
  services.value = type === 'default' ? [...originalServices.value] : next
}
</script>
