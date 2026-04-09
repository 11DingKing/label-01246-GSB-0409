<template>
  <a-layout class="main-layout">
    <!-- 侧边栏 -->
    <a-layout-sider
      :width="240"
      :collapsed-width="64"
      collapsible
      :collapsed="collapsed"
      @collapse="onCollapse"
      class="layout-sider"
    >
      <div class="logo-area">
        <icon-thunderbolt :size="24" style="color: #165DFF" />
        <span v-if="!collapsed" class="logo-text">风电故障诊断</span>
      </div>
      <a-menu
        :selected-keys="[currentRoute]"
        :default-open-keys="openKeys"
        @menu-item-click="onMenuClick"
        theme="dark"
        :style="{ background: 'transparent' }"
      >
        <template v-for="menu in userStore.menus" :key="menu.path">
          <!-- 有子菜单 -->
          <a-sub-menu v-if="menu.children && menu.children.length" :key="menu.permCode">
            <template #icon><component :is="menu.icon" /></template>
            <template #title>{{ menu.permName }}</template>
            <a-menu-item v-for="child in menu.children" :key="child.path">
              <template #icon><component :is="child.icon" /></template>
              {{ child.permName }}
            </a-menu-item>
          </a-sub-menu>
          <!-- 无子菜单 -->
          <a-menu-item v-else :key="menu.path">
            <template #icon><component :is="menu.icon" /></template>
            {{ menu.permName }}
          </a-menu-item>
        </template>
      </a-menu>
    </a-layout-sider>

    <!-- 右侧内容 -->
    <a-layout class="layout-content-wrapper">
      <!-- 顶部栏 -->
      <a-layout-header class="layout-header">
        <div class="header-left">
          <a-breadcrumb>
            <a-breadcrumb-item>首页</a-breadcrumb-item>
            <a-breadcrumb-item>{{ currentTitle }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="header-right">
          <a-dropdown trigger="click">
            <div class="user-info">
              <a-avatar :size="32" style="background-color: #165DFF">
                {{ userStore.realName?.charAt(0) || 'U' }}
              </a-avatar>
              <span class="user-name">{{ userStore.realName || userStore.username }}</span>
              <icon-down />
            </div>
            <template #content>
              <a-doption @click="showPasswordModal = true">
                <icon-lock /> 修改密码
              </a-doption>
              <a-doption @click="handleLogout">
                <icon-export /> 退出登录
              </a-doption>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 页面内容 -->
      <a-layout-content class="layout-content">
        <transition name="fade" mode="out-in">
          <router-view />
        </transition>
      </a-layout-content>
    </a-layout>

    <!-- 修改密码弹窗 -->
    <a-modal v-model:visible="showPasswordModal" title="修改密码" @ok="handleChangePassword" :ok-loading="pwdLoading">
      <a-form :model="pwdForm" layout="vertical">
        <a-form-item label="原密码" required>
          <a-input-password v-model="pwdForm.oldPassword" placeholder="请输入原密码" />
        </a-form-item>
        <a-form-item label="新密码" required>
          <a-input-password v-model="pwdForm.newPassword" placeholder="请输入新密码（至少6位）" />
        </a-form-item>
        <a-form-item label="确认密码" required>
          <a-input-password v-model="pwdForm.confirmPassword" placeholder="请再次输入新密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-layout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { changePassword } from '@/api/auth'
import { Message } from '@arco-design/web-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const collapsed = ref(false)
const showPasswordModal = ref(false)
const pwdLoading = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const currentRoute = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '首页')

// 自动展开当前路由所在的父菜单
const openKeys = computed(() => {
  return userStore.menus
    .filter(m => m.children && m.children.length && m.children.some(c => route.path === c.path))
    .map(m => m.permCode)
})

const onCollapse = (val) => { collapsed.value = val }
const onMenuClick = (key) => { router.push(key) }

const handleLogout = () => {
  userStore.logout()
}

const handleChangePassword = async () => {
  if (!pwdForm.value.oldPassword) {
    Message.warning('请输入原密码')
    return
  }
  if (!pwdForm.value.newPassword || pwdForm.value.newPassword.length < 6) {
    Message.warning('新密码长度不少于6位')
    return
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    Message.warning('两次输入的密码不一致')
    return
  }
  pwdLoading.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })
    Message.success('密码修改成功，请重新登录')
    showPasswordModal.value = false
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    userStore.logout()
  } catch (e) {
    // handled by interceptor
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}

.layout-sider {
  background: $bg-sidebar !important;
  border-right: 1px solid $border-color;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.2);
  overflow: hidden;

  :deep(.arco-layout-sider-children) {
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  :deep(.arco-layout-sider-trigger) {
    background: $bg-sidebar;
    border-top: 1px solid $border-color;
    transition: background 0.2s ease;
    &:hover {
      background: $bg-hover;
    }
  }
  // 统一所有菜单项间距
  :deep(.arco-menu) {
    padding: 4px 0 !important;
  }
  :deep(.arco-menu-inner) {
    padding: 0 !important;
  }
  :deep(.arco-menu-item) {
    border-radius: 6px;
    margin: 0 8px !important;
    margin-top: 4px !important;
    line-height: 40px !important;
    height: 40px !important;
    transition: background 0.2s ease, color 0.2s ease;
    &:hover {
      background: $bg-hover;
    }
  }
  :deep(.arco-menu-item.arco-menu-selected) {
    background: rgba(22, 93, 255, 0.15);
  }
  :deep(.arco-menu-inline-header) {
    border-radius: 6px;
    margin: 0 8px !important;
    margin-top: 4px !important;
    line-height: 40px !important;
    height: 40px !important;
    padding-top: 0 !important;
    padding-bottom: 0 !important;
    transition: background 0.2s ease, color 0.2s ease;
    &:hover {
      background: $bg-hover;
    }
  }
  :deep(.arco-menu-inline-content) {
    background: transparent !important;
    padding: 0 !important;
    margin: 0 !important;
  }
  // 二级菜单图标与一级菜单"系"字左边缘对齐
  :deep(.arco-menu-inline .arco-menu-inline-content .arco-menu-item) {
    margin: 0 8px !important;
    margin-top: 4px !important;
    padding-left: 22px !important;
  }
  // 消除子菜单展开后额外的间距
  :deep(.arco-menu-inline) {
    margin: 0 !important;
    padding: 0 !important;
  }
  :deep(.arco-menu-inline-header) + :deep(.arco-menu-inline-content) {
    margin-top: 0 !important;
  }
  // 收起状态：图标居中对齐、背景宽、图标统一
  :deep(.arco-menu-collapsed) {
    padding: 4px 0 !important;
    width: 100% !important;

    .arco-menu-item,
    .arco-menu-inline-header,
    .arco-menu-pop-header {
      margin: 4px 0 0 !important;
      padding: 0 !important;
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      width: 100% !important;
      height: 44px !important;
      line-height: 44px !important;
      border-radius: 0 !important;
    }

    .arco-menu-icon {
      margin-right: 0 !important;
      margin-left: 0 !important;
      padding: 0 !important;
      width: 20px !important;
      height: 20px !important;
      font-size: 20px !important;
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      flex-shrink: 0;
    }

    // 隐藏收起后残留的文字和箭头
    .arco-menu-title,
    .arco-menu-icon-suffix {
      display: none !important;
    }
  }

  // 收起状态下 sider children 占满宽度
  &.arco-layout-sider-collapsed {
    :deep(.arco-layout-sider-children) {
      width: 100% !important;
      overflow: hidden;
    }
    :deep(.arco-menu) {
      width: 100% !important;
    }
    :deep(.arco-menu-inner) {
      width: 100% !important;
    }
  }
}

.logo-area {
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid $border-color;
  padding: 0 16px;
  flex-shrink: 0;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}
.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
  white-space: nowrap;
}

.layout-content-wrapper {
  background: $bg-main;
}

.layout-header {
  height: $header-height;
  background: $bg-sidebar;
  border-bottom: 1px solid $border-color;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-lg;
  position: relative;
  z-index: 10;
}
.header-left {
  :deep(.arco-breadcrumb-item) {
    color: $text-secondary;
    transition: color 0.2s ease;
    &:hover {
      color: $text-primary;
    }
  }
  :deep(.arco-breadcrumb-item-separator) {
    color: $text-disabled;
  }
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s ease;
  &:hover {
    background: $bg-hover;
  }
}
.user-name {
  color: $text-primary;
  font-size: 14px;
}

.layout-content {
  padding: $spacing-lg;
  overflow-y: auto;
  height: calc(100vh - #{$header-height});
  background: $bg-main;
}
</style>
