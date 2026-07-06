<template>
  <article class="service-card">
    <button class="service-card__main" @click="$emit('view', service)">
      <div class="service-card__image-wrap">
      <img class="service-card__image" :src="service.imageUrl" :alt="service.name">
      <span class="service-card__duration">◷ {{ service.durationMinutes }}分钟</span>
      </div>
      <div class="service-card__body">
        <h3>{{ service.name }}</h3>
        <p>{{ service.slogan }}</p>
        <div class="price-row">
          <strong>¥ {{ Number(service.price).toFixed(2) }}</strong>
          <s>¥ {{ Number(service.originalPrice).toFixed(0) }}</s>
          <span>(已售:{{ service.soldCount }})</span>
        </div>
      </div>
    </button>
    <button class="pill-button" @click="$emit('select', service)">选择商户</button>
  </article>
</template>

<script setup>
defineProps({
  service: {
    type: Object,
    required: true
  }
})

defineEmits(['select', 'view'])
</script>
