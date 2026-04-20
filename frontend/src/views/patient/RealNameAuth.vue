<template>
  <div class="page-container">
    <el-card>
      <template #header><h3>实名认证</h3></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width:500px">
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">提交认证</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { realNameAuth } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ realName: '', idCard: '' })
const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ]
}

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await realNameAuth(form)
    userStore.realName = form.realName
    localStorage.setItem('realName', form.realName)
    ElMessage.success('实名认证成功')
    router.push('/home')
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page-container { max-width: 700px; margin: 0 auto; }
</style>
