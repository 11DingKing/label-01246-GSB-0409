<template>
  <div class="user-page">
    <div class="page-card">
      <div class="page-header">
        <h3>用户管理</h3>
        <a-button type="primary" @click="openCreateModal" v-if="hasPerm('system:user:add')">
          <template #icon><icon-plus /></template>
          新增用户
        </a-button>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <a-input v-model="searchForm.username" placeholder="用户名" style="width: 180px" allow-clear />
        <a-input v-model="searchForm.realName" placeholder="真实姓名" style="width: 180px" allow-clear />
        <a-select v-model="searchForm.status" placeholder="状态" style="width: 140px" allow-clear>
          <a-option :value="0">禁用</a-option>
          <a-option :value="1">启用</a-option>
          <a-option :value="2">待审核</a-option>
          <a-option :value="3">待验证邮箱</a-option>
        </a-select>
        <a-button type="primary" @click="fetchData"><icon-search /> 搜索</a-button>
        <a-button @click="resetSearch">重置</a-button>
      </div>

      <!-- 表格 -->
      <a-table :data="tableData" :loading="loading" :pagination="false" row-key="id" stripe>
        <template #columns>
          <a-table-column title="ID" data-index="id" :width="60" />
          <a-table-column title="用户名" data-index="username" :width="120" />
          <a-table-column title="真实姓名" data-index="realName" :width="120" />
          <a-table-column title="邮箱" data-index="email" :width="180" />
          <a-table-column title="手机号" data-index="phone" :width="130" />
          <a-table-column title="状态" :width="100">
            <template #cell="{ record }">
              <a-tag :color="statusMap[record.status]?.color">{{ statusMap[record.status]?.text }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="锁定" :width="80">
            <template #cell="{ record }">
              <a-tag :color="record.accountLocked === 1 ? 'red' : 'green'">
                {{ record.accountLocked === 1 ? '已锁定' : '正常' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" data-index="createdAt" :width="170" />
          <a-table-column title="操作" :width="280" fixed="right">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" @click="openEditModal(record)" v-if="hasPerm('system:user:edit')">编辑</a-button>
                <a-button type="text" size="small" @click="openRoleModal(record)" v-if="hasPerm('system:user:assign')">分配角色</a-button>
                <a-button type="text" size="small" :status="record.status === 1 ? 'warning' : 'success'"
                  @click="toggleStatus(record)" v-if="hasPerm('system:user:status')">
                  {{ record.status === 1 ? '禁用' : '启用' }}
                </a-button>
                <a-button type="text" size="small" v-if="record.accountLocked === 1 && hasPerm('system:user:unlock')" status="success"
                  @click="handleUnlock(record)">解锁</a-button>
                <a-popconfirm content="确定重置密码为 123456？" @ok="handleResetPwd(record)">
                  <a-button type="text" size="small" status="warning" v-if="hasPerm('system:user:resetPwd')">重置密码</a-button>
                </a-popconfirm>
                <a-popconfirm content="确定删除该用户？" @ok="handleDelete(record)">
                  <a-button type="text" size="small" status="danger" v-if="hasPerm('system:user:delete')">删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <a-pagination
        :total="pagination.total"
        :current="pagination.current"
        :page-size="pagination.size"
        show-total
        show-page-size
        @change="onPageChange"
        @page-size-change="onPageSizeChange"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:visible="formVisible" :title="isEdit ? '编辑用户' : '新增用户'" @before-ok="handleSubmit" :ok-loading="submitLoading">
      <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
        <a-form-item label="用户名" field="username" v-if="!isEdit">
          <a-input v-model="formData.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="密码" field="password" v-if="!isEdit">
          <a-input-password v-model="formData.password" placeholder="请输入密码（至少6位）" />
        </a-form-item>
        <a-form-item label="真实姓名" field="realName">
          <a-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </a-form-item>
        <a-form-item label="邮箱" field="email">
          <a-input v-model="formData.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="手机号" field="phone">
          <a-input v-model="formData.phone" placeholder="请输入手机号" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 分配角色弹窗 -->
    <a-modal v-model:visible="roleVisible" title="分配角色" @ok="handleAssignRoles" :ok-loading="roleLoading">
      <a-checkbox-group v-model="selectedRoleIds" direction="vertical">
        <a-checkbox v-for="role in allRoles" :key="role.id" :value="role.id">
          {{ role.roleName }} ({{ role.roleCode }})
        </a-checkbox>
      </a-checkbox-group>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { getUserPage, createUser, updateUser, deleteUser, updateUserStatus, updateUserLock, assignUserRoles, getUserRoleIds } from '@/api/user'
import { getRoleList } from '@/api/role'
import { resetPassword } from '@/api/auth'
import { Message } from '@arco-design/web-vue'
import { containsXss, stripTags } from '@/utils/xss'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (code) => userStore.permissions.length === 0 || userStore.hasPermission(code)

const statusMap = {
  0: { text: '禁用', color: 'red' },
  1: { text: '启用', color: 'green' },
  2: { text: '待审核', color: 'orange' },
  3: { text: '待验证邮箱', color: 'blue' }
}

const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 10, total: 0 })
const searchForm = ref({ username: '', realName: '', status: undefined })

const formVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formData = ref({})
const formRef = ref(null)

const formRules = {
  username: [
    { required: true, message: '请输入用户名' }
  ],
  password: [
    { required: true, message: '请输入密码' },
    { minLength: 6, message: '密码长度不少于6位' }
  ],
  email: [
    { match: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '邮箱格式不正确' }
  ],
  phone: [
    { match: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
  ]
}

const roleVisible = ref(false)
const roleLoading = ref(false)
const selectedRoleIds = ref([])
const allRoles = ref([])
const currentUserId = ref(null)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserPage({
      current: pagination.value.current,
      size: pagination.value.size,
      ...searchForm.value
    })
    tableData.value = res.data.records
    pagination.value.total = res.data.total
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.value = { username: '', realName: '', status: undefined }
  pagination.value.current = 1
  fetchData()
}

const onPageChange = (page) => {
  pagination.value.current = page
  fetchData()
}
const onPageSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.current = 1
  fetchData()
}

const openCreateModal = () => {
  isEdit.value = false
  formData.value = { username: '', password: '', realName: '', email: '', phone: '' }
  formVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

const openEditModal = (record) => {
  isEdit.value = true
  formData.value = { ...record }
  formVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

const handleSubmit = async (done) => {
  try {
    const valid = await formRef.value?.validate()
    if (valid) {
      done(false)
      return
    }
    // XSS 输入检测
    const fields = ['username', 'realName', 'email', 'phone']
    for (const field of fields) {
      const val = formData.value[field]
      if (val && containsXss(val)) {
        Message.warning(`${field} 中包含不安全的内容，请修改`)
        done(false)
        return
      }
      // 自动清除 HTML 标签
      if (val && typeof val === 'string') {
        formData.value[field] = stripTags(val)
      }
    }
    submitLoading.value = true
    if (isEdit.value) {
      await updateUser(formData.value.id, formData.value)
    } else {
      await createUser(formData.value)
    }
    Message.success(isEdit.value ? '更新成功' : '创建成功')
    done(true)
    fetchData()
  } catch (e) {
    done(false)
  } finally {
    submitLoading.value = false
  }
}

const toggleStatus = async (record) => {
  const newStatus = record.status === 1 ? 0 : 1
  await updateUserStatus(record.id, newStatus)
  Message.success('状态更新成功')
  fetchData()
}

const handleUnlock = async (record) => {
  await updateUserLock(record.id, 0)
  Message.success('解锁成功')
  fetchData()
}

const handleResetPwd = async (record) => {
  await resetPassword({ userId: record.id })
  Message.success('密码已重置为 123456')
}

const handleDelete = async (record) => {
  await deleteUser(record.id)
  Message.success('删除成功')
  fetchData()
}

const openRoleModal = async (record) => {
  currentUserId.value = record.id
  // 加载所有角色
  if (allRoles.value.length === 0) {
    const res = await getRoleList()
    allRoles.value = res.data
  }
  // 加载用户当前角色
  const res = await getUserRoleIds(record.id)
  selectedRoleIds.value = res.data || []
  roleVisible.value = true
}

const handleAssignRoles = async () => {
  roleLoading.value = true
  try {
    await assignUserRoles(currentUserId.value, selectedRoleIds.value)
    Message.success('角色分配成功')
    roleVisible.value = false
  } finally {
    roleLoading.value = false
  }
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.user-page {
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
