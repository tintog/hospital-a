<template>
  <div>
    <el-card>
      <template #header><h3>统计报表</h3></template>
      <el-row :gutter="20">
        <el-col :span="12">
          <h4 style="margin-bottom:12px;">预约趋势(近7天)</h4>
          <div ref="lineRef" style="height:350px;"></div>
        </el-col>
        <el-col :span="12">
          <h4 style="margin-bottom:12px;">科室分布</h4>
          <div ref="pieRef" style="height:350px;"></div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { fetchDashboardStats } from '@/api/admin'

const lineRef = ref(null)
const pieRef = ref(null)

onMounted(async () => {
  const res = await fetchDashboardStats()
  const data = res.data
  await nextTick()

  if (lineRef.value && data.weeklyTrend) {
    echarts.init(lineRef.value).setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.weeklyTrend.map(i => i.date) },
      yAxis: { type: 'value' },
      series: [{ data: data.weeklyTrend.map(i => i.count), type: 'bar', itemStyle: { color: '#409EFF' } }]
    })
  }
  if (pieRef.value && data.deptDistribution) {
    echarts.init(pieRef.value).setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{ type: 'pie', radius: '65%', data: data.deptDistribution }]
    })
  }
})
</script>
