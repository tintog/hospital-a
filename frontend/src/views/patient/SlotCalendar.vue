<template>
  <div class="slot-calendar">
    <el-card>
      <template #header>
        <div style="display:flex;align-items:center;justify-content:space-between;">
          <h3>选择号源</h3>
          <el-date-picker
            v-model="selectedDate"
            type="date"
            :disabled-date="disabledDate"
            @change="loadSlots"
            placeholder="选择就诊日期"
            value-format="YYYY-MM-DD"
          />
        </div>
      </template>

      <el-table :data="slots" border stripe style="width: 100%">
        <el-table-column prop="slotNo" label="号序" width="100" />
        <el-table-column label="就诊时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="title" label="职称" width="110" />
        <el-table-column label="费用" width="80">
          <template #default="{ row }">¥{{ row.fee }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]">{{ statusText[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" :disabled="row.status !== 0" @click="handleBook(row)">
              预约
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="slots.length === 0" description="该日期暂无号源" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="确认预约" width="480px" :close-on-click-modal="false">
      <el-form :model="bookForm" label-width="100px">
        <el-form-item label="就诊人">
          <el-select v-model="bookForm.memberId" placeholder="选择就诊人" clearable>
            <el-option :label="selfMemberLabel" :value="null" />
            <el-option v-for="m in members" :key="m.id" :label="`${m.name}(${m.relation})`" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="就诊时间">
          {{ formatTime(currentSlot?.startTime) }} - {{ formatTime(currentSlot?.endTime) }}
        </el-form-item>
        <el-form-item label="医事服务费">
          <span style="font-size:18px;font-weight:600;color:#f56c6c;">¥{{ currentSlot?.fee }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitOrder">确认预约并支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchSlotsByDate, createAppointment, mockPaymentCallback } from '@/api/appointment'
import { fetchMembers } from '@/api/patient'
import { useUserStore } from '@/store/user'
import dayjs from 'dayjs'

const props = defineProps({ doctorId: [String, Number] })
const router = useRouter()
const userStore = useUserStore()

const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const slots = ref([])
const members = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const currentSlot = ref(null)
const bookForm = ref({ memberId: null })

const statusText = { 0: '可预约', 1: '锁定中', 2: '已预约', 3: '已取消', 4: '已就诊' }
const statusType = { 0: 'success', 1: 'warning', 2: 'info', 3: 'danger', 4: '' }
const selfMemberLabel = computed(() => {
  if (userStore.realName) return `${userStore.realName}（本人）`
  return '本人'
})

const disabledDate = (time) => time.getTime() < Date.now() - 86400000

async function loadSlots() {
  try {
    const res = await fetchSlotsByDate({ doctorId: props.doctorId, date: selectedDate.value })
    slots.value = res.data || []
  } catch (e) { /* handled */ }
}

onMounted(async () => {
  await loadSlots()
  try {
    const res = await fetchMembers()
    members.value = res.data || []
  } catch (e) { /* handled */ }
})

function handleBook(slot) {
  currentSlot.value = slot
  bookForm.value.memberId = null
  dialogVisible.value = true
}

async function submitOrder() {
  submitting.value = true
  try {
    const res = await createAppointment({
      slotId: currentSlot.value.id,
      doctorId: Number(props.doctorId),
      deptId: currentSlot.value.deptId || 1,
      fee: currentSlot.value.fee,
      memberId: bookForm.value.memberId
    })

    dialogVisible.value = false

    await ElMessageBox.confirm(
      `<div style="text-align:center;">
        <p style="font-size:16px;margin-bottom:12px;">订单号: <b>${res.data.orderNo}</b></p>
        <p>金额: <b style="color:#f56c6c;">¥${res.data.fee}</b></p>
        <p style="color:#909399;margin-top:8px;">请在 ${res.data.expireSeconds} 秒内完成支付</p>
        <p style="color:#409EFF;margin-top:16px;">[ 模拟支付环境 - 点击确认即支付成功 ]</p>
      </div>`,
      '模拟支付',
      {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消订单',
        dangerouslyUseHTMLString: true,
        type: 'info'
      }
    )

    await mockPaymentCallback({ orderNo: res.data.orderNo, transactionId: 'MOCK_' + Date.now() })
    ElMessage.success('支付成功，预约已确认！')
    await loadSlots()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      // error handled by interceptor
    }
  } finally {
    submitting.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  return t.substring(11, 16)
}
</script>

<style scoped>
.slot-calendar { max-width: 900px; margin: 0 auto; }
</style>
