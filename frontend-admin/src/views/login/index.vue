<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="particle" v-for="i in 20" :key="i" :style="particleStyle(i)"></div>
    </div>
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <icon-thunderbolt :size="36" style="color: #165DFF" />
          <h1>风力发电机故障诊断系统</h1>
          <p>Wind Turbine Fault Diagnosis System</p>
        </div>

        <a-tabs v-model:active-key="activeTab" class="login-tabs">
          <a-tab-pane key="login" title="登录">
            <a-form :model="loginForm" layout="vertical" @submit="handleLogin" autocomplete="off">
              <a-form-item label="用户名" required>
                <a-input v-model="loginForm.username" placeholder="请输入用户名" size="large" autocomplete="new-password">
                  <template #prefix><icon-user /></template>
                </a-input>
              </a-form-item>
              <a-form-item label="密码" required>
                <a-input-password v-model="loginForm.password" placeholder="请输入密码" size="large" autocomplete="new-password">
                  <template #prefix><icon-lock /></template>
                </a-input-password>
              </a-form-item>
              <a-button type="primary" html-type="submit" long size="large" :loading="loginLoading" class="login-btn">
                登 录
              </a-button>
            </a-form>
          </a-tab-pane>

          <a-tab-pane key="register" title="注册">
            <a-form :model="registerForm" layout="vertical" @submit="handleRegister" autocomplete="off">
              <a-form-item label="用户名" required>
                <a-input v-model="registerForm.username" placeholder="请输入用户名" size="large" autocomplete="new-password">
                  <template #prefix><icon-user /></template>
                </a-input>
              </a-form-item>
              <a-form-item label="真实姓名">
                <a-input v-model="registerForm.realName" placeholder="请输入真实姓名" size="large" autocomplete="new-password">
                  <template #prefix><icon-idcard /></template>
                </a-input>
              </a-form-item>
              <a-form-item label="邮箱" required>
                <a-input v-model="registerForm.email" placeholder="请输入邮箱（用于验证）" size="large" autocomplete="new-password">
                  <template #prefix><icon-email /></template>
                </a-input>
              </a-form-item>
              <a-form-item label="密码" required>
                <a-input-password v-model="registerForm.password" placeholder="请输入密码（至少6位）" size="large" autocomplete="new-password">
                  <template #prefix><icon-lock /></template>
                </a-input-password>
              </a-form-item>
              <a-button type="primary" html-type="submit" long size="large" :loading="registerLoading" class="login-btn">
                注 册
              </a-button>
            </a-form>

            <!-- 注册成功后的邮箱验证提示 -->
            <div v-if="showVerifyTip" class="verify-tip">
              <icon-check-circle-fill style="color: #00b42a; font-size: 20px;" />
              <div class="verify-tip-content">
                <p>注册成功！验证邮件已发送至 <strong>{{ registeredEmail }}</strong></p>
                <p class="verify-tip-sub">请前往邮箱点击验证链接完成验证，验证后等待管理员审核。</p>
                <a-button type="text" size="small" :loading="resendLoading" @click="handleResendVerify">
                  未收到邮件？重新发送
                </a-button>
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { register, resendVerifyEmail } from '@/api/auth'
import { Message } from '@arco-design/web-vue'
import { containsXss, stripTags } from '@/utils/xss'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loginLoading = ref(false)
const registerLoading = ref(false)
const showVerifyTip = ref(false)
const registeredEmail = ref('')
const resendLoading = ref(false)

const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', password: '', realName: '', email: '' })

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    Message.warning('请输入用户名和密码')
    return
  }
  // XSS 检测
  if (containsXss(loginForm.value.username)) {
    Message.warning('用户名包含不安全内容')
    return
  }
  loginLoading.value = true
  try {
    await userStore.login(loginForm.value.username, loginForm.value.password)
    Message.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // handled by interceptor
  } finally {
    loginLoading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.value.username || !registerForm.value.password) {
    Message.warning('请输入用户名和密码')
    return
  }
  if (!registerForm.value.email) {
    Message.warning('请输入邮箱，注册需要进行邮箱验证')
    return
  }
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (!emailRegex.test(registerForm.value.email)) {
    Message.warning('邮箱格式不正确，请输入有效的邮箱地址')
    return
  }
  if (registerForm.value.password.length < 6) {
    Message.warning('密码长度不少于6位')
    return
  }
  // XSS 检测
  const fields = ['username', 'realName', 'email']
  for (const field of fields) {
    const val = registerForm.value[field]
    if (val && containsXss(val)) {
      Message.warning(`${field} 中包含不安全的内容，请修改`)
      return
    }
    if (val && typeof val === 'string') {
      registerForm.value[field] = stripTags(val)
    }
  }
  registerLoading.value = true
  try {
    await register(registerForm.value)
    Message.success('注册成功，验证邮件已发送至您的邮箱')
    registeredEmail.value = registerForm.value.email
    showVerifyTip.value = true
    registerForm.value = { username: '', password: '', realName: '', email: '' }
  } catch (e) {
    // handled by interceptor
  } finally {
    registerLoading.value = false
  }
}

const handleResendVerify = async () => {
  if (!registeredEmail.value) {
    Message.warning('邮箱信息丢失，请重新注册')
    return
  }
  resendLoading.value = true
  try {
    await resendVerifyEmail({ email: registeredEmail.value })
    Message.success('验证邮件已重新发送，请查收')
  } catch (e) {
    // handled by interceptor
  } finally {
    resendLoading.value = false
  }
}

const particleStyle = (i) => ({
  left: `${Math.random() * 100}%`,
  top: `${Math.random() * 100}%`,
  width: `${2 + Math.random() * 4}px`,
  height: `${2 + Math.random() * 4}px`,
  animationDelay: `${Math.random() * 5}s`,
  animationDuration: `${3 + Math.random() * 4}s`
})
</script>

<style lang="scss" scoped>
.login-page {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0a0a12 0%, #17171a 50%, #0d1b2a 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  .particle {
    position: absolute;
    background: rgba(22, 93, 255, 0.3);
    border-radius: 50%;
    animation: float 4s ease-in-out infinite;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); opacity: 0.3; }
  50% { transform: translateY(-20px) scale(1.2); opacity: 0.8; }
}

.login-container {
  position: relative;
  z-index: 1;
}

.login-card {
  width: 420px;
  background: rgba(35, 35, 36, 0.95);
  border-radius: 12px;
  padding: 40px 36px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(51, 51, 53, 0.6);
  backdrop-filter: blur(10px);
  transition: box-shadow 0.3s ease, transform 0.3s ease;
  &:hover {
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.6);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
  h1 {
    font-size: 22px;
    color: #F2F3F5;
    margin-top: 12px;
    font-weight: 600;
  }
  p {
    font-size: 12px;
    color: #6B7075;
    margin-top: 4px;
    letter-spacing: 1px;
  }
}

.login-tabs {
  :deep(.arco-tabs-nav-tab) {
    justify-content: center;
  }
  :deep(.arco-tabs-tab) {
    color: #A9AEB8;
    &.arco-tabs-tab-active {
      color: #165DFF;
    }
  }
}

.login-btn {
  margin-top: 8px;
  height: 44px;
  font-size: 16px;
  border-radius: 6px;
  transition: all 0.2s ease;
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.3);
  }
  &:active {
    transform: translateY(0);
  }
}

:deep(.arco-input-wrapper) {
  background: #2a2a2c !important;
  border-color: #333335 !important;
  height: 44px;
  border-radius: 6px;
}
:deep(.arco-input-wrapper:hover),
:deep(.arco-input-wrapper.arco-input-focus) {
  border-color: #165DFF !important;
}
:deep(.arco-form-label-item > label) {
  color: #C9CDD4 !important;
}

/* 消除浏览器自动填充的背景色 */
:deep(input:-webkit-autofill),
:deep(input:-webkit-autofill:hover),
:deep(input:-webkit-autofill:focus),
:deep(input:-webkit-autofill:active) {
  -webkit-box-shadow: 0 0 0 1000px #2a2a2c inset !important;
  -webkit-text-fill-color: #F2F3F5 !important;
  transition: background-color 5000s ease-in-out 0s;
  caret-color: #F2F3F5;
}

.verify-tip {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-top: 20px;
  padding: 16px;
  background: rgba(0, 180, 42, 0.08);
  border: 1px solid rgba(0, 180, 42, 0.2);
  border-radius: 8px;
  .verify-tip-content {
    flex: 1;
    p {
      color: #C9CDD4;
      margin: 0;
      font-size: 14px;
      strong { color: #165DFF; }
    }
    .verify-tip-sub {
      color: #86909C;
      font-size: 12px;
      margin-top: 4px;
    }
  }
}
</style>
