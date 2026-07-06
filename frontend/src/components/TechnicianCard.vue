<template>
  <article class="technician-card">
    <div class="technician-card__badge">{{ technician.level }}</div>
    <div class="portrait-frame">
      <img :src="technician.portraitUrl" :alt="technician.name">
      <span>可预约</span>
    </div>
    <section class="technician-card__info">
      <h3>
        {{ technician.name }}
        <span class="merchant-chip">商</span>
        <small>商家</small>
      </h3>
      <p class="distance">● {{ technician.distanceKm.toFixed(2) }}km</p>
      <p>已服务{{ technician.servedOrders }}单</p>
    </section>
    <aside class="technician-card__action">
      <div class="time-box">最近可约<br><strong>{{ technician.nextAvailableTime }}</strong></div>
      <button class="reserve-button" @click="$emit('reserve', technician)">预约</button>
      <span class="free-tag">1公里内免费</span>
    </aside>
  </article>
</template>

<script setup>
defineProps({
  technician: {
    type: Object,
    required: true
  }
})

defineEmits(['reserve'])
</script>
