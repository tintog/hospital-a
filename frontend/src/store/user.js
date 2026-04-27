import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const realName = ref(localStorage.getItem('realName') || '')
  const role = ref(localStorage.getItem('role') || '')
  const phone = ref(localStorage.getItem('phone') || '')

/**
 * 设置用户登录信息函数
 * 该函数将接收到的用户数据保存到响应式变量和localStorage中
*/
  function setLoginInfo(data) {

    // 将用户信息保存到响应式变量中
    token.value = data.token           // 设置认证令牌
    userId.value = data.userId         // 设置用户ID
    username.value = data.username     // 设置用户名
    realName.value = data.realName || ''  // 设置真实姓名，如果不存在则为空字符串
    role.value = data.role             // 设置用户角色
    phone.value = data.phone || data.username || ''  // 设置手机号，如果不存在则使用用户名，如果用户名也不存在则为空字符串



    // 将用户信息保存到localStorage中，以便页面刷新后可以保持登录状态
    localStorage.setItem('token', data.token)           // 存储认证令牌
    localStorage.setItem('userId', data.userId)         // 存储用户ID
    localStorage.setItem('username', data.username)     // 存储用户名
    localStorage.setItem('realName', data.realName || '')  // 存储真实姓名，如果不存在则为空字符串
    localStorage.setItem('role', data.role)             // 存储用户角色
    localStorage.setItem('phone', data.phone || data.username || '')  // 存储手机号，如果不存在则使用用户名，如果用户名也不存在则为空字符串
  }

  function logout() {
    token.value = ''
    userId.value = ''
    username.value = ''
    realName.value = ''
    role.value = ''
    phone.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('realName')
    localStorage.removeItem('role')
    localStorage.removeItem('phone')
  }

  const isLoggedIn = () => !!token.value

  return { token, userId, username, realName, role, phone, setLoginInfo, logout, isLoggedIn }
})
