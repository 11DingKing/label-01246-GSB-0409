<template>
  <div class="log-page">
    <div class="page-card">
      <a-tabs v-model:active-key="activeTab" @change="onTabChange">
        <a-tab-pane key="operation" title="操作日志">
          <div class="search-bar">
            <a-input v-model="opSearch.username" placeholder="操作用户" style="width: 160px" allow-clear />
            <a-input v-model="opSearch.module" placeholder="操作模块" style="width: 160px" allow-clear />
            <a-button type="primary" @click="fetchOperationLog"><icon-search /> 搜索</a-button>
            <a-button @click="resetOpSearch">重置</a-button>
          </div>
          <a-table :data="opData" :loading="opLoading" :pagination="false" row-key="id" stripe>
            <template #columns>
              <a-table-column title="ID" data-index="id" :width="60" />
              <a-table-column title="操作用户" data-index="username" :width="100" />
              <a-table-column title="模块" data-index="module" :width="100" />
              <a-table-column title="操作" data-index="operation" :width="120" />
              <a-table-column title="请求方法" data-index="method" :width="280" ellipsis />
              <a-table-column title="IP" data-index="ip" :width="130" />
              <a-table-column title="耗时(ms)" data-index="duration" :width="90" />
              <a-table-column title="状态" :width="80">
                <template #cell="{ record }">
                  <a-tag :color="record.status === 1 ? 'green' : 'red'">{{ record.status === 1 ? '成功' : '失败' }}</a-tag>
                </template>
              </a-table-column>
              <a-table-column title="时间" data-index="createdAt" :width="170" />
            </template>
          </a-table>
          <a-pagination
            :total="opPagination.total" :current="opPagination.current" :page-size="opPagination.size"
            show-total show-page-size
            @change="(p) => { opPagination.current = p; fetchOperationLog() }"
            @page-size-change="(s) => { opPagination.size = s; opPagination.current = 1; fetchOperationLog() }"
            style="margin-top: 16px; justify-content: flex-end;"
          />
        </a-tab-pane>

        <a-tab-pane key="login" title="登录日志">
          <div class="search-bar">
            <a-input v-model="loginSearch.username" placeholder="用户名" style="width: 160px" allow-clear />
            <a-select v-model="loginSearch.status" placeholder="状态" style="width: 140px" allow-clear>
              <a-option :value="0">失败</a-option>
              <a-option :value="1">成功</a-option>
            </a-select>
            <a-button type="primary" @click="fetchLoginLog"><icon-search /> 搜索</a-button>
            <a-button @click="resetLoginSearch">重置</a-button>
          </div>
          <a-table :data="loginData" :loading="loginLoading" :pagination="false" row-key="id" stripe>
            <template #columns>
              <a-table-column title="ID" data-index="id" :width="60" />
              <a-table-column title="用户名" data-index="username" :width="120" />
              <a-table-column title="IP" data-index="ip" :width="130" />
              <a-table-column title="类型" :width="80">
                <template #cell="{ record }">
                  <a-tag :color="record.loginType === 1 ? 'blue' : 'orange'">{{ record.loginType === 1 ? '登录' : '登出' }}</a-tag>
                </template>
              </a-table-column>
              <a-table-column title="状态" :width="80">
                <template #cell="{ record }">
                  <a-tag :color="record.status === 1 ? 'green' : 'red'">{{ record.status === 1 ? '成功' : '失败' }}</a-tag>
                </template>
              </a-table-column>
              <a-table-column title="消息" data-index="message" :width="200" ellipsis />
              <a-table-column title="时间" data-index="createdAt" :width="170" />
            </template>
          </a-table>
          <a-pagination
            :total="loginPagination.total" :current="loginPagination.current" :page-size="loginPagination.size"
            show-total show-page-size
            @change="(p) => { loginPagination.current = p; fetchLoginLog() }"
            @page-size-change="(s) => { loginPagination.size = s; loginPagination.current = 1; fetchLoginLog() }"
            style="margin-top: 16px; justify-content: flex-end;"
          />
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOperationLogPage, getLoginLogPage } from '@/api/log'

const activeTab = ref('operation')

// 操作日志
const opLoading = ref(false)
const opData = ref([])
const opPagination = ref({ current: 1, size: 10, total: 0 })
const opSearch = ref({ username: '', module: '' })

const fetchOperationLog = async () => {
  opLoading.value = true
  try {
    const res = await getOperationLogPage({
      current: opPagination.value.current,
      size: opPagination.value.size,
      ...opSearch.value
    })
    opData.value = res.data.records
    opPagination.value.total = res.data.total
  } finally {
    opLoading.value = false
  }
}

const resetOpSearch = () => {
  opSearch.value = { username: '', module: '' }
  opPagination.value.current = 1
  fetchOperationLog()
}

// 登录日志
const loginLoading = ref(false)
const loginData = ref([])
const loginPagination = ref({ current: 1, size: 10, total: 0 })
const loginSearch = ref({ username: '', status: undefined })

const fetchLoginLog = async () => {
  loginLoading.value = true
  try {
    const res = await getLoginLogPage({
      current: loginPagination.value.current,
      size: loginPagination.value.size,
      ...loginSearch.value
    })
    loginData.value = res.data.records
    loginPagination.value.total = res.data.total
  } finally {
    loginLoading.value = false
  }
}

const resetLoginSearch = () => {
  loginSearch.value = { username: '', status: undefined }
  loginPagination.value.current = 1
  fetchLoginLog()
}

const onTabChange = (key) => {
  if (key === 'operation') fetchOperationLog()
  else fetchLoginLog()
}

onMounted(fetchOperationLog)
</script>

<style lang="scss" scoped>
.log-page {
  .page-card {
    background: $bg-card;
    border-radius: $border-radius-card;
    padding: $spacing-lg;
    box-shadow: $shadow-card;
    border: 1px solid $border-color;
  }
  :deep(.arco-tabs-tab) {
    color: $text-secondary;
    transition: color 0.2s ease;
    &:hover {
      color: $text-primary;
    }
    &.arco-tabs-tab-active {
      color: $primary-color;
    }
  }
}
</style>
