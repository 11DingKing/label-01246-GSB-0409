<template>
  <div class="role-page">
    <div class="page-card">
      <div class="page-header">
        <h3>角色管理</h3>
        <a-button type="primary" @click="openCreateModal" v-if="hasPerm('system:role:add')">
          <template #icon><icon-plus /></template>
          新增角色
        </a-button>
      </div>

      <div class="search-bar">
        <a-input v-model="searchForm.roleName" placeholder="角色名称" style="width: 180px" allow-clear />
        <a-select v-model="searchForm.status" placeholder="状态" style="width: 140px" allow-clear>
          <a-option :value="0">禁用</a-option>
          <a-option :value="1">启用</a-option>
        </a-select>
        <a-button type="primary" @click="fetchData"><icon-search /> 搜索</a-button>
        <a-button @click="resetSearch">重置</a-button>
      </div>

      <a-table :data="tableData" :loading="loading" :pagination="false" row-key="id" stripe>
        <template #columns>
          <a-table-column title="ID" data-index="id" :width="60" />
          <a-table-column title="角色编码" data-index="roleCode" :width="140" />
          <a-table-column title="角色名称" data-index="roleName" :width="140" />
          <a-table-column title="描述" data-index="description" :width="200" />
          <a-table-column title="数据范围" :width="140">
            <template #cell="{ record }">
              <a-tag :color="dataScopeColor(record.dataScope)">{{ dataScopeLabel(record.dataScope) }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="状态" :width="80">
            <template #cell="{ record }">
              <a-tag :color="record.status === 1 ? 'green' : 'red'">{{ record.status === 1 ? '启用' : '禁用' }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" data-index="createdAt" :width="170" />
          <a-table-column title="操作" :width="280" fixed="right">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" @click="openEditModal(record)" v-if="hasPerm('system:role:edit')">编辑</a-button>
                <a-button type="text" size="small" @click="openPermModal(record)" v-if="hasPerm('system:role:assign')">分配权限</a-button>
                <a-button type="text" size="small" status="warning" @click="openDataScopeModal(record)" v-if="hasPerm('system:role:dataScope')">数据范围</a-button>
                <a-popconfirm content="确定删除该角色？" @ok="handleDelete(record)">
                  <a-button type="text" size="small" status="danger" v-if="hasPerm('system:role:delete')">删除</a-button>
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
        show-total show-page-size
        @change="onPageChange"
        @page-size-change="onPageSizeChange"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:visible="formVisible" :title="isEdit ? '编辑角色' : '新增角色'" @ok="handleSubmit" :ok-loading="submitLoading">
      <a-form :model="formData" layout="vertical">
        <a-form-item label="角色编码" required>
          <a-input v-model="formData.roleCode" placeholder="如: ENGINEER" :disabled="isEdit" />
        </a-form-item>
        <a-form-item label="角色名称" required>
          <a-input v-model="formData.roleName" placeholder="如: 运维工程师" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model="formData.description" placeholder="角色描述" :max-length="200" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 分配权限弹窗 -->
    <a-modal v-model:visible="permVisible" title="分配权限" @ok="handleAssignPerms" :ok-loading="permLoading" width="560px">
      <div class="perm-tree-wrapper">
        <a-tree
          v-model:checked-keys="selectedPermIds"
          :data="permTree"
          :field-names="{ key: 'id', title: 'permName', children: 'children' }"
          checkable
          check-strictly
          default-expand-all
        />
      </div>
    </a-modal>

    <!-- 数据范围配置弹窗 -->
    <a-modal v-model:visible="dataScopeVisible" title="配置数据范围" @ok="handleSaveDataScope" :ok-loading="dataScopeLoading" width="420px">
      <a-form layout="vertical">
        <a-form-item label="数据范围">
          <a-select v-model="dataScopeValue" placeholder="请选择数据范围">
            <a-option :value="1">全部数据</a-option>
            <a-option :value="2">本部门数据</a-option>
            <a-option :value="3">本部门及下级数据</a-option>
            <a-option :value="4">仅本人数据</a-option>
          </a-select>
        </a-form-item>
        <div class="data-scope-desc">
          <p v-if="dataScopeValue === 1">可查看系统中所有数据，不受部门限制。</p>
          <p v-else-if="dataScopeValue === 2">仅可查看本人所在部门的数据。</p>
          <p v-else-if="dataScopeValue === 3">可查看本人所在部门及其所有下级部门的数据。</p>
          <p v-else-if="dataScopeValue === 4">仅可查看本人创建/关联的数据。</p>
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRolePage, createRole, updateRole, deleteRole, assignRolePermissions, getRolePermissionIds, updateRoleDataScope, getRoleDataScope } from '@/api/role'
import { getPermissionTree } from '@/api/permission'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (code) => userStore.permissions.length === 0 || userStore.hasPermission(code)

const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 10, total: 0 })
const searchForm = ref({ roleName: '', status: undefined })

const formVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formData = ref({})

const permVisible = ref(false)
const permLoading = ref(false)
const selectedPermIds = ref([])
const permTree = ref([])
const currentRoleId = ref(null)

const dataScopeVisible = ref(false)
const dataScopeLoading = ref(false)
const dataScopeValue = ref(1)
const dataScopeRoleId = ref(null)

const dataScopeLabels = { 1: '全部数据', 2: '本部门数据', 3: '本部门及下级', 4: '仅本人数据', 5: '自定义' }
const dataScopeColors = { 1: 'blue', 2: 'green', 3: 'cyan', 4: 'orange', 5: 'purple' }
const dataScopeLabel = (v) => dataScopeLabels[v] || '全部数据'
const dataScopeColor = (v) => dataScopeColors[v] || 'blue'

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRolePage({
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
  searchForm.value = { roleName: '', status: undefined }
  pagination.value.current = 1
  fetchData()
}

const onPageChange = (page) => { pagination.value.current = page; fetchData() }
const onPageSizeChange = (size) => { pagination.value.size = size; pagination.value.current = 1; fetchData() }

const openCreateModal = () => {
  isEdit.value = false
  formData.value = { roleCode: '', roleName: '', description: '', sortOrder: 0 }
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
      await updateRole(formData.value.id, formData.value)
    } else {
      await createRole(formData.value)
    }
    Message.success(isEdit.value ? '更新成功' : '创建成功')
    formVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (record) => {
  await deleteRole(record.id)
  Message.success('删除成功')
  fetchData()
}

// 本地权限树（与 SQL 初始数据一致）
const fallbackPermTree = [
  { id: 1, permName: '数据仪表盘', permCode: 'dashboard', permType: 'MENU', children: [] },
  { id: 30, permName: '实时监控', permCode: 'monitor', permType: 'MENU', children: [] },
  { id: 31, permName: '故障分析', permCode: 'fault', permType: 'MENU', children: [
    { id: 25, permName: '故障处理', permCode: 'fault:handle', permType: 'BUTTON', children: [] }
  ]},
  { id: 2, permName: '系统管理', permCode: 'system', permType: 'MENU', children: [
    { id: 3, permName: '用户管理', permCode: 'system:user', permType: 'MENU', children: [
      { id: 10, permName: '新增用户', permCode: 'system:user:add', permType: 'BUTTON', children: [] },
      { id: 11, permName: '编辑用户', permCode: 'system:user:edit', permType: 'BUTTON', children: [] },
      { id: 12, permName: '删除用户', permCode: 'system:user:delete', permType: 'BUTTON', children: [] },
      { id: 13, permName: '分配角色', permCode: 'system:user:assign', permType: 'BUTTON', children: [] },
      { id: 21, permName: '启用禁用', permCode: 'system:user:status', permType: 'BUTTON', children: [] },
      { id: 22, permName: '重置密码', permCode: 'system:user:resetPwd', permType: 'BUTTON', children: [] },
      { id: 23, permName: '解锁用户', permCode: 'system:user:unlock', permType: 'BUTTON', children: [] }
    ]},
    { id: 4, permName: '角色管理', permCode: 'system:role', permType: 'MENU', children: [
      { id: 14, permName: '新增角色', permCode: 'system:role:add', permType: 'BUTTON', children: [] },
      { id: 15, permName: '编辑角色', permCode: 'system:role:edit', permType: 'BUTTON', children: [] },
      { id: 16, permName: '删除角色', permCode: 'system:role:delete', permType: 'BUTTON', children: [] },
      { id: 17, permName: '分配权限', permCode: 'system:role:assign', permType: 'BUTTON', children: [] },
      { id: 24, permName: '数据范围', permCode: 'system:role:dataScope', permType: 'BUTTON', children: [] }
    ]},
    { id: 5, permName: '权限管理', permCode: 'system:permission', permType: 'MENU', children: [
      { id: 18, permName: '新增权限', permCode: 'system:perm:add', permType: 'BUTTON', children: [] },
      { id: 19, permName: '编辑权限', permCode: 'system:perm:edit', permType: 'BUTTON', children: [] },
      { id: 20, permName: '删除权限', permCode: 'system:perm:delete', permType: 'BUTTON', children: [] }
    ]},
    { id: 6, permName: '操作日志', permCode: 'system:log', permType: 'MENU', children: [] },
    { id: 32, permName: '系统配置', permCode: 'system:config', permType: 'MENU', children: [
      { id: 26, permName: '编辑配置', permCode: 'system:config:edit', permType: 'BUTTON', children: [] }
    ]}
  ]}
]

// 各角色默认权限ID — check-strictly 模式，每个节点独立勾选
const fallbackRolePerms = {
  // 管理员：全部权限
  1: [1, 2, 3, 4, 5, 6, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 30, 31, 32],
  // 运维工程师：仪表盘 + 实时监控 + 故障分析+故障处理 + 系统管理+系统配置+编辑配置 + 操作日志
  2: [1, 30, 31, 25, 2, 32, 26, 6],
  // 数据分析师：仪表盘 + 实时监控 + 故障分析(只读) + 系统管理+操作日志
  3: [1, 30, 31, 2, 6],
  // 普通查看用户：仪表盘 + 实时监控 + 故障分析(只读)
  4: [1, 30, 31]
}

const openPermModal = async (record) => {
  currentRoleId.value = record.id
  if (permTree.value.length === 0) {
    try {
      const res = await getPermissionTree()
      permTree.value = res.data
    } catch (e) {
      permTree.value = fallbackPermTree
    }
  }
  try {
    const res = await getRolePermissionIds(record.id)
    selectedPermIds.value = res.data || []
  } catch (e) {
    selectedPermIds.value = fallbackRolePerms[record.id] || []
  }
  permVisible.value = true
}

const handleAssignPerms = async () => {
  permLoading.value = true
  try {
    await assignRolePermissions(currentRoleId.value, selectedPermIds.value)
    Message.success('权限分配成功')
    permVisible.value = false
  } finally {
    permLoading.value = false
  }
}

const openDataScopeModal = async (record) => {
  dataScopeRoleId.value = record.id
  dataScopeValue.value = record.dataScope || 1
  try {
    const res = await getRoleDataScope(record.id)
    dataScopeValue.value = res.data || 1
  } catch (e) {
    // use default
  }
  dataScopeVisible.value = true
}

const handleSaveDataScope = async () => {
  dataScopeLoading.value = true
  try {
    await updateRoleDataScope(dataScopeRoleId.value, dataScopeValue.value)
    Message.success('数据范围配置成功')
    dataScopeVisible.value = false
    fetchData()
  } finally {
    dataScopeLoading.value = false
  }
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.role-page {
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
  .data-scope-desc {
    padding: 12px;
    background: rgba(22, 93, 255, 0.06);
    border-radius: 6px;
    p {
      margin: 0;
      color: #86909C;
      font-size: 13px;
    }
  }
  .perm-tree-wrapper {
    max-height: 420px;
    overflow-y: auto;
    padding: 8px 0;
    :deep(.arco-tree-node) {
      padding: 3px 0;
    }
  }
}
</style>
