<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h3>数据可视化仪表盘</h3>
      <span class="header-desc">风力发电机运行状态概览与数据分析</span>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-row">
      <div class="stat-card" v-for="item in statCards" :key="item.title" :style="{ borderLeft: `3px solid ${item.color}` }">
        <div class="stat-icon" :style="{ background: item.color + '20', color: item.color }">
          <component :is="item.icon" :size="24" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.title }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-row">
      <div class="chart-col chart-col-left">
        <div class="page-card chart-card">
          <h4 class="card-title"><icon-line-chart style="margin-right:8px;color:#165DFF" />风速与功率趋势</h4>
          <v-chart class="chart" :option="trendOption" autoresize />
        </div>
      </div>
      <div class="chart-col chart-col-right">
        <div class="page-card chart-card">
          <h4 class="card-title"><icon-pie-chart style="margin-right:8px;color:#165DFF" />设备状态分布</h4>
          <v-chart class="chart" :option="pieOption" autoresize />
        </div>
      </div>
    </div>

    <!-- 第二行图表 -->
    <div class="chart-row">
      <div class="chart-col chart-col-left">
        <div class="page-card chart-card">
          <h4 class="card-title"><icon-bar-chart style="margin-right:8px;color:#165DFF" />故障类型统计</h4>
          <v-chart class="chart" :option="faultBarOption" autoresize />
        </div>
      </div>
      <div class="chart-col chart-col-right">
        <div class="page-card chart-card">
          <h4 class="card-title"><icon-thunderbolt style="margin-right:8px;color:#FF7D00" />故障等级分布</h4>
          <v-chart class="chart" :option="faultLevelOption" autoresize />
        </div>
      </div>
    </div>

    <!-- 系统信息 + 快捷操作 -->
    <div class="info-row">
      <div class="info-col">
        <div class="page-card info-card">
          <h4 class="card-title"><icon-info-circle style="margin-right:8px;color:#165DFF" />系统信息</h4>
          <a-descriptions :column="1" size="large" bordered>
            <a-descriptions-item label="系统名称">风力发电机故障诊断系统</a-descriptions-item>
            <a-descriptions-item label="系统版本">v1.0.0</a-descriptions-item>
            <a-descriptions-item label="后端框架">Solon 3.7.3 + JDK 21</a-descriptions-item>
            <a-descriptions-item label="前端框架">Vue 3 + Arco Design Pro</a-descriptions-item>
            <a-descriptions-item label="数据库">MySQL 8.0</a-descriptions-item>
            <a-descriptions-item label="运行环境">Docker + Nginx</a-descriptions-item>
          </a-descriptions>
        </div>
      </div>
      <div class="info-col">
        <div class="page-card info-card">
          <h4 class="card-title"><icon-apps style="margin-right:8px;color:#165DFF" />快捷操作</h4>
          <div class="quick-actions">
            <div class="quick-action-item" v-for="item in quickActions" :key="item.path" @click="$router.push(item.path)">
              <div class="action-icon" :style="{ background: item.bg, color: item.color }">
                <component :is="item.icon" :size="22" />
              </div>
              <div class="action-text">
                <div class="action-title">{{ item.title }}</div>
                <div class="action-desc">{{ item.desc }}</div>
              </div>
            </div>
            <div v-if="quickActions.length === 0" style="color:#A9AEB8;text-align:center;padding:24px 0;">暂无可用的快捷操作</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getDashboardStats } from '@/api/turbine'
import { getFaultStatistics } from '@/api/fault'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, PieChart, BarChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])

const userStore = useUserStore()

const statCards = ref([
  { title: '设备总数', value: '--', color: '#165DFF', icon: 'icon-thunderbolt' },
  { title: '正常运行', value: '--', color: '#00B42A', icon: 'icon-check-circle' },
  { title: '告警设备', value: '--', color: '#FF7D00', icon: 'icon-exclamation-circle' },
  { title: '故障设备', value: '--', color: '#F53F3F', icon: 'icon-close-circle' },
  { title: '总装机容量', value: '--', color: '#722ED1', icon: 'icon-bulb' }
])

const trendOption = ref({})
const pieOption = ref({})
const faultBarOption = ref({})
const faultLevelOption = ref({})

const darkTextColor = '#A9AEB8'
const darkAxisColor = '#333335'

// 快捷操作
const actionConfig = {
  '/monitor': { desc: '查看设备实时运行数据', color: '#165DFF', bg: 'rgba(22,93,255,0.12)' },
  '/fault': { desc: '查看故障分析报告', color: '#F53F3F', bg: 'rgba(245,63,63,0.12)' },
  '/system/config': { desc: '系统参数配置管理', color: '#FF7D00', bg: 'rgba(255,125,0,0.12)' },
  '/system/user': { desc: '管理系统用户账号', color: '#00B42A', bg: 'rgba(0,180,42,0.12)' },
  '/system/role': { desc: '配置角色与权限', color: '#722ED1', bg: 'rgba(114,46,209,0.12)' },
  '/system/log': { desc: '查看系统操作记录', color: '#EB2F96', bg: 'rgba(235,47,150,0.12)' }
}

const quickActions = computed(() => {
  const actions = []
  const extract = (menus) => {
    for (const menu of menus) {
      if (menu.children && menu.children.length) extract(menu.children)
      else if (menu.path && menu.path !== '/dashboard' && actionConfig[menu.path]) {
        const cfg = actionConfig[menu.path]
        actions.push({ path: menu.path, title: menu.permName, icon: menu.icon, desc: cfg.desc, color: cfg.color, bg: cfg.bg })
      }
    }
  }
  extract(userStore.menus || [])
  return actions
})

const initTrendChart = (trendData) => {
  const times = trendData.map(d => {
    const t = d.time
    return typeof t === 'string' ? t.substring(11, 16) : ''
  })
  trendOption.value = {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', backgroundColor: '#1d1d1f', borderColor: '#333', textStyle: { color: '#F2F3F5' } },
    legend: { data: ['风速(m/s)', '功率(kW)'], textStyle: { color: darkTextColor }, top: 0 },
    grid: { left: 50, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: times, axisLine: { lineStyle: { color: darkAxisColor } }, axisLabel: { color: darkTextColor } },
    yAxis: [
      { type: 'value', name: '风速(m/s)', nameTextStyle: { color: darkTextColor }, axisLine: { lineStyle: { color: darkAxisColor } }, axisLabel: { color: darkTextColor }, splitLine: { lineStyle: { color: darkAxisColor, type: 'dashed' } } },
      { type: 'value', name: '功率(kW)', nameTextStyle: { color: darkTextColor }, axisLine: { lineStyle: { color: darkAxisColor } }, axisLabel: { color: darkTextColor }, splitLine: { show: false } }
    ],
    series: [
      { name: '风速(m/s)', type: 'line', data: trendData.map(d => d.windSpeed), smooth: true, lineStyle: { width: 2 }, itemStyle: { color: '#165DFF' }, areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(22,93,255,0.3)' }, { offset: 1, color: 'rgba(22,93,255,0.02)' }] } } },
      { name: '功率(kW)', type: 'line', yAxisIndex: 1, data: trendData.map(d => d.power), smooth: true, lineStyle: { width: 2 }, itemStyle: { color: '#00B42A' }, areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(0,180,42,0.3)' }, { offset: 1, color: 'rgba(0,180,42,0.02)' }] } } }
    ]
  }
}

const initPieChart = (distribution) => {
  const colors = ['#00B42A', '#FF7D00', '#F53F3F', '#FADC19', '#86909C']
  pieOption.value = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: '#1d1d1f',
      borderColor: '#333',
      textStyle: { color: '#F2F3F5' },
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical', right: 10, top: 'middle',
      textStyle: { color: darkTextColor, fontSize: 13 },
      itemWidth: 12, itemHeight: 12, itemGap: 12,
      formatter: (name) => {
        const item = distribution.find(d => d.name === name)
        return item ? `${name}  ${item.value}` : name
      }
    },
    series: [{
      type: 'pie', radius: ['38%', '65%'], center: ['35%', '50%'],
      label: { show: false },
      emphasis: {
        label: { show: true, color: '#F2F3F5', fontSize: 14, fontWeight: 'bold', formatter: '{b}\n{c} ({d}%)' },
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' }
      },
      data: distribution.map((d, i) => ({ ...d, itemStyle: { color: colors[i] || '#86909C' } }))
    }]
  }
}

const initFaultBarChart = (typeDistribution) => {
  const types = Object.keys(typeDistribution)
  const values = Object.values(typeDistribution)
  faultBarOption.value = {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', backgroundColor: '#1d1d1f', borderColor: '#333', textStyle: { color: '#F2F3F5' } },
    grid: { left: 60, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: types, axisLine: { lineStyle: { color: darkAxisColor } }, axisLabel: { color: darkTextColor, rotate: 15 } },
    yAxis: { type: 'value', axisLine: { lineStyle: { color: darkAxisColor } }, axisLabel: { color: darkTextColor }, splitLine: { lineStyle: { color: darkAxisColor, type: 'dashed' } } },
    series: [{ type: 'bar', data: values, barWidth: '40%', itemStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#165DFF' }, { offset: 1, color: 'rgba(22,93,255,0.3)' }] }, borderRadius: [4, 4, 0, 0] } }]
  }
}

const initFaultLevelChart = (levelDistribution) => {
  const levelColors = { LOW: '#00B42A', MEDIUM: '#FF7D00', HIGH: '#F53F3F', CRITICAL: '#7B1FA2' }
  const levelNames = { LOW: '低', MEDIUM: '中', HIGH: '高', CRITICAL: '严重' }
  const data = Object.entries(levelDistribution).map(([k, v]) => ({
    name: levelNames[k] || k, value: v, itemStyle: { color: levelColors[k] || '#86909C' }
  }))
  faultLevelOption.value = {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', backgroundColor: '#1d1d1f', borderColor: '#333', textStyle: { color: '#F2F3F5' } },
    legend: { orient: 'vertical', right: 10, top: 'center', textStyle: { color: darkTextColor } },
    series: [{
      type: 'pie', radius: ['40%', '70%'], center: ['40%', '50%'], roseType: 'area',
      label: { color: darkTextColor }, data,
      emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.5)' } }
    }]
  }
}

onMounted(async () => {
  try {
    const res = await getDashboardStats()
    const d = res.data
    statCards.value[0].value = String(d.totalTurbines || 0)
    statCards.value[1].value = String(d.runningCount || 0)
    statCards.value[2].value = String(d.warningCount || 0)
    statCards.value[3].value = String(d.faultCount || 0)
    statCards.value[4].value = (d.totalCapacity || 0) + ' MW'
    initTrendChart(d.trendData || [])
    initPieChart(d.statusDistribution || [])
  } catch (e) {
    // 接口不可用时使用模拟数据
    statCards.value[0].value = '128'
    statCards.value[1].value = '120'
    statCards.value[2].value = '5'
    statCards.value[3].value = '2'
    statCards.value[4].value = '384.0 MW'
    initTrendChart(generateMockTrend())
    initPieChart([
      { name: '正常运行', value: 120 }, { name: '告警', value: 5 },
      { name: '故障', value: 2 }, { name: '停机维护', value: 1 }, { name: '离线', value: 0 }
    ])
  }

  try {
    const faultRes = await getFaultStatistics()
    const fd = faultRes.data
    initFaultBarChart(fd.typeDistribution || {})
    initFaultLevelChart(fd.levelDistribution || {})
  } catch (e) {
    initFaultBarChart({ '齿轮箱故障': 12, '发电机故障': 8, '叶片故障': 6, '偏航系统': 4, '变桨系统': 3, '电气系统': 5 })
    initFaultLevelChart({ LOW: 15, MEDIUM: 10, HIGH: 8, CRITICAL: 3 })
  }
})

function generateMockTrend() {
  const data = []
  const now = new Date()
  for (let i = 23; i >= 0; i--) {
    const t = new Date(now.getTime() - i * 3600000)
    data.push({
      time: t.toISOString().replace('T', ' ').substring(0, 16),
      windSpeed: (6 + Math.random() * 8).toFixed(1),
      power: (800 + Math.random() * 1200).toFixed(0)
    })
  }
  return data
}
</script>

<style lang="scss" scoped>
.dashboard-page {
  .page-header {
    margin-bottom: $spacing-lg;
    .header-desc { font-size: $font-size-sm; color: $text-secondary; margin-left: $spacing-sm; }
  }
}

.stat-row {
  display: flex; flex-wrap: wrap; gap: 16px;
  .stat-card {
    flex: 1 1 calc(20% - 13px); min-width: 180px; background: $bg-card; border-radius: $border-radius-card;
    padding: $spacing-lg; box-shadow: $shadow-card; border: 1px solid $border-color;
    display: flex; align-items: center; gap: $spacing-md;
    transition: transform 0.2s ease, box-shadow 0.2s ease; cursor: default; white-space: nowrap;
    &:hover { transform: translateY(-3px); box-shadow: 0 4px 20px rgba(0,0,0,0.4); }
  }
  .stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
  .stat-value { font-size: 28px; font-weight: 700; color: $text-primary; line-height: 1; }
  .stat-label { font-size: $font-size-sm; color: $text-secondary; margin-top: 4px; }
}

.chart-row {
  display: flex; gap: 16px; margin-top: 16px;
  .chart-col-left { flex: 3; min-width: 0; }
  .chart-col-right { flex: 2; min-width: 0; }
  .chart-card { width: 100%; display: flex; flex-direction: column; }
  .chart { height: 320px; width: 100%; }
}

.card-title {
  font-size: $font-size-lg; color: $text-primary; margin-bottom: $spacing-md; font-weight: 600;
  padding-bottom: $spacing-sm; border-bottom: 1px solid $border-color; display: flex; align-items: center;
}

.info-row {
  display: flex; gap: 16px; margin-top: 16px;
  .info-col { flex: 1; min-width: 0; display: flex; }
  .info-card { width: 100%; display: flex; flex-direction: column; }
}

.quick-actions {
  display: flex; flex-direction: column; gap: $spacing-sm; flex: 1;
  .quick-action-item {
    display: flex; align-items: center; gap: $spacing-md; padding: 14px 16px; border-radius: 8px;
    border: 1px solid $border-color; background: $bg-hover; cursor: pointer; transition: all 0.2s ease;
    &:hover { border-color: $primary-color; background: rgba(22,93,255,0.06); transform: translateX(4px); }
  }
  .action-icon { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
  .action-title { font-size: 14px; font-weight: 600; color: $text-primary; }
  .action-desc { font-size: 12px; color: $text-secondary; margin-top: 2px; }
}

:deep(.arco-descriptions-row) {
  .arco-descriptions-item-label { background: $bg-hover !important; color: $text-secondary !important; border-color: $border-color !important; }
  .arco-descriptions-item-value { background: $bg-card !important; color: $text-primary !important; border-color: $border-color !important; }
}

@media (max-width: 1200px) {
  .stat-row .stat-card { flex: 1 1 calc(50% - 8px); }
  .chart-row, .info-row { flex-direction: column; }
}
</style>
