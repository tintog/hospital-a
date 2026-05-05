import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/patient/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/patient/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/admin-login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLogin.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',//用户页面
    component: () => import('@/layouts/PatientLayout.vue'),
    meta: { requiresAuth: true, role: 'patient' },
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', name: 'Home', component: () => import('@/views/patient/Home.vue') },
      { path: 'departments', name: 'Departments', component: () => import('@/views/patient/DepartmentList.vue') },
      { path: 'doctor/:id', name: 'DoctorDetail', component: () => import('@/views/patient/DoctorDetail.vue'), props: true },
      { path: 'booking/:doctorId', name: 'SlotCalendar', component: () => import('@/views/patient/SlotCalendar.vue'), props: true },
      { path: 'appointments', name: 'MyAppointments', component: () => import('@/views/patient/MyAppointments.vue') },
      { path: 'members', name: 'MemberManage', component: () => import('@/views/patient/MemberManage.vue') },
      { path: 'profile', name: 'PatientProfile', component: () => import('@/views/patient/Profile.vue') },
      { path: 'realname', name: 'RealNameAuth', component: () => import('@/views/patient/RealNameAuth.vue') }
    ]
  },
  {
    path: '/admin',//管理员页面
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'patients', name: 'PatientManage', component: () => import('@/views/admin/PatientManage.vue') },
      { path: 'doctors', name: 'DoctorManage', component: () => import('@/views/admin/DoctorManage.vue') },
      { path: 'departments', name: 'DepartmentManage', component: () => import('@/views/admin/DepartmentManage.vue') },
      { path: 'schedule', name: 'ScheduleManage', component: () => import('@/views/admin/ScheduleManage.vue') },
      { path: 'appointments', name: 'AdminAppointments', component: () => import('@/views/admin/AppointmentList.vue') },
     // { path: 'statistics', name: 'Statistics', component: () => import('@/views/admin/Statistics.vue') },
      { path: 'users', name: 'SystemUser', component: () => import('@/views/admin/SystemUser.vue') }
    ]
  },
  {
    path: '/doctor',//医生页面
    component: () => import('@/layouts/DoctorLayout.vue'),
    meta: { requiresAuth: true, role: 'doctor' },
    children: [
      { path: '', redirect: '/doctor/schedule' },
      { path: 'schedule', name: 'DoctorSchedule', component: () => import('@/views/doctor/MySchedule.vue') },
      { path: 'patients', name: 'TodayPatients', component: () => import('@/views/doctor/TodayPatients.vue') },
      { path: 'profile', name: 'DoctorProfile', component: () => import('@/views/doctor/Profile.vue') }
    ]
  },
  {
    path: '/403',//找不到页面
    name: 'Forbidden',
    component: { template: '<div style="text-align:center;padding:100px"><h1>403</h1><p>权限不足</p></div>' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 路由前置守卫
 * 在路由跳转前执行，用于权限验证、登录状态检查等
 * @param {Object} to - 即将要进入的目标路由对象
 * @param {Object} _from - 当前导航正要离开的路由对象
 * @param {Function} next - 必须调用的函数，用于 resolve 这个钩子，进行下一步操作
 */
router.beforeEach((to, _from, next) => {
  // 获取用户状态管理store
  const userStore = useUserStore()

// 检查目标路由的元信息是否不需要认证
  if (to.meta.requiresAuth === false) {
  // 如果不需要认证，则直接放行，进入下一个导航钩子
    next()
    return
  }

  if (!userStore.token) {
// query参数传递查询字符串，包含redirect字段，值为当前要访问的完整路径(to.fullPath)
// 这样可以在用户登录后重定向回原本想要访问的页面
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  const routeRole = to.meta.role
  const userRole = userStore.role

  // 检查用户角色是否为管理员，并且当前用户角色不是管理员或部门管理员
  if (routeRole === 'admin' && !['admin', 'dept_admin'].includes(userRole)) {
    next({ path: '/403' })
     return
  }
  if (routeRole === 'doctor' && userRole !== 'doctor') {
    next({ path: '/403' })
    return
  }

  next()
})

export default router
