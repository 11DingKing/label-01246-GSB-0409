import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo as getUserInfoApi, logout as logoutApi } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null,
    roleIds: [],
    permissions: [],
    menus: [],
    dataScope: 1
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    realName: (state) => state.userInfo?.realName || ''
  },

  actions: {
    async login(username, password) {
      const res = await loginApi({ username, password })
      this.token = res.data.token
      localStorage.setItem('token', res.data.token)
      return res
    },

    async fetchUserInfo() {
      const res = await getUserInfoApi()
      this.userInfo = res.data.user
      this.roleIds = res.data.roleIds || []
      this.permissions = res.data.permissions || []
      this.menus = res.data.menus || []
      this.dataScope = res.data.dataScope || 1
      return res.data
    },

    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        // ignore
      }
      this.token = ''
      this.userInfo = null
      this.roleIds = []
      this.permissions = []
      this.menus = []
      this.dataScope = 1
      localStorage.removeItem('token')
      router.push('/login')
    },

    hasPermission(permCode) {
      return this.permissions.includes(permCode)
    }
  }
})
