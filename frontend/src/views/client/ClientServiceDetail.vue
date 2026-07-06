<template>
  <MobileShell :tabbar="false">
    <section v-if="service" class="service-detail">
      <img class="service-detail__hero" :src="service.imageUrl" :alt="service.name">
      <section class="service-detail__summary">
        <h1>{{ service.name }}</h1>
        <div class="speed-strip"><strong>极速上门</strong><span>随叫随到 最快30分钟上门</span></div>
        <div class="service-option">
          <strong>{{ service.name }}</strong>
          <span>¥ {{ Number(service.price).toFixed(0) }}/{{ service.durationMinutes }}分钟</span>
        </div>
        <div class="service-badges">
          <span>▣ 不满意重复服务</span>
          <span>▣ 无额外收费</span>
        </div>
      </section>

      <nav class="detail-tabs">
        <button :class="{ active: activeTab === 'intro' }" @click="activeTab = 'intro'">项目介绍</button>
        <button :class="{ active: activeTab === 'notice' }" @click="activeTab = 'notice'">下单说明</button>
      </nav>

      <section class="detail-content">
        <template v-if="activeTab === 'intro'">
          <DetailBlock title="项目亮点">
            <div class="highlight-card">
              <img :src="service.imageUrl" :alt="service.name">
              <ul>
                <li v-for="item in service.detail.highlights" :key="item">{{ item }}</li>
              </ul>
            </div>
          </DetailBlock>
          <DetailBlock title="服务流程">
            <ol class="process-list">
              <li v-for="item in service.detail.process" :key="item">{{ item }}</li>
            </ol>
          </DetailBlock>
          <DetailBlock title="服务物料">
            <div class="material-card">{{ service.detail.materials.join('、') }}</div>
          </DetailBlock>
        </template>
        <template v-else>
          <DetailBlock title="注意事项">
            <ol class="notice-card">
              <li v-for="item in service.detail.notices" :key="item">{{ item }}</li>
            </ol>
          </DetailBlock>
          <DetailBlock title="服务禁忌">
            <ol class="forbid-card">
              <li v-for="item in service.detail.contraindications" :key="item">{{ item }}</li>
            </ol>
          </DetailBlock>
          <DetailBlock title="选择本服务的理由">
            <div class="reason-card">
              <p v-for="item in service.detail.reasons" :key="item">{{ item }}</p>
            </div>
          </DetailBlock>
        </template>
      </section>

      <footer class="detail-order-bar">
        <button @click="orderNow">立即下单</button>
      </footer>
    </section>
  </MobileShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MobileShell from '../../components/MobileShell.vue'
import { api } from '../../services/api'
import { booking } from '../../state/booking'
import DetailBlock from './DetailBlock.vue'

const route = useRoute()
const router = useRouter()
const service = ref(null)
const activeTab = ref('intro')

onMounted(async () => {
  const services = await api.listServices()
  service.value = services.find((item) => item.id === route.params.serviceId) ?? services[0]
  booking.service = service.value
})

function orderNow() {
  router.push(`/client/services/${service.value.id}/technicians`)
}
</script>
