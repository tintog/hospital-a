<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <h3>科室管理</h3>
          <el-button type="primary" @click="openCreate">新增科室</el-button>
        </div>
      </template>

      <el-table :data="records" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="科室名称" width="180" />
        <el-table-column prop="description" label="科室简介" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑科室' : '新增科室'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入科室名称" />
        </el-form-item>
        <el-form-item label="科室简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入科室简介" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchDepartments, createDepartment, updateDepartment } from '@/api/department'

const records = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const editingId = ref(null)

const form = reactive({
  name: '',
  description: '',
  sortOrder: 0
})

const rules = {
  name: [{ required: true, message: '请输入科室名称', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'change' }]
}

async function loadData() {
  const res = await fetchDepartments()
  records.value = res.data || []
}

function resetForm() {
  form.name = ''
  form.description = ''
  form.sortOrder = 0
}

function openCreate() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  editingId.value = row.id
  form.name = row.name || ''
  form.description = row.description || ''
  form.sortOrder = row.sortOrder ?? 0
  dialogVisible.value = true
}

async function submitSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (isEdit.value) {
      await updateDepartment(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createDepartment(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

onMounted(() => loadData())
</script>
