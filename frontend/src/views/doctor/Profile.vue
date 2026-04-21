<template>
  <div>
    <el-card>
      <template #header><h3>个人信息</h3></template>

      <el-descriptions :column="1" border style="margin-bottom: 16px;">
        <el-descriptions-item label="姓名">{{ profile.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ profile.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ profile.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职称">{{ profile.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="擅长">{{ profile.specialty || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>修改账户名（登录账号）</el-divider>
      <el-form ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" label-width="130px" style="max-width: 560px;">
        <el-form-item label="新账户名" prop="newPhone">
          <el-input v-model="phoneForm.newPhone" placeholder="请输入新账户名" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="phoneSaving" @click="submitPhone">保存账户名</el-button>
        </el-form-item>
      </el-form>

      <el-divider>修改密码</el-divider>
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="130px" style="max-width: 560px;">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="passwordSaving" @click="submitPassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { fetchDoctorProfile, updateDoctorPhone, updateDoctorPassword } from '@/api/doctorProfile'

const router = useRouter()
const userStore = useUserStore()

const profile = reactive({
  realName: '',
  username: '',
  deptName: '',
  title: '',
  specialty: ''
})

const phoneFormRef = ref(null)
const phoneSaving = ref(false)
const phoneForm = reactive({ newPhone: '' })
const phoneRules = {
  newPhone: [
    { required: true, message: '请输入新账户名', trigger: 'blur' }
  ]
}

const passwordFormRef = ref(null)
const passwordSaving = ref(false)
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度需在6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value !== passwordForm.newPassword) callback(new Error('两次输入的新密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

async function loadProfile() {
  const res = await fetchDoctorProfile()
  const data = res.data || {}
  profile.realName = data.realName || ''
  profile.username = data.username || ''
  profile.deptName = data.deptName || ''
  profile.title = data.title || ''
  profile.specialty = data.specialty || ''

  phoneForm.newPhone = profile.username
  userStore.phone = profile.username
}

async function submitPhone() {
  await phoneFormRef.value.validate()
  phoneSaving.value = true
  try {
    await updateDoctorPhone({ newPhone: phoneForm.newPhone })
    ElMessage.success('手机号修改成功')
    profile.username = phoneForm.newPhone
    userStore.phone = phoneForm.newPhone
    userStore.username = phoneForm.newPhone
    localStorage.setItem('phone', phoneForm.newPhone)
    localStorage.setItem('username', phoneForm.newPhone)
  } finally {
    phoneSaving.value = false
  }
}

async function submitPassword() {
  await passwordFormRef.value.validate()
  passwordSaving.value = true
  try {
    await updateDoctorPassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    await ElMessageBox.alert('密码已修改，请重新登录', '提示', { confirmButtonText: '确定' })
    userStore.logout()
    router.push('/admin-login')
  } finally {
    passwordSaving.value = false
  }
}

onMounted(() => loadProfile())
</script>
