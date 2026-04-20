<template>
  <div>
    <el-card>
      <template #header><h3>预约管理</h3></template>

      <el-form :inline="true" style="margin-bottom:16px;">
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width:120px;" @change="loadData">
            <el-option label="待支付" :value="0" />
            <el-option label="已确认" :value="1" />
            <el-option label="已取消" :value="2" />
            <el-option label="已完成" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室">
          <el-select
            v-model="filter.deptId"
            placeholder="全部科室"
            clearable
            style="width: 180px;"
            @change="handleDeptChange"
          >
            <el-option
              v-for="dept in departmentOptions"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="医生">
          <el-select
            v-model="filter.doctorId"
            placeholder="全部医生"
            filterable
            clearable
            style="width: 220px;"
            @change="loadData"
          >
            <el-option
              v-for="doctor in queryDoctorOptions"
              :key="doctor.id"
              :label="`${doctor.name}（${doctor.title || '未设置职称'}）`"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="records" border stripe>
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="deptName" label="科室" width="90" />
        <el-table-column prop="doctorName" label="医生" width="80" />
        <el-table-column label="就诊时间" width="140">
          <template #default="{ row }">{{ row.visitTime ? row.visitTime.substring(0, 16) : '-' }}</template>
        </el-table-column>
        <el-table-column label="费用" width="70">
          <template #default="{ row }">¥{{ row.fee }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]">{{ row.statusDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="memberName" label="就诊人" width="80">
          <template #default="{ row }">{{ row.memberName || '本人' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">{{ row.createdAt ? row.createdAt.substring(0, 19) : '' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确认取消此预约？" @confirm="handleCancel(row.id)" v-if="row.status <= 1">
              <template #reference>
                <el-button type="danger" size="small" link>取消</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        style="margin-top:16px;justify-content:center;"
        layout="total, prev, pager, next"
        :total="total" :page-size="10"
        v-model:current-page="page"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminAppointments, adminCancelAppointment } from '@/api/admin'
import { fetchDoctors } from '@/api/doctor'
import { fetchDepartments } from '@/api/department'

const records = ref([])
const total = ref(0)
const page = ref(1)
const doctorOptions = ref([])
const departmentOptions = ref([])
const filter = reactive({ status: null, deptId: null, doctorId: null })
const statusType = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info', 4: '' }

const queryDoctorOptions = computed(() => {
  if (!filter.deptId) return doctorOptions.value
  return doctorOptions.value.filter(d => d.deptId === filter.deptId)
})

async function loadData() {
  const params = { page: page.value, size: 10 }
  if (filter.status !== null && filter.status !== '') params.status = filter.status
  if (filter.doctorId) params.doctorId = filter.doctorId
  const res = await fetchAdminAppointments(params)
  records.value = res.data.records || []
  total.value = res.data.total || 0
}

async function loadDoctors() {
  const res = await fetchDoctors()
  doctorOptions.value = res.data || []
}

async function loadDepartments() {
  const res = await fetchDepartments()
  departmentOptions.value = res.data || []
}

function handleDeptChange() {
  filter.doctorId = null
  loadData()
}

async function handleCancel(id) {
  await adminCancelAppointment(id, '管理员取消')
  ElMessage.success('已取消')
  await loadData()
}

onMounted(async () => {
  await Promise.all([loadDoctors(), loadDepartments()])
  await loadData()
})
</script>
