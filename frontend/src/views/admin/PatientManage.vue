<template>
  <div>
    <el-card>
      <template #header><h3>患者管理</h3></template>

      <el-form :inline="true" style="margin-bottom:16px;">
        <el-form-item label="手机号">
          <el-input v-model="filter.phone" placeholder="手机号" clearable style="width:160px;" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="filter.realName" placeholder="姓名" clearable style="width:140px;" />
        </el-form-item>
        <el-form-item label="认证状态">
          <el-select v-model="filter.authStatus" clearable placeholder="全部" style="width:130px;">
            <el-option label="未认证" :value="0" />
            <el-option label="已认证" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="黑名单">
          <el-select v-model="filter.blacklistStatus" clearable placeholder="全部" style="width:130px;">
            <el-option label="正常" :value="0" />
            <el-option label="黑名单中" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="records" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column label="认证" width="90">
          <template #default="{ row }">
            <el-tag :type="row.authStatus === 1 ? 'success' : 'info'">{{ row.authStatus === 1 ? '已认证' : '未认证' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="黑名单截止" width="180">
          <template #default="{ row }">{{ formatBlacklistEnd(row.blacklistEndTime) }}</template>
        </el-table-column>
        <el-table-column prop="recentAppointmentCount" label="预约数" width="90" />
        <el-table-column label="注册时间" width="180">
          <template #default="{ row }">{{ row.createdAt ? row.createdAt.substring(0, 19) : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="openDetail(row.id)">详情</el-button>
            <el-button size="small" link type="warning" @click="openBlacklist(row.id)">拉黑</el-button>
            <el-button size="small" link type="success" @click="handleUnblacklist(row.id)">解除</el-button>
            <el-popconfirm title="确认删除该患者？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        style="margin-top:16px;justify-content:center;"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="size"
        v-model:current-page="page"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="患者详情" width="520px">
      <el-descriptions :column="1" border v-if="detail">
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.phone }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detail.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="认证状态">{{ detail.authStatus === 1 ? '已认证' : '未认证' }}</el-descriptions-item>
        <el-descriptions-item label="黑名单截止">{{ detail.blacklistEndTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ detail.createdAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预约数">{{ detail.recentAppointmentCount || 0 }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="blacklistVisible" title="拉黑患者" width="420px">
      <el-form label-width="90px">
        <el-form-item label="拉黑天数">
          <el-input-number v-model="blacklistDays" :min="1" :max="365" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="blacklistVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBlacklist">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchAdminPatients,
  fetchAdminPatientDetail,
  blacklistPatient,
  unblacklistPatient,
  deletePatient
} from '@/api/adminManage'

const records = ref([])
const total = ref(0)
const page = ref(1)
const size = 10

const filter = reactive({
  phone: '',
  realName: '',
  authStatus: null,
  blacklistStatus: null
})

const detailVisible = ref(false)
const detail = ref(null)

const blacklistVisible = ref(false)
const blacklistPatientId = ref(null)
const blacklistDays = ref(30)

async function loadData() {
  const params = { page: page.value, size }
  if (filter.phone) params.phone = filter.phone
  if (filter.realName) params.realName = filter.realName
  if (filter.authStatus !== null) params.authStatus = filter.authStatus
  if (filter.blacklistStatus !== null) params.blacklistStatus = filter.blacklistStatus

  const res = await fetchAdminPatients(params)
  records.value = res.data.records || []
  total.value = res.data.total || 0
}

function handleSearch() {
  page.value = 1
  loadData()
}

async function openDetail(id) {
  const res = await fetchAdminPatientDetail(id)
  detail.value = res.data
  detailVisible.value = true
}

function openBlacklist(id) {
  blacklistPatientId.value = id
  blacklistDays.value = 30
  blacklistVisible.value = true
}

async function submitBlacklist() {
  await blacklistPatient(blacklistPatientId.value, blacklistDays.value)
  ElMessage.success('拉黑成功')
  blacklistVisible.value = false
  await loadData()
}

async function handleUnblacklist(id) {
  await unblacklistPatient(id)
  ElMessage.success('已解除黑名单')
  await loadData()
}

function formatBlacklistEnd(value) {
  if (!value) return '-'
  const end = new Date(value)
  if (Number.isNaN(end.getTime())) return value
  return end.getTime() > Date.now() ? value : '-'
}

async function handleDelete(id) {
  await deletePatient(id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(() => loadData())
</script>
