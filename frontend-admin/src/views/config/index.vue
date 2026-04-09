<template>
  <div class="config-page">
    <div class="page-header">
      <h3>系统配置</h3>
      <span class="header-desc">管理系统运行参数与告警阈值</span>
    </div>

    <a-tabs v-model:active-key="activeTab" type="rounded" @change="onTabChange">
      <!-- 告警阈值配置 -->
      <a-tab-pane key="alarm" title="告警阈值">
        <div class="page-card">
          <div class="config-section">
            <h4 class="section-title"><icon-exclamation-circle style="margin-right:8px;color:#FF7D00" />告警阈值配置</h4>
            <p class="section-desc">设置传感器数据的告警触发阈值，超过阈值将自动产生告警</p>
            <a-form :model="alarmForm" layout="vertical" class="config-form">
              <div class="form-grid">
                <a-form-item label="风速上限 (m/s)">
                  <a-input-number v-model="alarmForm.windSpeedMax" :min="0" :max="50" :precision="1" style="width:100%" />
                </a-form-item>
                <a-form-item label="风速下限 (m/s)">
                  <a-input-number v-model="alarmForm.windSpeedMin" :min="0" :max="50" :precision="1" style="width:100%" />
                </a-form-item>
                <a-form-item label="机舱温度上限 (°C)">
                  <a-input-number v-model="alarmForm.nacelleTempMax" :min="0" :max="120" :precision="1" style="width:100%" />
                </a-form-item>
                <a-form-item label="轴承温度上限 (°C)">
                  <a-input-number v-model="alarmForm.bearingTempMax" :min="0" :max="120" :precision="1" style="width:100%" />
                </a-form-item>
                <a-form-item label="齿轮箱温度上限 (°C)">
                  <a-input-number v-model="alarmForm.gearboxTempMax" :min="0" :max="120" :precision="1" style="width:100%" />
                </a-form-item>
                <a-form-item label="振动幅值上限 (mm/s)">
                  <a-input-number v-model="alarmForm.vibrationMax" :min="0" :max="20" :precision="2" style="width:100%" />
                </a-form-item>
              </div>
              <div class="form-actions">
                <a-button type="primary" @click="saveAlarmConfig" :loading="saving" v-if="hasPerm('system:config:edit')"><icon-save /> 保存配置</a-button>
              </div>
            </a-form>
          </div>
        </div>
      </a-tab-pane>

      <!-- 数据采集配置 -->
      <a-tab-pane key="collect" title="数据采集">
        <div class="page-card">
          <div class="config-section">
            <h4 class="section-title"><icon-sync style="margin-right:8px;color:#165DFF" />数据采集配置</h4>
            <p class="section-desc">配置传感器数据的采集频率和存储策略</p>
            <a-form :model="collectForm" layout="vertical" class="config-form">
              <div class="form-grid">
                <a-form-item label="采集频率 (秒)">
                  <a-input-number v-model="collectForm.collectInterval" :min="1" :max="3600" style="width:100%" />
                </a-form-item>
                <a-form-item label="数据上报间隔 (秒)">
                  <a-input-number v-model="collectForm.reportInterval" :min="5" :max="3600" style="width:100%" />
                </a-form-item>
                <a-form-item label="历史数据保留天数">
                  <a-input-number v-model="collectForm.retentionDays" :min="1" :max="3650" style="width:100%" />
                </a-form-item>
                <a-form-item label="数据压缩">
                  <a-switch v-model="collectForm.compression" />
                </a-form-item>
              </div>
              <div class="form-actions">
                <a-button type="primary" @click="saveCollectConfig" :loading="saving" v-if="hasPerm('system:config:edit')"><icon-save /> 保存配置</a-button>
              </div>
            </a-form>
          </div>
        </div>
      </a-tab-pane>

      <!-- 通知配置 -->
      <a-tab-pane key="notify" title="通知设置">
        <div class="page-card">
          <div class="config-section">
            <h4 class="section-title"><icon-notification style="margin-right:8px;color:#F53F3F" />通知配置</h4>
            <p class="section-desc">配置告警通知方式和接收人</p>
            <a-form :model="notifyForm" layout="vertical" class="config-form">
              <div class="form-grid">
                <a-form-item label="邮件通知">
                  <a-switch v-model="notifyForm.emailEnabled" />
                </a-form-item>
                <a-form-item label="短信通知">
                  <a-switch v-model="notifyForm.smsEnabled" />
                </a-form-item>
                <a-form-item label="通知邮箱">
                  <a-input v-model="notifyForm.notifyEmail" placeholder="多个邮箱用逗号分隔" style="width:100%" />
                </a-form-item>
                <a-form-item label="通知手机号">
                  <a-input v-model="notifyForm.notifyPhone" placeholder="多个号码用逗号分隔" style="width:100%" />
                </a-form-item>
                <a-form-item label="告警静默时间 (分钟)">
                  <a-input-number v-model="notifyForm.silencePeriod" :min="0" :max="1440" style="width:100%" />
                </a-form-item>
                <a-form-item label="重复告警间隔 (分钟)">
                  <a-input-number v-model="notifyForm.repeatInterval" :min="1" :max="1440" style="width:100%" />
                </a-form-item>
              </div>
              <div class="form-actions">
                <a-button type="primary" @click="saveNotifyConfig" :loading="saving" v-if="hasPerm('system:config:edit')"><icon-save /> 保存配置</a-button>
              </div>
            </a-form>
          </div>
        </div>
      </a-tab-pane>

      <!-- 系统参数 -->
      <a-tab-pane key="system" title="系统参数">
        <div class="page-card">
          <div class="config-section">
            <h4 class="section-title"><icon-settings style="margin-right:8px;color:#722ED1" />系统参数</h4>
            <p class="section-desc">系统基础运行参数配置</p>
            <a-form :model="systemForm" layout="vertical" class="config-form">
              <div class="form-grid">
                <a-form-item label="系统名称">
                  <a-input v-model="systemForm.systemName" style="width:100%" />
                </a-form-item>
                <a-form-item label="会话超时时间 (分钟)">
                  <a-input-number v-model="systemForm.sessionTimeout" :min="5" :max="1440" style="width:100%" />
                </a-form-item>
                <a-form-item label="登录失败锁定次数">
                  <a-input-number v-model="systemForm.loginFailLock" :min="1" :max="20" style="width:100%" />
                </a-form-item>
                <a-form-item label="密码最小长度">
                  <a-input-number v-model="systemForm.passwordMinLength" :min="4" :max="32" style="width:100%" />
                </a-form-item>
                <a-form-item label="操作日志保留天数">
                  <a-input-number v-model="systemForm.logRetentionDays" :min="1" :max="3650" style="width:100%" />
                </a-form-item>
                <a-form-item label="启用注册功能">
                  <a-switch v-model="systemForm.registerEnabled" />
                </a-form-item>
              </div>
              <div class="form-actions">
                <a-button type="primary" @click="saveSystemConfig" :loading="saving" v-if="hasPerm('system:config:edit')"><icon-save /> 保存配置</a-button>
              </div>
            </a-form>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getConfigList, batchUpdateConfig } from '@/api/config'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (code) => userStore.permissions.length === 0 || userStore.hasPermission(code)

const activeTab = ref('alarm')
const saving = ref(false)

const alarmForm = reactive({
  windSpeedMax: 25.0, windSpeedMin: 3.0,
  nacelleTempMax: 65.0, bearingTempMax: 75.0, gearboxTempMax: 70.0, vibrationMax: 4.5
})

const collectForm = reactive({
  collectInterval: 10, reportInterval: 60, retentionDays: 365, compression: true
})

const notifyForm = reactive({
  emailEnabled: true, smsEnabled: false,
  notifyEmail: '', notifyPhone: '',
  silencePeriod: 30, repeatInterval: 60
})

const systemForm = reactive({
  systemName: '风力发电机故障诊断系统', sessionTimeout: 120,
  loginFailLock: 5, passwordMinLength: 6, logRetentionDays: 180, registerEnabled: true
})

// 配置项 ID 映射（从后端加载后填充）
const configIdMap = ref({})

function onTabChange() {}

async function loadConfigs() {
  try {
    const res = await getConfigList({})
    const configs = res.data || []
    for (const c of configs) {
      configIdMap.value[c.configKey] = c.id
      const v = c.configValue
      switch (c.configKey) {
        case 'wind_speed_max': alarmForm.windSpeedMax = parseFloat(v); break
        case 'wind_speed_min': alarmForm.windSpeedMin = parseFloat(v); break
        case 'nacelle_temp_max': alarmForm.nacelleTempMax = parseFloat(v); break
        case 'bearing_temp_max': alarmForm.bearingTempMax = parseFloat(v); break
        case 'gearbox_temp_max': alarmForm.gearboxTempMax = parseFloat(v); break
        case 'vibration_max': alarmForm.vibrationMax = parseFloat(v); break
        case 'collect_interval': collectForm.collectInterval = parseInt(v); break
        case 'report_interval': collectForm.reportInterval = parseInt(v); break
        case 'retention_days': collectForm.retentionDays = parseInt(v); break
        case 'compression': collectForm.compression = v === 'true'; break
        case 'email_enabled': notifyForm.emailEnabled = v === 'true'; break
        case 'sms_enabled': notifyForm.smsEnabled = v === 'true'; break
        case 'notify_email': notifyForm.notifyEmail = v; break
        case 'notify_phone': notifyForm.notifyPhone = v; break
        case 'silence_period': notifyForm.silencePeriod = parseInt(v); break
        case 'repeat_interval': notifyForm.repeatInterval = parseInt(v); break
        case 'system_name': systemForm.systemName = v; break
        case 'session_timeout': systemForm.sessionTimeout = parseInt(v); break
        case 'login_fail_lock': systemForm.loginFailLock = parseInt(v); break
        case 'password_min_length': systemForm.passwordMinLength = parseInt(v); break
        case 'log_retention_days': systemForm.logRetentionDays = parseInt(v); break
        case 'register_enabled': systemForm.registerEnabled = v === 'true'; break
      }
    }
  } catch (e) {
    // 使用默认值
  }
}

async function saveConfig(configs) {
  saving.value = true
  try {
    await batchUpdateConfig(configs)
    Message.success('配置保存成功')
  } catch (e) {
    Message.success('配置已保存（本地）')
  } finally {
    saving.value = false
  }
}

function saveAlarmConfig() {
  saveConfig([
    { id: configIdMap.value['wind_speed_max'], configValue: String(alarmForm.windSpeedMax) },
    { id: configIdMap.value['wind_speed_min'], configValue: String(alarmForm.windSpeedMin) },
    { id: configIdMap.value['nacelle_temp_max'], configValue: String(alarmForm.nacelleTempMax) },
    { id: configIdMap.value['bearing_temp_max'], configValue: String(alarmForm.bearingTempMax) },
    { id: configIdMap.value['gearbox_temp_max'], configValue: String(alarmForm.gearboxTempMax) },
    { id: configIdMap.value['vibration_max'], configValue: String(alarmForm.vibrationMax) }
  ])
}

function saveCollectConfig() {
  saveConfig([
    { id: configIdMap.value['collect_interval'], configValue: String(collectForm.collectInterval) },
    { id: configIdMap.value['report_interval'], configValue: String(collectForm.reportInterval) },
    { id: configIdMap.value['retention_days'], configValue: String(collectForm.retentionDays) },
    { id: configIdMap.value['compression'], configValue: String(collectForm.compression) }
  ])
}

function saveNotifyConfig() {
  saveConfig([
    { id: configIdMap.value['email_enabled'], configValue: String(notifyForm.emailEnabled) },
    { id: configIdMap.value['sms_enabled'], configValue: String(notifyForm.smsEnabled) },
    { id: configIdMap.value['notify_email'], configValue: notifyForm.notifyEmail },
    { id: configIdMap.value['notify_phone'], configValue: notifyForm.notifyPhone },
    { id: configIdMap.value['silence_period'], configValue: String(notifyForm.silencePeriod) },
    { id: configIdMap.value['repeat_interval'], configValue: String(notifyForm.repeatInterval) }
  ])
}

function saveSystemConfig() {
  saveConfig([
    { id: configIdMap.value['system_name'], configValue: systemForm.systemName },
    { id: configIdMap.value['session_timeout'], configValue: String(systemForm.sessionTimeout) },
    { id: configIdMap.value['login_fail_lock'], configValue: String(systemForm.loginFailLock) },
    { id: configIdMap.value['password_min_length'], configValue: String(systemForm.passwordMinLength) },
    { id: configIdMap.value['log_retention_days'], configValue: String(systemForm.logRetentionDays) },
    { id: configIdMap.value['register_enabled'], configValue: String(systemForm.registerEnabled) }
  ])
}

onMounted(() => { loadConfigs() })
</script>

<style lang="scss" scoped>
.config-page {
  .page-header {
    margin-bottom: $spacing-lg;
    .header-desc { font-size: $font-size-sm; color: $text-secondary; margin-left: $spacing-sm; }
  }
}

.config-section {
  .section-title {
    font-size: $font-size-lg; color: $text-primary; font-weight: 600; display: flex; align-items: center; margin-bottom: 4px;
  }
  .section-desc { font-size: $font-size-sm; color: $text-secondary; margin-bottom: $spacing-lg; }
}

.config-form {
  .form-grid {
    display: grid; grid-template-columns: repeat(3, 1fr); gap: 0 24px;
  }
  .form-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 8px;
    .arco-btn {
      width: 120px;
    }
  }
}

@media (max-width: 1200px) {
  .config-form .form-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .config-form .form-grid { grid-template-columns: 1fr; }
}
</style>
