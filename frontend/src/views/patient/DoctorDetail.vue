<template>
  <div class="doctor-detail-page" v-if="doctor">
    <el-card>
      <div class="doctor-profile">
        <el-avatar :size="80" icon="UserFilled" />
        <div class="profile-info">
          <h2>{{ doctor.name }}</h2>
          <el-tag type="warning">{{ doctor.title }}</el-tag>
          <p><strong>擅长:</strong> {{ doctor.specialty }}</p>
          <p class="intro">{{ doctor.introduction }}</p>
        </div>
      </div>
      <div style="text-align: center; margin-top: 24px;">
        <el-button type="primary" size="large" @click="router.push(`/booking/${doctor.id}`)">
          <el-icon><Calendar /></el-icon>立即预约
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchDoctorDetail } from '@/api/doctor'

const props = defineProps({ id: [String, Number] })
const router = useRouter()
const doctor = ref(null)

onMounted(async () => {
  const res = await fetchDoctorDetail(props.id)
  doctor.value = res.data
})
</script>

<style scoped>
.doctor-detail-page { max-width: 700px; margin: 0 auto; }
.doctor-profile { display: flex; gap: 20px; align-items: flex-start; }
.profile-info h2 { margin-bottom: 8px; }
.profile-info p { margin-top: 8px; color: #606266; line-height: 1.6; }
.intro { font-size: 14px; }
</style>
