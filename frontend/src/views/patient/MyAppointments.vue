<template>
  <div class="appointments-page">
    <el-card>
      <template #header>
        <div style="display:flex;align-items:center;justify-content:space-between;gap:16px;">
          <h3>我的预约</h3>
          <div style="display:flex;align-items:center;gap:12px;">
            <el-segmented v-model="tab" :options="tabOptions" @change="handleTabChange" />
            <el-select v-model="statusFilter" placeholder="筛选状态" clearable @change="loadData" style="width:140px;">
              <el-option label="全部" :value="null" />
              <el-option label="待支付" :value="0" />
              <el-option label="已确认" :value="1" />
              <el-option label="已取消" :value="2" />
              <el-option label="已完成" :value="3" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="records" border stripe>
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="deptName" label="科室" width="100" />
        <el-table-column prop="doctorName" label="医生" width="80" />
        <el-table-column label="就诊时间" width="130">
          <template #default="{ row }">
            {{ row.visitTime ? row.visitTime.substring(0, 16) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="费用" width="80">
          <template #default="{ row }">¥{{ row.fee }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
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
        <el-table-column prop="memberName" label="就诊人" width="80">
          <template #default="{ row }">{{ row.memberName || '本人' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="tab === 'current' && (row.status === 0 || row.status === 1)"
              type="danger"
              size="small"
              link
              @click="handleCancel(row)"
            >取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        style="margin-top:16px;justify-content:center;"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        v-model:current-page="currentPage"
        @current-change="loadData"
      />
      <el-empty v-if="records.length === 0" :description="tab === 'current' ? '暂无当前预约' : '暂无历史预约'" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchMyAppointments, cancelAppointment } from '@/api/appointment'

const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const statusFilter = ref(null)
const tab = ref('current')
const tabOptions = [
  { label: '当前预约', value: 'current' },
  { label: '历史预约', value: 'history' }
]

const statusType = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info', 4: '' }

async function loadData() {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      history: tab.value === 'history'
    }
    if (statusFilter.value !== null) params.status = statusFilter.value
    const res = await fetchMyAppointments(params)
    records.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
}

function handleTabChange() {
  currentPage.value = 1
  loadData()
}

async function handleCancel(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消预约', {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      inputPlaceholder: '如：时间冲突'
    })
    await cancelAppointment(row.id, value || '用户主动取消')
    ElMessage.success('取消成功')
    await loadData()
  } catch (e) {
    // user cancelled dialog
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.appointments-page { max-width: 1100px; margin: 0 auto; }
</style>
