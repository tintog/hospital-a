<template>
  <el-container class="doctor-layout" style="height: 100%;">
    <el-aside width="200px" class="doctor-aside">
      <div class="aside-header">
        <el-icon :size="24" color="#fff"><Stethoscope /></el-icon>
        <span>医生工作站</span>
      </div>
      <el-menu
        :default-active="currentRoute"
        router
        background-color="#545c64"
        text-color="#bfcbd9"
        active-text-color="#67C23A"
      >
        <el-menu-item index="/doctor/schedule">
          <el-icon><Calendar /></el-icon>
          <span>我的排班</span>
        </el-menu-item>
        <el-menu-item index="/doctor/patients">
          <el-icon><User /></el-icon>
          <span>我的患者</span>
        </el-menu-item>
        <el-menu-item index="/doctor/profile">
          <el-icon><Setting /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="doctor-header">
        <span>{{ userStore.realName }} 医生</span>
        <el-button type="danger" link @click="handleLogout">退出</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const currentRoute = computed(() => route.path)

function handleLogout() {
  userStore.logout()
  router.push('/admin-login')
}
</script>

<style scoped>
.doctor-aside {
  background: #545c64;
  overflow-y: auto;
}
.aside-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #545c64;
}
.doctor-header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  font-size: 14px;
  color: #606266;
}
</style>
