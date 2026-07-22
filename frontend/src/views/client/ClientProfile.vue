<template>
  <MobileShell>
    <section class="client-tab-page">
      <section class="profile-head">
        <div class="avatar">MAX</div>
        <strong>MAX</strong>
      </section>

      <section class="profile-stats">
        <button @click="flash('暂无可用优惠券')">
          <strong>0</strong>
          <span>优惠券</span>
        </button>
      </section>

      <section class="profile-order-shortcuts">
        <button @click="router.push('/client/orders')">
          <span>▰</span>
          待接单
        </button>
        <button @click="router.push('/client/orders')">
          <span>◩</span>
          待服务
        </button>
        <button @click="router.push('/client/orders')">
          <span>●</span>
          已完成
        </button>
        <button @click="router.push('/client/orders')">
          <span>▱</span>
          退换/售后
        </button>
        <button @click="router.push('/client/orders')">
          <span>▤</span>
          全部订单
        </button>
      </section>

      <section class="profile-menu">
        <button @click="router.push('/client/addresses?from=profile')"><span>♧</span>地址管理<i>›</i></button>
        <button @click="logout"><span>↩</span>退出登录<i>›</i></button>
      </section>
    </section>

    <div v-if="toast" class="toast">{{ toast }}</div>
  </MobileShell>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import MobileShell from '../../components/MobileShell.vue'
import { api } from '../../services/api'
import { clearPortalUser } from '../../services/auth'

const router = useRouter()
const toast = ref('')

async function logout() {
  try {
    await api.wechatLogout()
  } finally {
    clearPortalUser()
    await router.replace('/')
  }
}

function flash(message) {
  toast.value = message
  window.setTimeout(() => {
    toast.value = ''
  }, 1800)
}
</script>
