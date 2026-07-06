<template>
  <main class="login-entry">
    <section class="login-entry__panel">
      <div class="login-entry__brand">
        <div class="login-entry__logo">温</div>
        <div>
          <strong>温暖到家</strong>
          <span>客户端 / 技师端</span>
        </div>
      </div>

      <section class="login-entry__headline">
        <span>账号登录</span>
        <h1>登录进入移动工作台</h1>
        <p>客户和技师使用同一移动入口，系统会根据后台配置的角色自动进入对应页面。</p>
      </section>

      <van-form class="login-entry__form" @submit="login">
        <van-field
          v-model="form.username"
          name="username"
          label="账号"
          placeholder="请输入账号"
          autocomplete="username"
          :rules="[{ required: true, message: '请输入账号' }]"
        />
        <van-field
          v-model="form.password"
          name="password"
          label="密码"
          type="password"
          placeholder="请输入密码"
          autocomplete="current-password"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
        <button class="login-entry__submit" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </van-form>

      <section class="login-entry__demo">
        <span>初始账号</span>
        <button type="button" @click="fillDemo('tech', 'tech123')">技师 tech</button>
        <button type="button" @click="fillDemo('client', 'client123')">客户 client</button>
      </section>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { api } from '../services/api'
import { getPortalUser, roleHome, setPortalUser } from '../services/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

onMounted(() => {
  const user = getPortalUser()
  if (user?.role) {
    router.replace(roleHome(user))
  }
})

function fillDemo(username, password) {
  form.username = username
  form.password = password
}

async function login() {
  loading.value = true
  try {
    const user = await api.login({
      username: form.username.trim(),
      password: form.password,
      scope: 'PORTAL'
    })
    if (user.role === 'ADMIN') {
      showToast('管理员请从管理端入口登录')
      return
    }
    setPortalUser(user)
    await router.replace(roleHome(user))
  } catch (error) {
    showToast(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
