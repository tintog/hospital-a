<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <div class="header-left">
        <el-icon :size="28" color="#409EFF"><FirstAidKit /></el-icon>
        <span class="app-title">医院预约挂号系统</span>
      </div>
      <el-menu mode="horizontal" :default-active="currentRoute" router :ellipsis="false" class="header-menu">
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>首页
        </el-menu-item>
        <el-menu-item index="/departments">
          <el-icon><OfficeBuilding /></el-icon>科室导航
        </el-menu-item>
        <el-menu-item index="/appointments">
          <el-icon><List /></el-icon>我的预约
        </el-menu-item>
        <el-menu-item index="/members">
          <el-icon><User /></el-icon>就诊人管理
        </el-menu-item>
      </el-menu>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-icon><UserFilled /></el-icon>
            {{ userStore.realName || userStore.username }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="realname" v-if="!userStore.realName">实名认证</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="layout-main">
      <router-view />
    </el-main>
    <el-footer class="layout-footer">
      <p>医院预约挂号系统 &copy; 2026</p>
    </el-footer>
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

function handleCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (cmd === 'realname') {
    router.push('/realname')
  }
}
</script>

<style scoped>
.layout-container { height: 100%; }
.layout-header {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 40px;
  flex-shrink: 0;
}
.app-title { font-size: 18px; font-weight: 600; color: #303133; white-space: nowrap; }
.header-menu { flex: 1; border-bottom: none; }
.header-right { flex-shrink: 0; margin-left: 20px; }
.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
}
.layout-main {
  background: #f5f7fa;
  min-height: 0;
  padding: 20px;
}
.layout-footer {
  text-align: center;
  color: #909399;
  font-size: 13px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
}
</style>
