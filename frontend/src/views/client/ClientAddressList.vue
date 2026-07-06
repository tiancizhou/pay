<template>
  <MobileShell :tabbar="false">
    <WechatChrome />
    <section class="address-list-page">
      <header>
        <h1>地址管理</h1>
        <button @click="router.push(`/client/address?from=${from}`)">新增地址</button>
      </header>

      <section v-if="addresses.length" class="address-list">
        <article v-for="address in addresses" :key="address.id" class="address-list-card">
          <button class="address-list-card__main" @click="useAddress(address)">
            <strong>{{ address.contactName }} {{ address.phone }}</strong>
            <span>{{ address.region }} {{ address.detail }}</span>
            <em v-if="address.defaultAddress">默认地址</em>
          </button>
          <footer>
            <button @click="setDefault(address)">设为默认</button>
            <button @click="router.push(`/client/address?id=${address.id}&from=${from}`)">编辑</button>
          </footer>
        </article>
      </section>

      <section v-else class="empty-orders">
        <div class="empty-illustration">⌖</div>
        <p>暂无地址</p>
        <button @click="router.push(`/client/address?from=${from}`)">去新增地址</button>
      </section>
    </section>
  </MobileShell>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MobileShell from '../../components/MobileShell.vue'
import WechatChrome from '../../components/WechatChrome.vue'
import { booking, selectAddress, upsertAddress } from '../../state/booking'

const route = useRoute()
const router = useRouter()
const from = computed(() => route.query.from ?? 'profile')
const addresses = computed(() => booking.addresses)

function useAddress(address) {
  selectAddress(address)
  if (from.value === 'checkout') {
    router.push('/client/checkout')
    return
  }
  if (from.value === 'technicians' && booking.service && booking.technician) {
    router.push('/client/checkout')
    return
  }
  router.push('/client/profile')
}

function setDefault(address) {
  upsertAddress({ ...address, defaultAddress: true })
}
</script>
