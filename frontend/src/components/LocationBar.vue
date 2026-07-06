<template>
  <section class="location-bar">
    <div>
      <strong>{{ title }}</strong>
      <span>{{ locationText }}</span>
    </div>
    <button :disabled="loading" @click="refresh">
      {{ loading ? '定位中...' : '重新定位' }}
    </button>
  </section>
  <div v-if="toast" class="toast">{{ toast }}</div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { cityFromRegion, locate } from '../services/location'
import { booking } from '../state/booking'

const loading = ref(false)
const toast = ref('')
const title = computed(() => cityFromRegion(booking.address.region))
const locationText = computed(() => {
  if (!booking.customerLocation) return '点击获取当前位置，用于筛选附近技师'
  return booking.customerLocation.label
})

async function refresh() {
  loading.value = true
  toast.value = '正在获取定位，请允许浏览器定位权限'
  try {
    booking.customerLocation = await locate('当前位置')
    toast.value = `定位成功：${booking.customerLocation.label}`
  } finally {
    loading.value = false
    window.setTimeout(() => {
      toast.value = ''
    }, 2200)
  }
}
</script>
