<template>
  <div class="monitor-page">
    <div class="page-header">
      <h3>实时监控</h3>
      <div class="header-actions">
        <a-select v-model="selectedTurbine" placeholder="选择设备" style="width:220px" allow-clear @change="onTurbineChange">
          <a-option v-for="t in turbineList" :key="t.id" :value="t.id">{{ t.turbineCode }} - {{ t.turbineName }}</a-option>
        </a-select>
        <a-button type="primary" @click="refreshData" :loading="loading"><icon-refresh /> 刷新数据</a-button>
      </div>
    </div>

    <!-- 实时指标卡片 -->
    <div class="metric-row">
      <div class="metric-card" v-for="m in metrics" :key="m.label">
        <div class="metric-icon" :style="{ color: m.color }"><component :is="m.icon" :size="20" /></div>
        <div class="metric-body">
          <div class="metric-value" :style="{ color: m.color }">{{ m.value }}</div>
          <div class="metric-label">{{ m.label }}</div>
        </div>
      </div>
    </div>

    <!-- 设备列表 -->
    <div class="page-card" style="margin-top:16px">
      <h4 class="card-title"><icon-computer style="margin-right:8px;color:#165DFF" />设备运行状态</h4>
      <a-table :data="turbineList" :pagination="false" :bordered="false" size="medium" :scroll="{ y: 300 }">
        <template #columns>
          <a-table-column title="设备编号" data-index="turbineCode" :width="120" />
          <a-table-column title="设备名称" data-index="turbineName" :width="150" />
          <a-table-column title="型号" data-index="model" :width="120" />
          <a-table-column title="额定功率(MW)" data-index="ratedPower" :width="120" />
          <a-table-column title="位置" data-index="location" :width="150" />
          <a-table-column title="状态" :width="100">
            <template #cell="{ record }">
              <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>

    <!-- 传感器趋势图 -->
    <div class="chart-row" style="margin-top:16px">
      <div class="chart-col" style="flex:1">
        <div class="page-card chart-card">
          <h4 class="card-title"><icon-line-chart style="margin-right:8px;color:#165DFF" />风速与转速趋势</h4>
          <v-chart class="chart" :option="windChartOption" autoresize />
        </div>
      </div>
      <div class="chart-col" style="flex:1">
        <div class="page-card chart-card">
          <h4 class="card-title"><icon-fire style="margin-right:8px;color:#F53F3F" />温度监测</h4>
          <v-chart class="chart" :option="tempChartOption" autoresize />
        </div>
      </div>
    </div>

    <div class="chart-row" style="margin-top:16px">
      <div class="chart-col" style="flex:1">
        <div class="page-card chart-card">
          <h4 class="card-title"><icon-shake style="margin-right:8px;color:#FF7D00" />振动监测</h4>
          <v-chart class="chart" :option="vibrationChartOption" autoresize />
        </div>
      </div>
      <div class="chart-col" style="flex:1">
        <div class="page-card chart-card">
          <h4 class="card-title"><icon-thunderbolt style="margin-right:8px;color:#00B42A" />功率输出</h4>
          <v-chart class="chart" :option="powerChartOption" autoresize />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { getTurbineList, getMonitorData } from '@/api/turbine'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const loading = ref(false)
const selectedTurbine = ref(null)
const turbineList = ref([])
let refreshTimer = null

const darkText = '#A9AEB8'
const darkAxis = '#333335'

const metrics = ref([
  { label: '风速', value: '--', color: '#165DFF', icon: 'icon-cloud' },
  { label: '转速', value: '--', color: '#00B42A', icon: 'icon-sync' },
  { label: '功率', value: '--', color: '#FF7D00', icon: 'icon-thunderbolt' },
  { label: '机舱温度', value: '--', color: '#F53F3F', icon: 'icon-fire' },
  { label: '轴承温度', value: '--', color: '#EB2F96', icon: 'icon-fire' },
  { label: '齿轮箱温度', value: '--', color: '#722ED1', icon: 'icon-fire' }
])

const windChartOption = ref({})
const tempChartOption = ref({})
const vibrationChartOption = ref({})
const powerChartOption = ref({})

const statusText = (s) => ({ 1: '正常运行', 2: '告警', 3: '故障', 4: '停机维护', 5: '离线' }[s] || '未知')
const statusColor = (s) => ({ 1: 'green', 2: 'orangered', 3: 'red', 4: 'blue', 5: 'gray' }[s] || 'gray')

function makeLineOption(legend, series) {
  return {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', backgroundColor: '#1d1d1f', borderColor: '#333', textStyle: { color: '#F2F3F5' } },
    legend: { data: legend, textStyle: { color: darkText }, top: 0 },
    grid: { left: 50, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: [], axisLine: { lineStyle: { color: darkAxis } }, axisLabel: { color: darkText } },
    yAxis: { type: 'value', axisLine: { lineStyle: { color: darkAxis } }, axisLabel: { color: darkText }, splitLine: { lineStyle: { color: darkAxis, type: 'dashed' } } },
    series
  }
}

function updateCharts(sensorData) {
  const times = sensorData.map(d => {
    const t = d.recordTime || d.record_time || ''
    return typeof t === 'string' ? t.substring(11, 16) : ''
  })

  windChartOption.value = {
    ...makeLineOption(['风速(m/s)', '转速(rpm)'], [
      { name: '风速(m/s)', type: 'line', data: sensorData.map(d => d.windSpeed), smooth: true, itemStyle: { color: '#165DFF' } },
      { name: '转速(rpm)', type: 'line', data: sensorData.map(d => d.rotorSpeed), smooth: true, itemStyle: { color: '#00B42A' } }
    ]),
    xAxis: { type: 'category', data: times, axisLine: { lineStyle: { color: darkAxis } }, axisLabel: { color: darkText } }
  }

  tempChartOption.value = {
    ...makeLineOption(['机舱温度', '轴承温度', '齿轮箱温度'], [
      { name: '机舱温度', type: 'line', data: sensorData.map(d => d.nacelleTemp), smooth: true, itemStyle: { color: '#F53F3F' } },
      { name: '轴承温度', type: 'line', data: sensorData.map(d => d.bearingTemp), smooth: true, itemStyle: { color: '#EB2F96' } },
      { name: '齿轮箱温度', type: 'line', data: sensorData.map(d => d.gearboxTemp), smooth: true, itemStyle: { color: '#722ED1' } }
    ]),
    xAxis: { type: 'category', data: times, axisLine: { lineStyle: { color: darkAxis } }, axisLabel: { color: darkText } }
  }

  vibrationChartOption.value = {
    ...makeLineOption(['X轴振动', 'Y轴振动', 'Z轴振动'], [
      { name: 'X轴振动', type: 'line', data: sensorData.map(d => d.vibrationX), smooth: true, itemStyle: { color: '#FF7D00' } },
      { name: 'Y轴振动', type: 'line', data: sensorData.map(d => d.vibrationY), smooth: true, itemStyle: { color: '#165DFF' } },
      { name: 'Z轴振动', type: 'line', data: sensorData.map(d => d.vibrationZ), smooth: true, itemStyle: { color: '#00B42A' } }
    ]),
    xAxis: { type: 'category', data: times, axisLine: { lineStyle: { color: darkAxis } }, axisLabel: { color: darkText } }
  }

  powerChartOption.value = {
    ...makeLineOption(['功率(kW)'], [
      { name: '功率(kW)', type: 'line', data: sensorData.map(d => d.power), smooth: true, itemStyle: { color: '#00B42A' },
        areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(0,180,42,0.3)' }, { offset: 1, color: 'rgba(0,180,42,0.02)' }] } } }
    ]),
    xAxis: { type: 'category', data: times, axisLine: { lineStyle: { color: darkAxis } }, axisLabel: { color: darkText } }
  }
}

function updateMetrics(latest) {
  if (!latest) return
  metrics.value[0].value = (latest.windSpeed || '--') + ' m/s'
  metrics.value[1].value = (latest.rotorSpeed || '--') + ' rpm'
  metrics.value[2].value = (latest.power || '--') + ' kW'
  metrics.value[3].value = (latest.nacelleTemp || '--') + ' °C'
  metrics.value[4].value = (latest.bearingTemp || '--') + ' °C'
  metrics.value[5].value = (latest.gearboxTemp || '--') + ' °C'
}

function generateMockSensor() {
  const data = []
  const now = new Date()
  for (let i = 23; i >= 0; i--) {
    const t = new Date(now.getTime() - i * 3600000)
    data.push({
      recordTime: t.toISOString().replace('T', ' ').substring(0, 16),
      windSpeed: +(6 + Math.random() * 8).toFixed(1),
      rotorSpeed: +(10 + Math.random() * 5).toFixed(1),
      power: +(800 + Math.random() * 1200).toFixed(0),
      nacelleTemp: +(35 + Math.random() * 15).toFixed(1),
      bearingTemp: +(40 + Math.random() * 20).toFixed(1),
      gearboxTemp: +(45 + Math.random() * 15).toFixed(1),
      vibrationX: +(0.5 + Math.random() * 2).toFixed(2),
      vibrationY: +(0.3 + Math.random() * 1.5).toFixed(2),
      vibrationZ: +(0.2 + Math.random() * 1).toFixed(2)
    })
  }
  return data
}

async function refreshData() {
  loading.value = true
  try {
    const res = await getMonitorData({ turbineId: selectedTurbine.value })
    const sensorData = res.data?.sensorData || []
    const latest = res.data?.latest
    if (sensorData.length > 0) {
      updateCharts(sensorData)
      updateMetrics(latest)
    } else {
      const mock = generateMockSensor()
      updateCharts(mock)
      updateMetrics(mock[mock.length - 1])
    }
  } catch (e) {
    const mock = generateMockSensor()
    updateCharts(mock)
    updateMetrics(mock[mock.length - 1])
  } finally {
    loading.value = false
  }
}

function onTurbineChange() {
  refreshData()
}

onMounted(async () => {
  try {
    const res = await getTurbineList()
    turbineList.value = res.data || []
  } catch (e) {
    turbineList.value = Array.from({ length: 8 }, (_, i) => ({
      id: i + 1, turbineCode: `WT-${String(i + 1).padStart(3, '0')}`,
      turbineName: `${i + 1}号风机`, model: 'GW-3.0MW', ratedPower: 3.0,
      location: '风电场A区', status: i < 6 ? 1 : i < 7 ? 2 : 3
    }))
  }
  refreshData()
  refreshTimer = setInterval(refreshData, 30000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style lang="scss" scoped>
.monitor-page {
  .page-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-lg; flex-wrap: wrap; gap: 12px;
    .header-actions { display: flex; gap: 12px; align-items: center; }
  }
}

.metric-row {
  display: flex; flex-wrap: wrap; gap: 12px;
  .metric-card {
    flex: 1 1 calc(16.66% - 10px); min-width: 150px; background: $bg-card; border-radius: $border-radius-card;
    padding: 16px; box-shadow: $shadow-card; border: 1px solid $border-color;
    display: flex; align-items: center; gap: 12px;
    transition: transform 0.2s; &:hover { transform: translateY(-2px); }
  }
  .metric-icon { font-size: 20px; }
  .metric-value { font-size: 22px; font-weight: 700; color: $text-primary; }
  .metric-label { font-size: 12px; color: $text-secondary; margin-top: 2px; }
}

.card-title {
  font-size: $font-size-lg; color: $text-primary; margin-bottom: $spacing-md; font-weight: 600;
  padding-bottom: $spacing-sm; border-bottom: 1px solid $border-color; display: flex; align-items: center;
}

.chart-row {
  display: flex; gap: 16px;
  .chart-col { min-width: 0; display: flex; }
  .chart-card { width: 100%; display: flex; flex-direction: column; }
  .chart { height: 280px; width: 100%; }
}

@media (max-width: 1200px) {
  .metric-row .metric-card { flex: 1 1 calc(33% - 8px); }
  .chart-row { flex-direction: column; }
}
</style>
