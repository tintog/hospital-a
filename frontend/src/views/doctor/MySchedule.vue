<template>
  <div>
    <el-card>
      <template #header><h3>我的排班</h3></template>
      <el-table :data="schedules" border stripe>
        <el-table-column prop="workDate" label="日期" width="120" />
        <el-table-column label="班次" width="80">
          <template #default="{ row }">{{ row.shiftType === 1 ? '上午' : '下午' }}</template>
        </el-table-column>
        <el-table-column label="时段" width="150">
          <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
        </el-table-column>
        <el-table-column prop="totalSlots" label="总号源" width="80" />
        <el-table-column prop="bookedSlots" label="已约" width="80" />
        <el-table-column label="剩余" width="80">
          <template #default="{ row }">
            <span :style="{ color: (row.totalSlots - row.bookedSlots) > 0 ? '#67C23A' : '#F56C6C' }">
              {{ row.totalSlots - row.bookedSlots }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '停诊' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="schedules.length === 0" description="暂无排班" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const schedules = ref([])

onMounted(async () => {
  try {
    const res = await request.get('/doctor/my-schedule')
    schedules.value = res.data || []
  } catch (e) { /* handled */ }
})
</script>
