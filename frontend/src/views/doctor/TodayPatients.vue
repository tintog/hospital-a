<template>
  <div>
    <el-card>
      <template #header><h3>今日患者</h3></template>
      <el-table :data="records" border stripe>
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="slotNo" label="号序" width="100" />
        <el-table-column label="就诊时间" width="140">
          <template #default="{ row }">{{ row.visitTime ? row.visitTime.substring(11, 16) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="memberName" label="就诊人" width="80">
          <template #default="{ row }">{{ row.memberName || '本人' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]">{{ row.statusDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="费用" width="70">
          <template #default="{ row }">¥{{ row.fee }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="records.length === 0" description="今日暂无患者" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const records = ref([])
const statusType = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info', 4: '' }

onMounted(async () => {
  try {
    const res = await request.get('/doctor/today-patients')
    records.value = res.data.records || []
  } catch (e) { /* handled */ }
})
</script>
