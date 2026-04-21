<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <h3>排班管理</h3>
          <el-button type="primary" @click="showCreate = true"><el-icon><Plus /></el-icon>新建排班</el-button>
        </div>
      </template>

      <el-form :inline="true" style="margin-bottom:16px;">
        <el-form-item label="医生ID">
          <el-input v-model="filter.doctorId" placeholder="医生ID" style="width:120px;" clearable />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="filter.startDate" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadSchedules">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="schedules" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="doctorId" label="医生ID" width="80" />
        <el-table-column prop="workDate" label="日期" width="120" />
        <el-table-column label="班次" width="80">
          <template #default="{ row }">{{ row.shiftType === 1 ? '上午' : '下午' }}</template>
        </el-table-column>
        <el-table-column label="时段" width="140">
          <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
        </el-table-column>
        <el-table-column prop="totalSlots" label="总号源" width="80" />
        <el-table-column prop="bookedSlots" label="已约" width="60" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '停诊' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-popconfirm title="确认停诊？" @confirm="handleCancel(row.id)" v-if="row.status === 1">
              <template #reference>
                <el-button type="danger" size="small" link>停诊</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreate" title="新建排班" width="500px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="医生ID" prop="doctorId">
          <el-input-number v-model="createForm.doctorId" :min="1" />
        </el-form-item>
        <el-form-item label="出诊日期" prop="workDate">
          <el-date-picker v-model="createForm.workDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="班次" prop="shiftType">
          <el-radio-group v-model="createForm.shiftType">
            <el-radio :value="1">上午</el-radio>
            <el-radio :value="2">下午</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="号源数" prop="totalSlots">
          <el-input-number v-model="createForm.totalSlots" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="时长(分)" prop="slotDuration">
          <el-input-number v-model="createForm.slotDuration" :min="5" :max="60" :step="5" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker v-model="createForm.startTime" format="HH:mm" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker v-model="createForm.endTime" format="HH:mm" value-format="HH:mm:ss" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchSchedules, createSchedule, cancelSchedule } from '@/api/admin'

const schedules = ref([])
const showCreate = ref(false)
const creating = ref(false)
const createFormRef = ref(null)
const filter = reactive({ doctorId: '', startDate: '' })

const createForm = reactive({
  doctorId: 1, workDate: '', shiftType: 1, totalSlots: 10,
  slotDuration: 15, startTime: '08:00:00', endTime: '10:30:00'
})
const createRules = {
  doctorId: [{ required: true, message: '必填' }],
  workDate: [{ required: true, message: '必填' }],
  shiftType: [{ required: true, message: '必填' }],
  totalSlots: [{ required: true, message: '必填' }],
  startTime: [{ required: true, message: '必填' }],
  endTime: [{ required: true, message: '必填' }]
}

async function loadSchedules() {
  const params = {}
  if (filter.doctorId) params.doctorId = filter.doctorId
  if (filter.startDate) params.startDate = filter.startDate
  const res = await fetchSchedules(params)
  schedules.value = res.data || []
}

async function handleCreate() {
  await createFormRef.value.validate()
  creating.value = true
  try {
    await createSchedule(createForm)
    ElMessage.success('排班创建成功（号源已自动生成）')
    showCreate.value = false
    await loadSchedules()
  } catch (e) { /* handled */ }
  finally { creating.value = false }
}

async function handleCancel(id) {
  await cancelSchedule(id)
  ElMessage.success('已停诊')
  await loadSchedules()
}

onMounted(() => loadSchedules())
</script>
