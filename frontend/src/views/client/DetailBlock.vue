<template>
  <section class="detail-block">
    <h2>{{ title }}</h2>
    <slot />
  </section>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  }
})
</script>
