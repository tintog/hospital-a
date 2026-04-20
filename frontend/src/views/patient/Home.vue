<template>
  <div class="home-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="welcome-banner">
          <h1>欢迎使用医院预约挂号系统</h1>
          <p>在线预约，便捷就诊</p>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="8" v-for="item in quickActions" :key="item.title">
        <el-card class="action-card" shadow="hover" @click="router.push(item.path)">
          <el-icon :size="36" :color="item.color"><component :is="item.icon" /></el-icon>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 30px;">
      <el-col :span="24">
        <el-card>
          <template #header><h3>热门科室</h3></template>
          <el-row :gutter="16">
            <el-col :xs="12" :sm="6" v-for="dept in departments" :key="dept.id">
              <div class="dept-item" @click="goToDept(dept.id)">
                <el-icon :size="28" color="#409EFF"><OfficeBuilding /></el-icon>
                <span>{{ dept.name }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchDepartments } from '@/api/department'

const router = useRouter()
const departments = ref([])

const quickActions = [
  { title: '预约挂号', desc: '选择科室和医生进行预约', icon: 'Calendar', color: '#409EFF', path: '/departments' },
  { title: '我的预约', desc: '查看预约记录和状态', icon: 'List', color: '#67C23A', path: '/appointments' },
  { title: '就诊人管理', desc: '添加/管理就诊人信息', icon: 'User', color: '#E6A23C', path: '/members' }
]

onMounted(async () => {
  try {
    const res = await fetchDepartments()
    departments.value = res.data
  } catch (e) { /* handled */ }
})

function goToDept(deptId) {
  router.push({ path: '/departments', query: { deptId } })
}
</script>

<style scoped>
.welcome-banner {
  background: linear-gradient(135deg, #409EFF, #53a8ff);
  color: #fff;
  padding: 40px;
  border-radius: 12px;
  text-align: center;
}
.welcome-banner h1 { font-size: 28px; margin-bottom: 8px; }
.welcome-banner p { font-size: 16px; opacity: 0.9; }
.action-card {
  text-align: center;
  padding: 20px 0;
  cursor: pointer;
  transition: transform 0.2s;
  margin-bottom: 16px;
}
.action-card:hover { transform: translateY(-4px); }
.action-card h3 { margin: 12px 0 4px; color: #303133; }
.action-card p { color: #909399; font-size: 13px; }
.dept-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 8px;
}
.dept-item:hover { background: #f0f5ff; }
.dept-item span { font-size: 14px; color: #303133; }
</style>
