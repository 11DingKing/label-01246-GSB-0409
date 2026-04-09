<template>
  <div class="permission-page">
    <div class="page-card">
      <div class="page-header">
        <h3>权限管理</h3>
        <a-button type="primary" @click="openCreateModal(0)" v-if="hasPerm('system:perm:add')">
          <template #icon><icon-plus /></template>
          新增顶级权限
        </a-button>
      </div>

      <a-table :data="permTree" :loading="loading" row-key="id" :pagination="false" default-expand-all-rows>
        <template #columns>
          <a-table-column title="权限名称" data-index="permName" :width="200" />
          <a-table-column title="权限编码" data-index="permCode" :width="200" />
          <a-table-column title="类型" :width="100">
            <template #cell="{ record }">
              <a-tag :color="typeMap[record.permType]?.color">{{ typeMap[record.permType]?.text }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="路径" data-index="path" :width="180" />
          <a-table-column title="图标" data-index="icon" :width="120" />
          <a-table-column title="排序" data-index="sortOrder" :width="80" />
          <a-table-column title="操作" :width="220" fixed="right">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" @click="openCreateModal(record.id)" v-if="record.permType === 'MENU' && hasPerm('system:perm:add')">
                  添加子权限
                </a-button>
                <a-button type="text" size="small" @click="openEditModal(record)" v-if="hasPerm('system:perm:edit')">编辑</a-button>
                <a-popconfirm content="确定删除该权限？" @ok="handleDelete(record)">
                  <a-button type="text" size="small" status="danger" v-if="hasPerm('system:perm:delete')">删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:visible="formVisible" :title="isEdit ? '编辑权限' : '新增权限'" @ok="handleSubmit" :ok-loading="submitLoading">
      <a-form :model="formData" layout="vertical">
        <a-form-item label="权限编码" required>
          <a-input v-model="formData.permCode" placeholder="如: system:user:add" />
        </a-form-item>
        <a-form-item label="权限名称" required>
          <a-input v-model="formData.permName" placeholder="如: 新增用户" />
        </a-form-item>
        <a-form-item label="权限类型" required>
          <a-select v-model="formData.permType">
            <a-option value="MENU">菜单</a-option>
            <a-option value="BUTTON">按钮</a-option>
            <a-option value="API">接口</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="路由路径" v-if="formData.permType === 'MENU'">
          <a-input v-model="formData.path" placeholder="如: /system/user" />
        </a-form-item>
        <a-form-item label="图标" v-if="formData.permType === 'MENU'">
          <a-input v-model="formData.icon" placeholder="如: icon-user" />
        </a-form-item>
        <a-form-item label="组件路径" v-if="formData.permType === 'MENU'">
          <a-input v-model="formData.component" placeholder="如: user/index" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPermissionTree, createPermission, updatePermission, deletePermission } from '@/api/permission'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (code) => userStore.permissions.length === 0 || userStore.hasPermission(code)

const typeMap = {
  MENU: { text: '菜单', color: 'blue' },
  BUTTON: { text: '按钮', color: 'green' },
  API: { text: '接口', color: 'orange' }
}

const loading = ref(false)
const permTree = ref([])

const formVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formData = ref({})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPermissionTree()
    permTree.value = res.data
  } finally {
    loading.value = false
  }
}

const openCreateModal = (parentId) => {
  isEdit.value = false
  formData.value = { parentId, permCode: '', permName: '', permType: 'MENU', path: '', icon: '', component: '', sortOrder: 0 }
  formVisible.value = true
}

const openEditModal = (record) => {
  isEdit.value = true
  formData.value = { ...record }
  formVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updatePermission(formData.value.id, formData.value)
    } else {
      await createPermission(formData.value)
    }
    Message.success(isEdit.value ? '更新成功' : '创建成功')
    formVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (record) => {
  await deletePermission(record.id)
  Message.success('删除成功')
  fetchData()
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.permission-page {
  .page-card {
    background: $bg-card;
    border-radius: $border-radius-card;
    padding: $spacing-lg;
    box-shadow: $shadow-card;
    border: 1px solid $border-color;
  }
  :deep(.arco-btn-text) {
    transition: color 0.2s ease, background 0.2s ease;
    &:hover {
      background: rgba(22, 93, 255, 0.08);
    }
  }
}
</style>
