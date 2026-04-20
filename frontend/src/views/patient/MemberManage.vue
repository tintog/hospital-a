<template>
  <div class="member-page">
    <el-card>
      <template #header>
        <div style="display:flex;align-items:center;justify-content:space-between;">
          <h3>就诊人管理</h3>
          <el-button type="primary" @click="showDialog = true">
            <el-icon><Plus /></el-icon>添加就诊人
          </el-button>
        </div>
      </template>

      <el-table :data="members" border stripe>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="idCard" label="身份证号" width="200" />
        <el-table-column prop="relation" label="关系" width="100" />
        <el-table-column label="认证状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.authStatus === 1 ? 'success' : 'warning'">
              {{ row.authStatus === 1 ? '已认证' : '未认证' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" size="small" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="members.length === 0" description="暂未添加就诊人" />
    </el-card>

    <el-dialog v-model="showDialog" title="添加就诊人" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="18位身份证号" maxlength="18" />
        </el-form-item>
        <el-form-item label="与本人关系" prop="relation">
          <el-select v-model="form.relation" placeholder="请选择">
            <el-option label="父子/父女" value="父子" />
            <el-option label="母子/母女" value="母子" />
            <el-option label="夫妻" value="夫妻" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAdd">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchMembers, addMember, deleteMember } from '@/api/patient'

const members = ref([])
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({ name: '', idCard: '', relation: '' })
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '格式不正确', trigger: 'blur' }
  ],
  relation: [{ required: true, message: '请选择关系', trigger: 'change' }]
}

async function loadMembers() {
  const res = await fetchMembers()
  members.value = res.data || []
}

async function handleAdd() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await addMember(form)
    ElMessage.success('添加成功')
    showDialog.value = false
    form.name = ''
    form.idCard = ''
    form.relation = ''
    await loadMembers()
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

async function handleDelete(id) {
  await deleteMember(id)
  ElMessage.success('删除成功')
  await loadMembers()
}

onMounted(() => loadMembers())
</script>

<style scoped>
.member-page { max-width: 800px; margin: 0 auto; }
</style>
