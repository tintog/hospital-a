<template>
  <el-container class="admin-layout" style="height: 100%;">
    <el-aside width="220px" class="admin-aside">
      <div class="aside-header">
        <el-icon :size="24" color="#fff"><Setting /></el-icon>
        <span>管理后台</span>
      </div>
      <el-menu
        :default-active="currentRoute"
        router
        background-color="#545c64"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据面板</span>
        </el-menu-item>
        <el-menu-item index="/admin/patients">
          <el-icon><User /></el-icon>
          <span>患者管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/doctors">
          <el-icon><UserFilled /></el-icon>
          <span>医生管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/departments">
          <el-icon><OfficeBuilding /></el-icon>
          <span>科室管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/schedule">
          <el-icon><Calendar /></el-icon>
          <span>排班管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/appointments">
          <el-icon><List /></el-icon>
          <span>预约管理</span>
        </el-menu-item>
    <!--   <el-menu-item index="/admin/statistics">
          <el-icon><TrendCharts /></el-icon>
          <span>统计报表</span>
        </el-menu-item>-->
        <el-menu-item index="/admin/users">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <span>{{ userStore.realName }} ({{ userStore.role === 'admin' ? '超级管理员' : '科室管理员' }})</span>
        <el-button type="danger" link @click="handleLogout">退出登录</el-button>
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
.admin-aside {
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
.admin-header {
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
