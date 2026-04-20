<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <el-icon :size="40" color="#E6A23C"><Setting /></el-icon>
        <h2>管理后台</h2>
        <p>管理员 / 医生 登录</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="warning" :loading="loading" @click="handleLogin" style="width:100%">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <router-link to="/login">患者登录入口</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { sysLogin } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await sysLogin(form)
    userStore.setLoginInfo(res.data)
    ElMessage.success('登录成功')
    const role = res.data.role
    if (role === 'doctor') {
      router.push('/doctor/schedule')
    } else {
      router.push('/admin/dashboard')
    }
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}
.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.login-header { text-align: center; margin-bottom: 30px; }
.login-header h2 { margin: 12px 0 4px; color: #303133; }
.login-header p { color: #909399; font-size: 14px; }
.login-footer { text-align: center; margin-top: 16px; }
.login-footer a { color: #409EFF; text-decoration: none; font-size: 13px; }
</style>
