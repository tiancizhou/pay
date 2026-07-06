<template>
  <MobileShell :tabbar="false">
    <WechatChrome />
    <form class="address-panel" @submit.prevent="save">
      <label>
        <span>姓名</span>
        <input v-model="form.contactName" placeholder="姓名">
      </label>
      <label>
        <span>电话</span>
        <input v-model="form.phone" placeholder="手机号">
      </label>
      <label>
        <span>所在地区</span>
        <input v-model="form.region" placeholder="请选择地区">
        <button type="button" class="inline-location" @click="fillLocation">定位</button>
      </label>
      <label>
        <span>详细地址</span>
        <input v-model="form.detail" placeholder="请输入街道门牌，楼层等信息">
      </label>
      <p v-if="booking.customerLocation" class="address-location">
        当前定位：{{ booking.customerLocation.label }}
      </p>
      <label class="switch-row">
        <span>设置为默认地址</span>
        <input v-model="form.defaultAddress" type="checkbox">
        <i></i>
      </label>
    </form>

    <footer class="address-actions">
      <button class="danger" @click="router.back()">取消</button>
      <button class="primary" @click="save">保存并使用</button>
    </footer>
  </MobileShell>
</template>

<script setup>
import { reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MobileShell from '../../components/MobileShell.vue'
import WechatChrome from '../../components/WechatChrome.vue'
import { locate } from '../../services/location'
import { booking, upsertAddress } from '../../state/booking'

const router = useRouter()
const route = useRoute()
const editingAddress = booking.addresses.find((item) => item.id === route.query.id)
const form = reactive({
  id: editingAddress?.id,
  contactName: editingAddress?.contactName ?? booking.address.contactName,
  phone: editingAddress?.phone ?? booking.address.phone,
  region: editingAddress?.region ?? booking.address.region,
  detail: editingAddress?.detail ?? booking.address.detail,
  defaultAddress: editingAddress?.defaultAddress ?? booking.address.defaultAddress
})

function save() {
  upsertAddress(form)
  router.push(returnPath())
}

function returnPath() {
  if (route.query.from === 'checkout') return '/client/checkout'
  if (route.query.from === 'technicians' && booking.service && booking.technician) return '/client/checkout'
  if (route.query.from === 'technicians') return '/client/addresses?from=technicians'
  return '/client/addresses?from=profile'
}

async function fillLocation() {
  booking.customerLocation = await locate('用户定位')
  form.region = booking.customerLocation.label
  if (!form.detail) {
    form.detail = '请补充门牌、楼层等信息'
  }
}
</script>
