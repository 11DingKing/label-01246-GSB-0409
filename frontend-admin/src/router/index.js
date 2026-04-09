import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据仪表盘', icon: 'icon-dashboard' }
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('@/views/monitor/index.vue'),
        meta: { title: '实时监控', icon: 'icon-computer' }
      },
      {
        path: 'fault',
        name: 'FaultAnalysis',
        component: () => import('@/views/fault/index.vue'),
        meta: { title: '故障分析', icon: 'icon-bug' }
      },
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('@/views/config/index.vue'),
        meta: { title: '系统配置', icon: 'icon-settings' }
      },
      {
        path: 'system/user',
        name: 'UserManage',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'icon-user' }
      },
      {
        path: 'system/role',
        name: 'RoleManage',
        component: () => import('@/views/role/index.vue'),
        meta: { title: '角色管理', icon: 'icon-user-group' }
      },
      {
        path: 'system/permission',
        name: 'PermissionManage',
        component: () => import('@/views/permission/index.vue'),
        meta: { title: '权限管理', icon: 'icon-lock' }
      },
      {
        path: 'system/log',
        name: 'LogManage',
        component: () => import('@/views/log/index.vue'),
        meta: { title: '操作日志', icon: 'icon-file' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.path === '/login') {
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
    return
  }

  if (!token) {
    next('/login')
    return
  }

  const userStore = useUserStore()
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      userStore.logout()
      return
    }
  }

  // 权限校验：从 menus 树中提取所有可访问的路径
  if (to.path !== '/dashboard' && to.meta?.requiresAuth !== false) {
    const allowedPaths = getAllMenuPaths(userStore.menus)
    if (allowedPaths.length > 0 && !allowedPaths.includes(to.path)) {
      next('/dashboard')
      return
    }
  }

  next()
})

// 递归提取菜单树中所有 path
function getAllMenuPaths(menus) {
  const paths = []
  for (const menu of menus) {
    if (menu.path) paths.push(menu.path)
    if (menu.children && menu.children.length) {
      paths.push(...getAllMenuPaths(menu.children))
    }
  }
  return paths
}

export default router
