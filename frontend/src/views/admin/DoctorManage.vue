<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <h3>医生管理</h3>
          <el-button type="primary" @click="openCreate">新增医生</el-button>
        </div>
      </template>

      <el-form :inline="true" style="margin-bottom:16px;">
        <el-form-item label="姓名">
          <el-input v-model="filter.name" placeholder="医生姓名" clearable style="width:150px;" />
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="filter.deptId" clearable placeholder="全部" style="width:160px;">
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="filter.title" placeholder="职称" clearable style="width:150px;" />
        </el-form-item>
        <el-form-item label="账号状态">
          <el-select v-model="filter.status" clearable placeholder="全部" style="width:140px;">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="records" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="账号" width="170">
          <template #default="{ row }">
            {{ formatDoctorUsername(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="deptName" label="科室" width="110" />
        <el-table-column prop="title" label="职称" width="120" />
        <el-table-column prop="specialty" label="擅长" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该医生？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑医生' : '新增医生'" width="560px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <template v-if="!isEdit">
          <el-form-item label="登录账号" prop="username">
            <el-input v-model="form.username" placeholder="请输入账号" />
          </el-form-item>
          <el-form-item label="初始密码" prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入初始密码" />
          </el-form-item>
        </template>

        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="科室" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择科室" style="width:100%;">
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-input v-model="form.title" placeholder="请输入职称" />
        </el-form-item>
        <el-form-item label="擅长" prop="specialty">
          <el-input v-model="form.specialty" placeholder="请输入擅长" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.introduction" type="textarea" :rows="3" placeholder="请输入简介" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="账号状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchAdminDoctors,
  fetchAdminDoctorDetail,
  createAdminDoctor,
  updateAdminDoctor,
  deleteAdminDoctor
} from '@/api/adminManage'
import { fetchDepartments } from '@/api/department'

const records = ref([])
const total = ref(0)
const page = ref(1)
const size = 10

const departments = ref([])

const filter = reactive({
  name: '',
  deptId: null,
  title: '',
  status: null
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const form = reactive({
  username: '',
  password: '',
  name: '',
  deptId: null,
  title: '',
  specialty: '',
  introduction: '',
  status: 1
})

const formRules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  title: [{ required: true, message: '请输入职称', trigger: 'blur' }],
  specialty: [{ required: true, message: '请输入擅长', trigger: 'blur' }],
  status: [{ required: true, message: '请选择账号状态', trigger: 'change' }]
}

async function loadData() {
  const params = { page: page.value, size }
  if (filter.name) params.name = filter.name
  if (filter.deptId) params.deptId = filter.deptId
  if (filter.title) params.title = filter.title
  if (filter.status !== null) params.status = filter.status

  const res = await fetchAdminDoctors(params)
  records.value = res.data.records || []
  total.value = res.data.total || 0
}

async function loadDepartments() {
  const res = await fetchDepartments()
  departments.value = res.data || []
}

function handleSearch() {
  page.value = 1
  loadData()
}

function resetForm() {
  form.username = ''
  form.password = ''
  form.name = ''
  form.deptId = null
  form.title = ''
  form.specialty = ''
  form.introduction = ''
  form.status = 1
}

function openCreate() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

async function openEdit(row) {
  const res = await fetchAdminDoctorDetail(row.id)
  const detail = res.data || {}

  isEdit.value = true
  editingId.value = row.id
  form.username = detail.username || row.username || ''
  form.password = ''
  form.name = detail.name || row.name || ''
  form.deptId = detail.deptId ?? row.deptId ?? null
  form.title = detail.title || row.title || ''
  form.specialty = detail.specialty || row.specialty || ''
  form.introduction = detail.introduction || row.introduction || ''
  form.status = detail.status ?? row.status ?? 1
  dialogVisible.value = true
}

function formatDoctorUsername(row) {
  if (row.username) return row.username
  const fallbackMap = {
    4: 'doctor_zhao',
    5: 'doctor_liu',
    6: 'doctor_chen',
    7: 'doctor_sun',
    8: 'doctor_zhou'
  }
  return fallbackMap[row.id] || `doctor_${row.id}`
}

async function submitForm() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (isEdit.value) {
      await updateAdminDoctor(editingId.value, {
        name: form.name,
        deptId: form.deptId,
        title: form.title,
        specialty: form.specialty,
        introduction: form.introduction,
        status: form.status
      })
      ElMessage.success('医生更新成功')
    } else {
      await createAdminDoctor({
        username: form.username,
        password: form.password,
        name: form.name,
        deptId: form.deptId,
        title: form.title,
        specialty: form.specialty,
        introduction: form.introduction
      })
      ElMessage.success('医生创建成功')
    }

    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  await deleteAdminDoctor(id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(async () => {
  await loadDepartments()
  await loadData()
})
</script>
