<template>
  <div>
    <el-card>
      <template #header><h3>用户管理</h3></template>
      <el-alert type="info" :closable="false" style="margin-bottom:16px;">
        用户管理功能（新增/编辑/禁用系统用户）可在此页面扩展实现。当前展示默认账号信息供参考。
      </el-alert>
      <el-table :data="users" border stripe>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="role" label="角色" />
        <el-table-column prop="password" label="默认密码" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const users = ref([
  { username: 'admin', realName: '系统管理员', role: '超级管理员', password: '123456' },
  { username: 'dept_admin_nk', realName: '内科管理员', role: '科室管理员', password: '123456' },
  { username: 'dept_admin_wk', realName: '外科管理员', role: '科室管理员', password: '123456' },
  { username: 'doctor_zhang', realName: '张医生', role: '医生', password: '123456' },
  { username: 'doctor_li', realName: '李医生', role: '医生', password: '123456' },
  { username: 'doctor_wang', realName: '王医生', role: '医生', password: '123456' }
])
</script>
