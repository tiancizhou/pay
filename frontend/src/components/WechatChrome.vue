<template>
  <header class="wechat-chrome">
    <button class="wechat-chrome__close" @click="goBack">×</button>
    <div>
      <strong>温暖到家</strong>
      <small>plat.wndaojia.com</small>
    </div>
    <span class="wechat-chrome__more">...</span>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/client')
}
</script>
