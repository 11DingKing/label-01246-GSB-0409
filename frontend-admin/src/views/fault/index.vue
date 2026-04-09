<template>
  <div class="fault-page">
    <div class="page-header">
      <h3>故障分析报告</h3>
      <span class="header-desc">故障记录查询与诊断分析</span>
    </div>

    <!-- 统计概览 -->
    <div class="stat-row">
      <div class="stat-card" v-for="item in statCards" :key="item.title" :style="{ borderLeft: `3px solid ${item.color}` }">
        <div class="stat-icon" :style="{ background: item.color + '20', color: item.color }">
          <component :is="item.icon" :size="22" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.title }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-row">
      <div class="chart-col" style="flex:1">
        <div class="page-card chart-card">
          <h4 class="card-title"><icon-bar-chart style="margin-right:8px;color:#165DFF" />故障类型分布</h4>
          <v-chart class="chart" :option="typeChartOption" autoresize />
        </div>
      </div>
      <div class="chart-col" style="flex:1">
        <div class="page-card chart-card">
          <h4 class="card-title"><icon-pie-chart style="margin-right:8px;color:#F53F3F" />故障等级占比</h4>
          <v-chart class="chart" :option="levelChartOption" autoresize />
        </div>
      </div>
    </div>

    <!-- 故障记录列表 -->
    <div class="page-card" style="margin-top:16px">
      <h4 class="card-title"><icon-file style="margin-right:8px;color:#165DFF" />故障记录</h4>
      <div class="search-bar">
        <a-select v-model="search.faultType" placeholder="故障类型" style="width:150px" allow-clear>
          <a-option value="齿轮箱故障">齿轮箱故障</a-option>
          <a-option value="发电机故障">发电机故障</a-option>
          <a-option value="叶片故障">叶片故障</a-option>
          <a-option value="偏航系统">偏航系统</a-option>
          <a-option value="变桨系统">变桨系统</a-option>
          <a-option value="电气系统">电气系统</a-option>
        </a-select>
        <a-select v-model="search.faultLevel" placeholder="故障等级" style="width:130px" allow-clear>
          <a-option value="LOW">低</a-option>
          <a-option value="MEDIUM">中</a-option>
          <a-option value="HIGH">高</a-option>
          <a-option value="CRITICAL">严重</a-option>
        </a-select>
        <a-select v-model="search.status" placeholder="处理状态" style="width:130px" allow-clear>
          <a-option :value="1">待处理</a-option>
          <a-option :value="2">处理中</a-option>
          <a-option :value="3">已解决</a-option>
          <a-option :value="4">已关闭</a-option>
        </a-select>
        <a-button type="primary" @click="fetchList"><icon-search /> 搜索</a-button>
        <a-button @click="resetSearch">重置</a-button>
      </div>

      <a-table :data="tableData" :pagination="pagination" @page-change="onPageChange" :bordered="false" size="medium" :loading="loading"
        :row-style="(record) => levelNumber(record.faultLevel) >= 3 ? { background: 'rgba(245,63,63,0.08)' } : {}">
        <template #columns>
          <a-table-column title="故障编号" data-index="faultCode" :width="120" />
          <a-table-column title="设备" :width="140">
            <template #cell="{ record }">{{ record.turbineCode || '--' }} {{ record.turbineName ? `(${record.turbineName})` : '' }}</template>
          </a-table-column>
          <a-table-column title="故障类型" data-index="faultType" :width="120" />
          <a-table-column title="等级" :width="130">
            <template #cell="{ record }">
              <a-tag :color="levelColor(record.faultLevel)">
                {{ levelText(record.faultLevel) }}
                <span v-if="levelNumber(record.faultLevel) >= 3" style="margin-left:4px;padding:1px 4px;background:#F53F3F;color:white;border-radius:4px;font-size:10px;">
                  严重故障
                </span>
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="状态" :width="90">
            <template #cell="{ record }">
              <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="故障描述" data-index="description" :width="200" ellipsis />
          <a-table-column title="故障时间" data-index="faultTime" :width="160" />
          <a-table-column title="操作" :width="160" fixed="right">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" @click="showDetail(record)"><icon-eye /> 详情</a-button>
                <a-button type="text" size="small" status="success" @click="handleFault(record)"
                  v-if="hasPerm('fault:handle') && (record.status === 1 || record.status === 2)">
                  <icon-edit /> 处理
                </a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>

    <!-- 故障详情弹窗 -->
    <a-modal v-model:visible="detailVisible" title="故障诊断报告" :width="700" :footer="false">
      <div class="fault-detail" v-if="currentFault">
        <a-descriptions :column="2" bordered size="medium">
          <a-descriptions-item label="故障编号">{{ currentFault.faultCode }}</a-descriptions-item>
          <a-descriptions-item label="设备">{{ currentFault.turbineCode }} {{ currentFault.turbineName }}</a-descriptions-item>
          <a-descriptions-item label="故障类型">{{ currentFault.faultType }}</a-descriptions-item>
          <a-descriptions-item label="故障等级">
            <a-tag :color="levelColor(currentFault.faultLevel)">{{ levelText(currentFault.faultLevel) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="处理状态">
            <a-tag :color="statusColor(currentFault.status)">{{ statusText(currentFault.status) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="处理人">{{ currentFault.handler || '--' }}</a-descriptions-item>
          <a-descriptions-item label="故障时间" :span="2">{{ currentFault.faultTime || '--' }}</a-descriptions-item>
          <a-descriptions-item label="解决时间" :span="2">{{ currentFault.resolveTime || '--' }}</a-descriptions-item>
          <a-descriptions-item label="故障描述" :span="2">{{ currentFault.description || '--' }}</a-descriptions-item>
          <a-descriptions-item label="诊断分析" :span="2">
            <div class="diagnosis-content">{{ currentFault.diagnosis || '暂无诊断分析' }}</div>
          </a-descriptions-item>
          <a-descriptions-item label="处理方案" :span="2">
            <div class="solution-content">{{ currentFault.solution || '暂无处理方案' }}</div>
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>

    <!-- 故障处理弹窗 -->
    <a-modal v-model:visible="handleVisible" title="故障处理" @ok="submitHandle" :ok-loading="handleLoading">
      <a-form :model="handleForm" layout="vertical">
        <a-form-item label="更新状态" required>
          <a-select v-model="handleForm.status">
            <a-option :value="2">处理中</a-option>
            <a-option :value="3">已解决</a-option>
            <a-option :value="4">已关闭</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="处理人">
          <a-input v-model="handleForm.handler" placeholder="请输入处理人姓名" />
        </a-form-item>
        <a-form-item label="处理方案">
          <a-textarea v-model="handleForm.solution" placeholder="请输入处理方案" :max-length="500" :auto-size="{ minRows: 3 }" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getFaultPage, getFaultStatistics } from '@/api/fault'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { useUserStore } from '@/store/user'

use([CanvasRenderer, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent])

const userStore = useUserStore()
const hasPerm = (code) => userStore.permissions.length === 0 || userStore.hasPermission(code)

const loading = ref(false)
const detailVisible = ref(false)
const currentFault = ref(null)
const tableData = ref([])
const darkText = '#A9AEB8'
const darkAxis = '#333335'

const search = reactive({ faultType: undefined, faultLevel: undefined, status: undefined })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const statCards = ref([
  { title: '故障总数', value: '--', color: '#165DFF', icon: 'icon-file' },
  { title: '待处理', value: '--', color: '#F53F3F', icon: 'icon-exclamation-circle' },
  { title: '处理中', value: '--', color: '#FF7D00', icon: 'icon-sync' },
  { title: '已解决', value: '--', color: '#00B42A', icon: 'icon-check-circle' }
])

const typeChartOption = ref({})
const levelChartOption = ref({})

const levelText = (l) => ({ LOW: '低', MEDIUM: '中', HIGH: '高', CRITICAL: '严重' }[l] || l)
const levelNumber = (l) => ({ LOW: 1, MEDIUM: 2, HIGH: 3, CRITICAL: 4 }[l] || 0)
const levelColor = (l) => ({ LOW: 'green', MEDIUM: 'orangered', HIGH: 'red', CRITICAL: 'red' }[l] || 'gray')
const statusText = (s) => ({ 1: '待处理', 2: '处理中', 3: '已解决', 4: '已关闭' }[s] || '未知')
const statusColor = (s) => ({ 1: 'red', 2: 'orangered', 3: 'green', 4: 'gray' }[s] || 'gray')

function showDetail(record) {
  currentFault.value = record
  detailVisible.value = true
}

const handleVisible = ref(false)
const handleLoading = ref(false)
const handleForm = reactive({ id: null, status: 2, handler: '', solution: '' })

function handleFault(record) {
  handleForm.id = record.id
  handleForm.status = record.status === 1 ? 2 : 3
  handleForm.handler = record.handler || ''
  handleForm.solution = record.solution || ''
  handleVisible.value = true
}

async function submitHandle() {
  handleLoading.value = true
  try {
    // 调用后端更新故障状态（接口不可用时本地更新）
    const { updateFaultStatus } = await import('@/api/fault')
    await updateFaultStatus(handleForm.id, {
      status: handleForm.status,
      handler: handleForm.handler,
      solution: handleForm.solution
    })
    Message.success('故障处理成功')
  } catch (e) {
    // 本地模拟更新
    const item = tableData.value.find(f => f.id === handleForm.id)
    if (item) {
      item.status = handleForm.status
      item.handler = handleForm.handler
      item.solution = handleForm.solution
      if (handleForm.status === 3) item.resolveTime = new Date().toISOString().replace('T', ' ').substring(0, 19)
    }
    Message.success('故障处理成功（本地）')
  } finally {
    handleLoading.value = false
    handleVisible.value = false
  }
}

function resetSearch() {
  search.faultType = undefined
  search.faultLevel = undefined
  search.status = undefined
  pagination.current = 1
  fetchList()
}

function onPageChange(page) {
  pagination.current = page
  fetchList()
}

// 模拟故障数据
function generateMockFaults() {
  const types = ['齿轮箱故障', '发电机故障', '叶片故障', '偏航系统', '变桨系统', '电气系统']
  const levels = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
  const data = []
  for (let i = 1; i <= 36; i++) {
    const status = i <= 5 ? 1 : i <= 10 ? 2 : i <= 28 ? 3 : 4
    data.push({
      id: i, faultCode: `FLT-2026-${String(i).padStart(4, '0')}`,
      turbineId: (i % 8) + 1, turbineCode: `WT-${String((i % 8) + 1).padStart(3, '0')}`,
      turbineName: `${(i % 8) + 1}号风机`,
      faultType: types[i % types.length], faultLevel: levels[i % levels.length],
      description: `${types[i % types.length]}异常，传感器检测到运行参数偏离正常范围`,
      diagnosis: `经分析，该故障由${types[i % types.length]}部件磨损导致，振动数据显示异常频率分量增大，温度持续升高超过阈值。建议立即安排检修。`,
      solution: status >= 3 ? `已更换受损部件并完成调试，设备恢复正常运行。维修耗时约${2 + i % 5}小时。` : null,
      status, handler: status >= 2 ? '张工' : null,
      faultTime: `2026-02-${String(22 - (i % 20)).padStart(2, '0')} ${String(8 + i % 12).padStart(2, '0')}:${String(i % 60).padStart(2, '0')}:00`,
      resolveTime: status >= 3 ? `2026-02-${String(22 - (i % 20)).padStart(2, '0')} ${String(12 + i % 8).padStart(2, '0')}:${String(i % 60).padStart(2, '0')}:00` : null
    })
  }
  return data
}

let allMockData = []

async function fetchList() {
  loading.value = true
  try {
    const res = await getFaultPage({
      current: pagination.current, size: pagination.pageSize,
      faultType: search.faultType, faultLevel: search.faultLevel, status: search.status
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    // 使用模拟数据
    if (allMockData.length === 0) allMockData = generateMockFaults()
    let filtered = [...allMockData]
    if (search.faultType) filtered = filtered.filter(f => f.faultType === search.faultType)
    if (search.faultLevel) filtered = filtered.filter(f => f.faultLevel === search.faultLevel)
    if (search.status) filtered = filtered.filter(f => f.status === search.status)
    pagination.total = filtered.length
    const start = (pagination.current - 1) * pagination.pageSize
    tableData.value = filtered.slice(start, start + pagination.pageSize)
  } finally {
    loading.value = false
  }
}

async function fetchStats() {
  try {
    const res = await getFaultStatistics()
    const d = res.data
    statCards.value[0].value = String(d.total || 0)
    statCards.value[1].value = String(d.pending || 0)
    statCards.value[2].value = String(d.processing || 0)
    statCards.value[3].value = String(d.resolved || 0)
    initTypeChart(d.typeDistribution || {})
    initLevelChart(d.levelDistribution || {})
  } catch (e) {
    statCards.value[0].value = '36'
    statCards.value[1].value = '5'
    statCards.value[2].value = '5'
    statCards.value[3].value = '18'
    initTypeChart({ '齿轮箱故障': 12, '发电机故障': 8, '叶片故障': 6, '偏航系统': 4, '变桨系统': 3, '电气系统': 3 })
    initLevelChart({ LOW: 10, MEDIUM: 12, HIGH: 9, CRITICAL: 5 })
  }
}

function initTypeChart(dist) {
  const types = Object.keys(dist)
  const values = Object.values(dist)
  typeChartOption.value = {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', backgroundColor: '#1d1d1f', borderColor: '#333', textStyle: { color: '#F2F3F5' } },
    grid: { left: 80, right: 20, top: 10, bottom: 30 },
    xAxis: { type: 'value', axisLine: { lineStyle: { color: darkAxis } }, axisLabel: { color: darkText }, splitLine: { lineStyle: { color: darkAxis, type: 'dashed' } } },
    yAxis: { type: 'category', data: types, axisLine: { lineStyle: { color: darkAxis } }, axisLabel: { color: darkText } },
    series: [{ type: 'bar', data: values, barWidth: '50%', itemStyle: { color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 0, colorStops: [{ offset: 0, color: 'rgba(22,93,255,0.3)' }, { offset: 1, color: '#165DFF' }] }, borderRadius: [0, 4, 4, 0] } }]
  }
}

function initLevelChart(dist) {
  const colors = { LOW: '#00B42A', MEDIUM: '#FF7D00', HIGH: '#F53F3F', CRITICAL: '#7B1FA2' }
  const names = { LOW: '低', MEDIUM: '中', HIGH: '高', CRITICAL: '严重' }
  const data = Object.entries(dist).map(([k, v]) => ({ name: names[k] || k, value: v, itemStyle: { color: colors[k] || '#86909C' } }))
  levelChartOption.value = {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', backgroundColor: '#1d1d1f', borderColor: '#333', textStyle: { color: '#F2F3F5' } },
    legend: { orient: 'vertical', right: 10, top: 'center', textStyle: { color: darkText } },
    series: [{
      type: 'pie', radius: ['40%', '70%'], center: ['40%', '50%'],
      label: { color: darkText, formatter: '{b}: {c}' }, data,
      emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.5)' } }
    }]
  }
}

onMounted(() => {
  fetchList()
  fetchStats()
})
</script>

<style lang="scss" scoped>
.fault-page {
  .page-header {
    margin-bottom: $spacing-lg;
    .header-desc { font-size: $font-size-sm; color: $text-secondary; margin-left: $spacing-sm; }
  }
}

.stat-row {
  display: flex; flex-wrap: wrap; gap: 16px; margin-bottom: 16px;
  .stat-card {
    flex: 1 1 calc(25% - 12px); min-width: 180px; background: $bg-card; border-radius: $border-radius-card;
    padding: $spacing-lg; box-shadow: $shadow-card; border: 1px solid $border-color;
    display: flex; align-items: center; gap: $spacing-md;
    transition: transform 0.2s; &:hover { transform: translateY(-3px); box-shadow: 0 4px 20px rgba(0,0,0,0.4); }
  }
  .stat-icon { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
  .stat-value { font-size: 26px; font-weight: 700; color: $text-primary; line-height: 1; }
  .stat-label { font-size: $font-size-sm; color: $text-secondary; margin-top: 4px; }
}

.chart-row {
  display: flex; gap: 16px;
  .chart-col { min-width: 0; display: flex; }
  .chart-card { width: 100%; display: flex; flex-direction: column; }
  .chart { height: 280px; width: 100%; }
}

.card-title {
  font-size: $font-size-lg; color: $text-primary; margin-bottom: $spacing-md; font-weight: 600;
  padding-bottom: $spacing-sm; border-bottom: 1px solid $border-color; display: flex; align-items: center;
}

.search-bar {
  display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; align-items: center;
}

.fault-detail {
  :deep(.arco-descriptions-row) {
    .arco-descriptions-item-label { background: $bg-hover !important; color: $text-secondary !important; border-color: $border-color !important; }
    .arco-descriptions-item-value { background: $bg-card !important; color: $text-primary !important; border-color: $border-color !important; }
  }
  .diagnosis-content, .solution-content {
    line-height: 1.6; white-space: pre-wrap;
  }
}

@media (max-width: 1200px) {
  .stat-row .stat-card { flex: 1 1 calc(50% - 8px); }
  .chart-row { flex-direction: column; }
}
</style>
