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
    path: '/',
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
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'patients', name: 'PatientManage', component: () => import('@/views/admin/PatientManage.vue') },
      { path: 'doctors', name: 'DoctorManage', component: () => import('@/views/admin/DoctorManage.vue') },
      { path: 'schedule', name: 'ScheduleManage', component: () => import('@/views/admin/ScheduleManage.vue') },
      { path: 'appointments', name: 'AdminAppointments', component: () => import('@/views/admin/AppointmentList.vue') },
      { path: 'statistics', name: 'Statistics', component: () => import('@/views/admin/Statistics.vue') },
      { path: 'users', name: 'SystemUser', component: () => import('@/views/admin/SystemUser.vue') }
    ]
  },
  {
    path: '/doctor',
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
    path: '/403',
    name: 'Forbidden',
    component: { template: '<div style="text-align:center;padding:100px"><h1>403</h1><p>权限不足</p></div>' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth === false) {
    next()
    return
  }

  if (!userStore.token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  const routeRole = to.meta.role
  const userRole = userStore.role

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
