<template>
  <MobileShell :tabbar="false">
    <section class="promise-strip">
      <span>✓ 不满意重复服务</span>
      <span>✓ 满意为止</span>
    </section>

    <section class="checkout-card">
      <h2>服务地址：</h2>
      <button class="address-summary" @click="router.push('/client/addresses?from=checkout')">
        <strong>{{ booking.address.contactName }} {{ maskedPhone }}</strong>
        <span>地址： {{ booking.address.region }} {{ booking.address.detail }}</span>
        <em>提示:平台保护用户个人隐私，您的电话号码不会展示给任何人。</em>
        <b>›</b>
      </button>
      <button class="location-button" @click="refreshCustomerLocation">定位当前位置</button>
      <p v-if="booking.customerLocation" class="location-line">
        {{ booking.customerLocation.label }}
      </p>
    </section>

    <section v-if="booking.service" class="checkout-service">
      <img :src="booking.service.imageUrl" :alt="booking.service.name">
      <div>
        <h3>{{ booking.service.name }}</h3>
        <p>{{ booking.service.slogan }}</p>
        <strong>¥ {{ payableServicePrice }}</strong>
        <s>¥ {{ Number(booking.service.originalPrice).toFixed(0) }}</s>
      </div>
    </section>

    <section class="checkout-card order-form">
      <p><strong>服务商户：</strong><span>{{ booking.technician?.name ?? '沙鑫鑫' }}</span></p>
      <p><strong>服务时间：</strong><button @click="chooseTime">{{ booking.serviceTime }} ›</button></p>
      <p><strong>出行方式：</strong><button class="checked" @click="toggleTravel">✓ {{ travelMode }}</button></p>
      <small>全程约{{ distanceKm }}公里，1公里内免费，超过1公里每公里增加4元</small>
      <p><span>车费</span><strong>¥ {{ travelFee }}</strong></p>
      <p><span>优惠券</span><button class="coupon" @click="pickCoupon">券 {{ couponUsed ? '已减10元' : '0张可用' }} ›</button></p>
      <p><span>备注</span><button @click="editRemark">{{ remark || '备注' }}</button></p>
    </section>

    <label class="agreement">
      <input v-model="agreed" type="checkbox">
      我已阅读并同意 《下单须知和用户使用协议以及隐私政策》
    </label>

    <section v-if="booking.latestOrder" class="checkout-card status-card">
      <h2>服务进度</h2>
      <div class="status-current">{{ statusText[booking.latestOrder.status] ?? booking.latestOrder.status }}</div>
      <ol>
        <li v-for="item in booking.latestOrder.timeline" :key="item.happenedAt">
          <span>{{ statusText[item.status] ?? item.status }}</span>
          <em>{{ item.message }}</em>
        </li>
      </ol>
      <p v-if="booking.latestOrder.technicianLocation" class="location-line">
        技师定位：{{ booking.latestOrder.technicianLocation.label }}
      </p>
    </section>

    <footer class="pay-bar">
      <span>待支付 <strong>¥ {{ totalAmount }}</strong></span>
      <button :disabled="!agreed" @click="submit">模拟支付</button>
    </footer>
    <div v-if="toast" class="toast">{{ toast }}</div>
  </MobileShell>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import MobileShell from '../../components/MobileShell.vue'
import { api } from '../../services/api'
import { locate } from '../../services/location'
import { statusText } from '../../services/status'
import { booking } from '../../state/booking'

const router = useRouter()
const agreed = ref(false)
const couponUsed = ref(false)
const remark = ref('')
const toast = ref('')
const travelMode = ref('滴滴出行')

const payableServicePrice = computed(() => Number(booking.service?.price ?? 218).toFixed(2))
const distanceKm = computed(() => Number(booking.technician?.distanceKm ?? 0).toFixed(2))
const travelFee = computed(() => {
  const distance = Number(booking.technician?.distanceKm ?? 0)
  if (distance <= 1) return '0.00'
  return (Math.ceil(distance - 1) * 4).toFixed(2)
})
const totalAmount = computed(() => (Number(booking.service?.price ?? 218) + Number(travelFee.value) - (couponUsed.value ? 10 : 0)).toFixed(2))
const maskedPhone = computed(() => booking.address.phone.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2'))

onMounted(async () => {
  booking.latestOrder = await api.latestClientOrder()
})

function chooseTime() {
  const options = ['今天 23:00', '明天 10:00', '明天 14:00', '明天 20:00']
  const currentIndex = options.indexOf(booking.serviceTime)
  booking.serviceTime = options[(currentIndex + 1) % options.length]
  flash(`已选择 ${booking.serviceTime}`)
}

function toggleTravel() {
  travelMode.value = travelMode.value === '滴滴出行' ? '技师自驾' : '滴滴出行'
  flash(`出行方式：${travelMode.value}`)
}

function pickCoupon() {
  couponUsed.value = !couponUsed.value
  flash(couponUsed.value ? '已使用10元优惠券' : '已取消优惠券')
}

function editRemark() {
  remark.value = remark.value ? '' : '请提前电话联系'
  flash(remark.value || '已清空备注')
}

async function submit() {
  if (!agreed.value) return
  if (!booking.customerLocation) {
    booking.customerLocation = await locate('用户定位')
  }
  const order = await api.createOrder({
    serviceId: booking.service?.id ?? 'tuina',
    technicianId: booking.technician?.id ?? 'sha',
    address: booking.address,
    customerLocation: booking.customerLocation,
    serviceTime: booking.serviceTime === '点击选择服务时间' ? '今天 23:00' : booking.serviceTime
  })
  booking.latestOrder = await api.payOrder(order.id)
  flash('模拟支付成功')
  window.setTimeout(() => router.push('/client/orders'), 500)
}

async function refreshCustomerLocation() {
  booking.customerLocation = await locate('用户定位')
  if (booking.latestOrder?.id) {
    booking.latestOrder = await api.updateCustomerLocation(booking.latestOrder.id, booking.customerLocation)
  }
  flash('定位已更新')
}

function flash(message) {
  toast.value = message
  window.setTimeout(() => {
    toast.value = ''
  }, 1800)
}
</script>
