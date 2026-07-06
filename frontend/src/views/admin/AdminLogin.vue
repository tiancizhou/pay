<template>
  <main class="admin-login">
    <section class="admin-login__panel">
      <div class="admin-login__brand">
        <div class="admin-login__logo">温</div>
        <div>
          <strong>温暖到家</strong>
          <span>Merchant OS</span>
        </div>
      </div>

      <section class="admin-login__headline">
        <span>管理端登录</span>
        <h1>商户后台账号入口</h1>
        <p>仅管理员角色可进入后台，客户和技师账号请使用移动端入口。</p>
      </section>

      <el-form label-position="top" class="admin-login__form" @submit.prevent="login">
        <el-form-item label="账号">
          <el-input v-model="form.username" clearable placeholder="请输入管理员账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" show-password placeholder="请输入密码" @keyup.enter="login" />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="login">登录管理端</el-button>
      </el-form>

      <div class="admin-login__hint">
        <span>默认管理员</span>
        <button type="button" @click="fillDemo">admin / admin123</button>
      </div>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../../services/api'
import { getAdminUser, setAdminUser } from '../../services/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

onMounted(() => {
  const admin = getAdminUser()
  if (admin?.role === 'ADMIN') {
    router.replace('/admin')
  }
})

function fillDemo() {
  form.username = 'admin'
  form.password = 'admin123'
}

async function login() {
  if (!form.username.trim() || !form.password) {
    ElMessage.warning('请填写账号和密码')
    return
  }
  loading.value = true
  try {
    const user = await api.login({
      username: form.username.trim(),
      password: form.password,
      scope: 'ADMIN'
    })
    if (user.role !== 'ADMIN') {
      ElMessage.warning('当前账号不是管理员')
      return
    }
    setAdminUser(user)
    await router.replace('/admin')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
