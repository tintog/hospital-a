/**
 * 导入必要的模块和库
 * axios: 用于发送HTTP请求
 * ElMessage: Element Plus的消息提示组件
 * router: 路由实例，用于页面导航
 * useUserStore: 用户状态管理store
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/store/user'

/**
 * 创建axios实例
 * 配置基础URL、超时时间和请求头
 */
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL, // 从环境变量中获取API基础URL
  timeout: 15000, // 请求超时时间设置为15秒
  headers: { 'Content-Type': 'application/json' } // 设置默认请求头为JSON格式
})

/**
 * 请求拦截器
 * 在请求发送前进行处理
 */
service.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    // 如果存在token，将其添加到请求头中
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

/**
 * 响应拦截器
 * 对响应结果进行处理
 * @param {Object} response - 响应对象
 * @param {Function} error - 错误处理函数
 */
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      // 如果响应状态码不是200，显示错误消息
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        // 如果是401未授权错误，清除用户状态并跳转到登录页
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    // 处理响应错误
    const status = error.response?.status
    const backendMessage = error.response?.data?.message
    const msg = backendMessage || error.message || '网络错误'

    if (status === 401) {
      // 如果是401错误，清除用户状态并跳转到登录页
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else if (status === 409) {
      ElMessage.error('号源被占用，请刷新重试')
    } else if (status === 500) {
      ElMessage.error('系统繁忙，请稍后重试')
    } else {
      // 显示其他错误信息
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default service
