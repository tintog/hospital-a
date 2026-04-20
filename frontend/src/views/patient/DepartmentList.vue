<template>
  <div class="dept-page">
    <el-card>
      <template #header><h3>科室导航</h3></template>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="8" :md="6" v-for="dept in departments" :key="dept.id">
          <div
            class="dept-card"
            :class="{ active: selectedDept === dept.id }"
            @click="selectDept(dept.id)"
          >
            <el-icon :size="32" color="#409EFF"><OfficeBuilding /></el-icon>
            <h4>{{ dept.name }}</h4>
            <p>{{ dept.description }}</p>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card v-if="selectedDept" style="margin-top: 20px;">
      <template #header><h3>医生列表</h3></template>
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8" v-for="doc in doctors" :key="doc.id">
          <el-card class="doctor-card" shadow="hover">
            <div class="doctor-info">
              <el-avatar :size="60" icon="UserFilled" />
              <div class="doctor-detail">
                <h4>{{ doc.name }}</h4>
                <el-tag size="small" type="warning">{{ doc.title }}</el-tag>
                <p class="specialty">擅长: {{ doc.specialty }}</p>
              </div>
            </div>
            <div class="doctor-actions">
              <el-button size="small" @click="router.push(`/doctor/${doc.id}`)">详情</el-button>
              <el-button type="primary" size="small" @click="router.push(`/booking/${doc.id}`)">预约</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="doctors.length === 0" description="暂无医生" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { fetchDepartments } from '@/api/department'
import { fetchDoctors } from '@/api/doctor'

const router = useRouter()
const route = useRoute()
const departments = ref([])
const doctors = ref([])
const selectedDept = ref(null)

onMounted(async () => {
  const res = await fetchDepartments()
  departments.value = res.data
  if (route.query.deptId) {
    selectDept(Number(route.query.deptId))
  }
})

async function selectDept(deptId) {
  selectedDept.value = deptId
  const res = await fetchDoctors({ deptId })
  doctors.value = res.data
}
</script>

<style scoped>
.dept-card {
  text-align: center;
  padding: 20px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
  margin-bottom: 12px;
}
.dept-card:hover { background: #f0f5ff; }
.dept-card.active { border-color: #409EFF; background: #ecf5ff; }
.dept-card h4 { margin: 8px 0 4px; }
.dept-card p { font-size: 12px; color: #909399; }
.doctor-card { margin-bottom: 12px; }
.doctor-info { display: flex; align-items: flex-start; gap: 12px; }
.doctor-detail h4 { margin-bottom: 4px; }
.specialty { font-size: 13px; color: #606266; margin-top: 6px; }
.doctor-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 12px; }
</style>
