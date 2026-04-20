<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :xs="12" :sm="6" v-for="card in summaryCards" :key="card.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-icon" :style="{ background: card.color }">
            <el-icon :size="28" color="#fff"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ card.value }}</p>
            <p class="stat-label">{{ card.title }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="14">
        <el-card>
          <template #header><h4>近7天预约趋势</h4></template>
          <div ref="trendChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header><h4>科室预约分布</h4></template>
          <div ref="pieChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { fetchDashboardStats } from '@/api/admin'
import * as echarts from 'echarts'

const stats = ref({})
const trendChartRef = ref(null)
const pieChartRef = ref(null)

const summaryCards = computed(() => [
  { title: '总预约数', value: stats.value.totalAppointments || 0, icon: 'List', color: '#409EFF' },
  { title: '今日预约', value: stats.value.todayAppointments || 0, icon: 'Calendar', color: '#67C23A' },
  { title: '患者总数', value: stats.value.totalPatients || 0, icon: 'User', color: '#E6A23C' },
  { title: '医生总数', value: stats.value.totalDoctors || 0, icon: 'UserFilled', color: '#F56C6C' }
])

onMounted(async () => {
  try {
    const res = await fetchDashboardStats()
    stats.value = res.data

    await nextTick()

    if (trendChartRef.value && res.data.weeklyTrend) {
      const chart = echarts.init(trendChartRef.value)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: res.data.weeklyTrend.map(i => i.date) },
        yAxis: { type: 'value' },
        series: [{ data: res.data.weeklyTrend.map(i => i.count), type: 'line', smooth: true, areaStyle: {} }]
      })
    }

    if (pieChartRef.value && res.data.deptDistribution) {
      const chart = echarts.init(pieChartRef.value)
      chart.setOption({
        tooltip: { trigger: 'item' },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          data: res.data.deptDistribution,
          emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
        }]
      })
    }
  } catch (e) { /* handled */ }
})
</script>

<style scoped>
.stat-card { display: flex; align-items: center; }
.stat-card .el-card__body { display: flex; align-items: center; gap: 16px; width: 100%; }
.stat-icon {
  width: 56px; height: 56px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-value { font-size: 24px; font-weight: 700; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 2px; }
</style>
