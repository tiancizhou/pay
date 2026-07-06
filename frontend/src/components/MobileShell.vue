<template>
  <main class="mobile-shell" :class="{ 'with-tabbar': tabbar }">
    <slot />
    <nav v-if="tabbar" class="tabbar">
      <RouterLink to="/client" class="tabbar__item">
        <span>⌂</span>
        首页
      </RouterLink>
      <RouterLink :to="merchantRoute" class="tabbar__item">
        <span>♟</span>
        商户
      </RouterLink>
      <RouterLink to="/client/orders" class="tabbar__item">
        <span>▤</span>
        订单
      </RouterLink>
      <RouterLink to="/client/profile" class="tabbar__item">
        <span>●</span>
        我的
      </RouterLink>
    </nav>
  </main>
</template>

<script setup>
import { computed } from 'vue'
import { booking } from '../state/booking'

defineProps({
  tabbar: {
    type: Boolean,
    default: true
  }
})

const merchantRoute = computed(() => `/client/services/${booking.service?.id ?? 'tuina'}/technicians`)
</script>
