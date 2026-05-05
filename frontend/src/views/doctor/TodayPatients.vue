<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;align-items:center;justify-content:space-between;">
          <h3>我的患者</h3>
          <el-segmented v-model="tab" :options="tabOptions" @change="handleTabChange" />
        </div>
      </template>

      <el-table :data="records" border stripe>
        <el-table-column prop="slotNo" label="号序" width="100" />
        <el-table-column label="就诊时间" width="160">
          <template #default="{ row }">{{ row.visitTime ? row.visitTime.substring(5, 16) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="patientName" label="患者姓名" width="120">
          <template #default="{ row }">{{ row.patientName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="memberName" label="就诊人" width="100">
          <template #default="{ row }">{{ row.memberName || row.patientName || '本人' }}</template>
        </el-table-column>
        <el-table-column label="预约状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]">{{ row.statusDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="就诊状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.visitStatus === 1 ? 'success' : 'warning'">
              {{ row.visitStatus === 1 ? '已就诊' : '未就诊' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="费用" width="80">
          <template #default="{ row }">¥{{ row.fee }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="tab === 'current' && row.visitStatus !== 1 && row.status === 1"
              type="primary"
              size="small"
              link
              @click="markVisited(row)"
            >标记已就诊</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="records.length === 0" :description="tab === 'current' ? '当前暂无患者' : '暂无历史患者'" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchTodayPatients, updateVisitStatus } from '@/api/doctorWork'

const records = ref([])
const tab = ref('current')
const tabOptions = [
  { label: '当前患者', value: 'current' },
  { label: '历史患者', value: 'history' }
]
const statusType = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info', 4: '' }

async function loadData() {
  try {
    const res = await fetchTodayPatients({ page: 1, size: 200, history: tab.value === 'history' })
    const list = res.data.records || []
    records.value = list.sort((a, b) => {
      const ta = a.visitTime ? new Date(a.visitTime).getTime() : 0
      const tb = b.visitTime ? new Date(b.visitTime).getTime() : 0
      return ta - tb
    })
  } catch (e) { /* handled */ }
}

function handleTabChange() {
  loadData()
}

async function markVisited(row) {
  await updateVisitStatus(row.id, 1)
  ElMessage.success('已标记为已就诊')
  await loadData()
}

onMounted(() => loadData())
</script>
